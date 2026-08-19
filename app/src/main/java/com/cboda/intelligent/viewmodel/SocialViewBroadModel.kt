package com.cboda.intelligent.viewmodel

import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.bean.SocialContact
import com.cboda.baselibrary.bean.SocialUiState
import com.cboda.baselibrary.event.UnreadCountBroadEvent
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.eLogD
import com.cboda.chatui.utils.PinyinBroadUtils
import com.cboda.intelligent.repository.SocialBroadRepository
import org.greenrobot.eventbus.EventBus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SocialViewBroadModel(
    private val repository: SocialBroadRepository = SocialBroadRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(SocialUiState(isLoading = true))
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()

    private var overlayJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {

                val circles = repository.fetchFriendshipCharacter()
                val contacts = repository.fetchFriendShipList()
                val newFriendCount = repository.fetchNewFriendCount()
                val newGroupInviteCount = repository.fetchNewGroupInviteCount()
                val selected = _uiState.value.selectedCircleTag
                val filtered = filterContacts(selected, contacts)
                val grouped = groupByInitial(filtered)
                val letters = reorderLetters(grouped.keys.toList())
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    circles = circles,
                    contactsAll = contacts,
                    filteredContacts = filtered,
                    groupedByLetter = grouped,
                    indexLetters = letters,
                    newFriendCount = newFriendCount,
                    newGroupInviteCount = newGroupInviteCount
                )
                EventBus.getDefault().post(
                    UnreadCountBroadEvent(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        MMKVBroadUtils.getContactBadgeTotal().toString()
                    )
                )
            } catch (t: Throwable) {
                t.printStackTrace()
                eLogD("SocialViewBroadModel","错误:${t.message}")
                _uiState.value = _uiState.value.copy(isLoading = false, error = t.message)
            }
        }
    }

    fun selectCircle(tag: String?) {
        viewModelScope.launch {
            val contacts = _uiState.value.contactsAll
            val filtered = filterContacts(tag, contacts)
            val grouped = groupByInitial(filtered)
            val letters = reorderLetters(grouped.keys.toList())
            _uiState.value = _uiState.value.copy(
                selectedCircleTag = tag,
                filteredContacts = filtered,
                groupedByLetter = grouped,
                indexLetters = letters
            )
        }
    }

    fun onIndexLetterSelected(letter: Char) {
        overlayJob?.cancel()
        _uiState.value = _uiState.value.copy(overlayLetter = letter, showOverlay = true)
        overlayJob = viewModelScope.launch {
            delay(800)
            _uiState.value = _uiState.value.copy(showOverlay = false)
        }
    }

    private fun filterContacts(tag: String?, contacts: List<SocialContact>): List<SocialContact> {
        val filtered = if (tag.isNullOrBlank()) contacts else contacts.filter { it.characterTags == tag }
        return filtered.sortedWith(compareBy(
            { displayName(it).uppercase() },
            { it.userId }
        ))
    }

    private fun displayName(c: SocialContact): String {
        val name = c.remarkName?.takeIf { it.isNotBlank() } ?: (c.nickName?:c.userId)
        return name
    }

    private fun initialOf(contact: SocialContact): Char {
        val name = displayName(contact)
        val firstChar = name.firstOrNull() ?: return '#'
        val py = PinyinBroadUtils.getPinyin(firstChar.toString())
        val ch = py.firstOrNull()
        return if (ch != null && ch.isLetter()) ch.uppercaseChar() else '#'
    }

    private fun groupByInitial(list: List<SocialContact>): Map<Char, List<SocialContact>> {
        return list.groupBy { initialOf(it) }
            .mapValues { (_, v) -> v.sortedBy { displayName(it).uppercase() } }
    }

    private fun reorderLetters(keys: List<Char>): List<Char> {
        val letters = keys.filter { it.isLetter() }.sorted()
        return if (keys.contains('#')) letters + listOf('#') else letters
    }
}