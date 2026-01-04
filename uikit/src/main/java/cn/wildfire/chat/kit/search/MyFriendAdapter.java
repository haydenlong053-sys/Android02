package cn.wildfire.chat.kit.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.baselibrary.bean.GlobalFriendListBroadBean;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.search.viewHolder.FriendViewHolder;

public class MyFriendAdapter extends RecyclerView.Adapter<FriendViewHolder> {
    private List<GlobalFriendListBroadBean> itemList = new ArrayList<>();
    private OnItemClickListener listener;
    private Context context;
    private String keyword = "";

    // 点击监听接口
    public interface OnItemClickListener {
        void onItemClick(int position, GlobalFriendListBroadBean bean);
    }

    // 构造方法
    public MyFriendAdapter(List<GlobalFriendListBroadBean> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    // 设置点击监听器
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载布局并创建ViewHolder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item_contact2, parent, false);

        return new FriendViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        // 获取当前位置的数据
        GlobalFriendListBroadBean currentItem = itemList.get(position);
//        Log.d("zhanglong","pos==="+position+"----size="+itemList.size());
        // 将数据绑定到ViewHolder的视图上
        if (position == 0 && position + 1 == itemList.size()) {
            //只有一条时
            holder.contactLinearLayout.setBackgroundResource(R.drawable.bg_rect_white_16dp);
        } else if (position == 0) {
            //第一条
            holder.contactLinearLayout.setBackgroundResource(R.drawable.bg_rect_white_only_top_16dp);
        } else if (position + 1 == itemList.size()) {
            //最后一条
            holder.contactLinearLayout.setBackgroundResource(R.drawable.bg_rect_white_only_bottom_16dp);
        } else {
            holder.contactLinearLayout.setBackgroundResource(R.color.white);
        }

        if (position == 0) {
            holder.categoryTextView.setVisibility(View.VISIBLE);
            holder.categoryTextView.setText(context.getString(R.string.msg_linkman));
        } else {
            holder.categoryTextView.setVisibility(View.GONE);
        }
        if (itemList != null && position == itemList.size() - 1) {
            holder.dividerLine.setVisibility(View.GONE);
        } else {
            holder.dividerLine.setVisibility(View.VISIBLE);
        }
//        UserViewModel userViewModel = ViewModelProviders.of(fragment).get(UserViewModel.class);
//        nameTextView.setText(userViewModel.getUserDisplayName(userInfo.getUserInfo()));
        if (!TextUtils.isEmpty(currentItem.getRemarkName()) && !TextUtils.isEmpty(currentItem.getNickName())) {

            holder.nameTextView.setText(currentItem.getRemarkName() + "(" + currentItem.getNickName() + ")");
        } else if (!TextUtils.isEmpty(currentItem.getRemarkName())) {
            holder.nameTextView.setText(currentItem.getRemarkName());
        } else {
            holder.nameTextView.setText(currentItem.getNickName());
        }

        if (!TextUtils.isEmpty(currentItem.getLianghao())) {
            holder.descTextView.setVisibility(View.VISIBLE);
            holder.descTextView.setText("ID:" + currentItem.getLianghao());
        } else if (!TextUtils.isEmpty(currentItem.getIdno())) {
            holder.descTextView.setVisibility(View.VISIBLE);
            holder.descTextView.setText("ID:" + currentItem.getIdno());
        } else {
            holder.descTextView.setVisibility(View.GONE);
        }
        Glide.with(context).load(currentItem.getIcon()).placeholder(R.mipmap.avatar_def)
                .transforms(new CenterCrop(), new RoundedCorners(10))
                .into(holder.portraitImageView);

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position, currentItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        // 返回数据总数
        return itemList == null ? 0 : itemList.size();
    }

    // 更新数据的方法
    public void updateData(List<GlobalFriendListBroadBean> newList, String keyword) {
        itemList = newList;
        this.keyword = keyword;
        notifyDataSetChanged(); // 通知列表刷新
    }

    // 局部更新方法（更高效）
    public void addItem(GlobalFriendListBroadBean item) {
        itemList.add(item);
        notifyItemInserted(itemList.size() - 1);
    }

    public void removeItem(int position) {
        itemList.remove(position);
        notifyItemRemoved(position);
    }
}
