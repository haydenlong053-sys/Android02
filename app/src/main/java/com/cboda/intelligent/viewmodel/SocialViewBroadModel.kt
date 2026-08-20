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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class SocialViewBroadModel(
    private val repository: SocialBroadRepository = SocialBroadRepository()
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(SocialUiState(isLoading = true))
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()

    private var overlayJob: Job? = null
    private var refreshJob: Job? = null
    private var lastPostedBadgeTotal: Long? = null

    init {
        refresh()
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val circles = repository.fetchFriendshipCharacter()
                val contacts = repository.fetchFriendShipList()
                val badgeCounts = fetchBadgeCounts()
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
                    newFriendCount = badgeCounts.first,
                    newGroupInviteCount = badgeCounts.second
                )
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                t.printStackTrace()
                eLogD("SocialViewBroadModel", "错误:${t.message}")
                _uiState.value = _uiState.value.copy(isLoading = false, error = t.message)
            }
        }
    }

    /** 仅刷新角标，避免 userInfo 触发的全量刷新导致列表/角标闪烁。 */
    fun refreshBadgesOnly() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            try {
                val badgeCounts = fetchBadgeCounts()
                _uiState.value = _uiState.value.copy(
                    newFriendCount = badgeCounts.first,
                    newGroupInviteCount = badgeCounts.second
                )
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                eLogD("SocialViewBroadModel", "角标刷新失败:${t.message}")
            }
        }
    }

    private suspend fun fetchBadgeCounts(): Pair<Int, Int> {
        val previous = _uiState.value
        val newFriendCount = runCatching { repository.fetchNewFriendCount() }
            .getOrElse { previous.newFriendCount }
        val newGroupInviteCount = runCatching { repository.fetchNewGroupInviteCount() }
            .getOrElse { previous.newGroupInviteCount }
        persistAndNotifyBadges(newFriendCount, newGroupInviteCount)
        return newFriendCount to newGroupInviteCount
    }

    private fun persistAndNotifyBadges(newFriendCount: Int, newGroupInviteCount: Int) {
        MMKVBroadUtils.saveContactBadgeCounts(
            newFriendCount.toLong(),
            newGroupInviteCount.toLong()
        )
        val total = MMKVBroadUtils.getContactBadgeTotal()
        if (lastPostedBadgeTotal != total) {
            lastPostedBadgeTotal = total
            EventBus.getDefault().post(
                UnreadCountBroadEvent(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    total.toString()
                )
            )
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
        val name = c.remarkName?.takeIf { it.isNotBlank() } ?: (c.nickName ?: c.userId)
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
