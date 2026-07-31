package com.freddy.chat;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cboda.intelligent.R;

/**
 * 会话列表容器 Activity，内部承载 {@link ConversationListFragment}。
 * <p>
 * 宿主也可在自己的 Activity 中直接 add {@link ConversationListFragment#newInstance()}。
 */
public class ConversationListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        setTitle(com.cboda.baselibrary.R.string.conversation_list_title);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, ConversationListFragment.newInstance())
                    .commit();
        }
    }
}
