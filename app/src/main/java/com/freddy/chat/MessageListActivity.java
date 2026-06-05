package com.freddy.chat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.freddy.im.http.model.im.SessionItem;
import com.cboda.intelligent.R;

/**
 * 消息列表容器 Activity，内部承载 {@link MessageListFragment}。
 */
public class MessageListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

        if (savedInstanceState != null) {
            return;
        }
        SessionItem sessionItem = SessionItemIntent.getSessionItem(getIntent());
        if (sessionItem == null) {
            finish();
            return;
        }
        attachMessageListFragment(sessionItem);
    }

    private void attachMessageListFragment(@NonNull SessionItem sessionItem) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, MessageListFragment.newInstance(sessionItem))
                .commit();
    }
}
