package com.freddy.chat.folder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.intelligent.R
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.viewmodel.ConversationParentViewModel
import com.freddy.chat.event.CEventCenter
import com.freddy.chat.event.Events
import com.freddy.chat.event.I_CEventListener
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.im.http.model.im.ConvFolderItem

/**
 * 首页「分组」Tab：可展开的会话分组列表。
 */
class ConversationFolderFragment : Fragment(), I_CEventListener {

    private val parentViewModel: ConversationParentViewModel by activityViewModels()
    private val expandedIds = HashSet<String>()
    private lateinit var adapter: ConversationFolderListAdapter
    private lateinit var tvTitle: TextView
    private lateinit var tvEmpty: TextView
    private var foldersCache: List<ConvFolderItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_conversation_folder, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvTitle = view.findViewById(R.id.tv_folder_title)
        tvEmpty = view.findViewById(R.id.tv_folder_empty)
        view.findViewById<View>(R.id.tv_folder_manage).setOnClickListener {
            ConvFolderManageActivity.start(requireContext())
        }
        val rv = view.findViewById<RecyclerView>(R.id.rv_folders)
        adapter = ConversationFolderListAdapter(
            currentUserId = IMSClientBootstrap.getInstance().currentUserId,
            onToggle = { folder ->
                val id = folder.id.orEmpty()
                if (id.isEmpty()) return@ConversationFolderListAdapter
                if (!expandedIds.add(id)) {
                    expandedIds.remove(id)
                }
                rebuildRows()
            },
            onMore = { folder ->
                ConvFolderMoreHelper.show(requireContext(), folder) {
                    parentViewModel.loadFolders()
                }
            },
            onSessionClick = { session ->
                JumpBroadUtils.startChatMsgActivity(session, requireContext())
            },
        )
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        parentViewModel.folders.observe(viewLifecycleOwner) { list ->
            foldersCache = list.orEmpty()
            rebuildRows()
        }
        parentViewModel.allSessions.observe(viewLifecycleOwner) {
            rebuildRows()
        }
        CEventCenter.registerEventListener(this, arrayOf(Events.CONV_FOLDERS_UPDATED))
    }

    override fun onDestroyView() {
        CEventCenter.unregisterEventListener(this, arrayOf(Events.CONV_FOLDERS_UPDATED))
        super.onDestroyView()
    }

    override fun onCEvent(topic: String?, msgCode: Int, resultCode: Int, obj: Any?) {
        if (Events.CONV_FOLDERS_UPDATED == topic) {
            parentViewModel.loadFolders()
        }
    }

    private fun rebuildRows() {
        if (!::adapter.isInitialized) return
        val folders = foldersCache
        tvTitle.text = getString(R.string.conv_folder_title, folders.size)
        tvEmpty.isVisible = folders.isEmpty()
        val rows = ArrayList<FolderListRow>()
        for (folder in folders) {
            val id = folder.id.orEmpty()
            val unread = parentViewModel.folderUnread(folder)
            val expanded = expandedIds.contains(id)
            rows.add(FolderListRow.Header(folder, unread, expanded))
            if (expanded) {
                for (session in parentViewModel.sessionsInFolder(folder)) {
                    rows.add(FolderListRow.Session(id, session))
                }
            }
        }
        adapter.submit(rows)
    }
}
