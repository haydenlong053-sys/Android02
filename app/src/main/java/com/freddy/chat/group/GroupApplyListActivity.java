package com.freddy.chat.group;

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
import com.freddy.im.http.model.im.GroupApplyInfoRes;
import com.freddy.im.listener.OnHttpImListener;

import java.util.List;

/** 入群申请审批（§3.18 / §3.16）。 */
public class GroupApplyListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private GroupApplyAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_apply_list);
        setTitle(R.string.group_apply_title);

        recyclerView = findViewById(R.id.rv_apply);
        progressBar = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tv_empty);

        adapter = new GroupApplyAdapter();
        adapter.setOnApplyActionListener(new GroupApplyAdapter.OnApplyActionListener() {
            @Override
            public void onApprove(@NonNull GroupApplyInfoRes item) {
                approve(item, "1");
            }

            @Override
            public void onReject(@NonNull GroupApplyInfoRes item) {
                approve(item, "2");
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadApplies();
    }

    private void loadApplies() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        ImGroupHttpExample.requestSearchGroupAsst(new OnHttpImListener<List<GroupApplyInfoRes>>() {
            @Override
            public void onHttpImSuccess(@NonNull List<GroupApplyInfoRes> result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    boolean empty = result.isEmpty();
                    tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                    adapter.setItems(result);
                });
            }
        }, () -> runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            Toast.makeText(this, R.string.group_apply_load_failed, Toast.LENGTH_SHORT).show();
        }));
    }

    private void approve(@NonNull GroupApplyInfoRes item, @NonNull String status) {
        progressBar.setVisibility(View.VISIBLE);
        ImGroupHttpExample.requestApproveInGroup(item.getApplyId(), status, new OnHttpImListener<Object>() {
            @Override
            public void onHttpImSuccess(@NonNull Object result) {
                runOnUiThread(() -> {
                    Toast.makeText(GroupApplyListActivity.this,
                            R.string.group_apply_action_success, Toast.LENGTH_SHORT).show();
                    loadApplies();
                });
            }
        }, () -> runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, R.string.group_apply_action_failed, Toast.LENGTH_SHORT).show();
        }));
    }
}
