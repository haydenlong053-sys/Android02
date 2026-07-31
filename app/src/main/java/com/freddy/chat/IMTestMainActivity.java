package com.freddy.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cboda.baselibrary.utils.HttpsBroadConfig;
import com.cboda.intelligent.R;
import com.freddy.chat.im.IMSClientBootstrap;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.IMSConfig;
import com.freddy.im.http.ImHttpConfig;
import com.freddy.im.http.ImMessageHttpExample;
import com.freddy.im.http.model.im.BaseMsgResponse;
import com.freddy.im.http.model.im.UserInfoItem;
import com.freddy.im.http.model.im.UserInfoListRes;
import com.freddy.im.listener.OnHttpImListener;
import com.freddy.im.signal.ImSignalProtos;

import java.util.ArrayList;
import java.util.List;

//仅测试IM登录使用
public class IMTestMainActivity extends AppCompatActivity {

    public static final String[] USER_IDS = {
            "00b0eb2fe9274dfab271f6b16dfad9db",
            "00b23d2725a0468190ab0b3f6ef0ff94",
            "00b2c08f52a64d5690f4d9f8a6f170f8",
            "00b4a68a74e3456c92516f81fa44774e",
            "00b5847dedc140cf98128c2f6d559d4a",
            "00b5a0b8920e4d3393035b50beda1ea0",
            "00b5a3d478c04650ab9dff92ece89d1e",
            "00b5fcc8c89745519e4fdfd1d9483347",
            "1791854966a9485c8483ac3cce077ff6",
    };

    String userId = "";
    String token = "";
    String hosts = HttpsBroadConfig.MY_IM_SOCKET_HOST_FAT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<String> labels = new ArrayList<>(USER_IDS.length);
        for (int i = 0; i < USER_IDS.length; i++) {
            labels.add("用户" + USER_IDS[i]);
        }
        ListView userList = findViewById(R.id.user_list);
        userList.setAdapter(new UserListAdapter(labels));
        userList.setOnItemClickListener((parent, v, position, id) -> {
            userId = USER_IDS[position];
            token = "token_" + userId;
        });
    }

    public void openConversationList(View view) {
        if (!TextUtils.isEmpty(userId)) {
            IMSClientBootstrap.getInstance().init(userId, token, hosts, IMSConfig.APP_STATUS_FOREGROUND);
            ImHttpConfig.setGlobalHeaders(token, userId, ImSignalProtos.ClientType.ANDROID_VALUE);
            loadCurrentUserAvatar(userId);
            Intent newIntent = new Intent(this, ConversationListActivity.class);
            startActivity(newIntent);
            finish();
        } else {
            Toast.makeText(this, com.cboda.baselibrary.R.string.main_select_user_hint, Toast.LENGTH_SHORT).show();
        }
    }

    /** 拉取用户列表，解析当前账号头像 URL 供聊天页展示。 */
    private void loadCurrentUserAvatar(String loginUserId) {
        ImMessageHttpExample.requestGetUserList(null, 1, 100,
                new OnHttpImListener<BaseMsgResponse<UserInfoListRes>>() {
                    @Override
                    public void onHttpImSuccess(@NonNull BaseMsgResponse<UserInfoListRes> result) {
                        if (!result.isSuccess() || result.getContent() == null) {
                            return;
                        }
                        List<UserInfoItem> list = result.getContent().getUserList();
                        if (list == null) {
                            return;
                        }
                        for (UserInfoItem item : list) {
                            if (item != null && loginUserId.equals(item.getUserId())) {
                                IMSClientBootstrap.getInstance()
                                        .setCurrentUserAvatarUrl(WeChatUiHelper.resolveAvatarUrl(item));
                                break;
                            }
                        }
                    }
                },
                null);
    }

    private static final class UserListAdapter extends BaseAdapter {

        private final List<String> labels;

        UserListAdapter(List<String> labels) {
            this.labels = labels;
        }

        @Override
        public int getCount() {
            return labels.size();
        }

        @Override
        public Object getItem(int position) {
            return labels.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_user_login, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String label = labels.get(position);
            holder.tvLabel.setText(label);
            com.freddy.chat.utils.AvatarViews.bind(
                    holder.avatar, null, label, USER_IDS[position]);
            return convertView;
        }

        static final class ViewHolder {
            final View avatar;
            final TextView tvLabel;

            ViewHolder(View root) {
                avatar = root.findViewById(R.id.avatar);
                tvLabel = root.findViewById(R.id.tv_user_label);
            }
        }
    }
}
