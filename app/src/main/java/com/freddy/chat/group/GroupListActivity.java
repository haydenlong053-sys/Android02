package com.freddy.chat.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.freddy.im.http.ImGroupHttpExample;
import com.freddy.im.http.model.im.GroupListResp;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.listener.OnHttpImListener;

import java.util.List;

/** 我的群聊列表（§3.2）。 */
public class GroupListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private GroupListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        setTitle(R.string.group_list_title);

        recyclerView = findViewById(R.id.rv_groups);
        progressBar = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tv_empty);

        adapter = new GroupListAdapter();
        adapter.setOnGroupClickListener(group -> {
            Intent intent = new Intent(this, GroupInfoActivity.class);
            GroupIntent.put(intent, group.getId(), group);
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadGroups();
    }

    private void loadGroups() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        ImGroupHttpExample.requestGetGroupList(0, new OnHttpImListener<GroupListResp>() {
            @Override
            public void onHttpImSuccess(@NonNull GroupListResp result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    List<GroupVo> list = result.getGroupList();
                    boolean empty = list == null || list.isEmpty();
                    tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
                    adapter.setItems(list);
                });
            }
        }, () -> runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            Toast.makeText(this, R.string.group_list_load_failed, Toast.LENGTH_SHORT).show();
        }));
    }
}
