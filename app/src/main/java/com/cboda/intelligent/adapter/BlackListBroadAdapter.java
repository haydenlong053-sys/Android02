package com.cboda.intelligent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.select.picture.entity.DataListBroadBean;
import com.cboda.intelligent.utils.GlideBroadUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/23
 * @Type:
 */
public class BlackListBroadAdapter extends RecyclerView.Adapter<BlackListBroadAdapter.MyHolder> {
    private Context context;
    private List<DataListBroadBean> list;
    public BlackListBroadAdapter(Context context, List<DataListBroadBean> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public BlackListBroadAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_black_broad_list, parent, false);
        return new BlackListBroadAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(BlackListBroadAdapter.MyHolder holder, final int position) {
        GlideBroadUtil.setHeadImag(context,list.get(position).icon,holder.riIcon);
        holder.tvName.setText(list.get(position).name);
        holder.tvQuxiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.OnItemClickListener(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (list == null) {
            return 0;
        } else {
            return list.size();
        }
    }


    public class MyHolder extends RecyclerView.ViewHolder {
        RoundedImageView riIcon;
        TextView tvName;
        TextView tvQuxiao;
        public MyHolder(View itemView) {
            super(itemView);
            riIcon = itemView.findViewById(R.id.riIcon);
            tvName = itemView.findViewById(R.id.tvName);
            tvQuxiao = itemView.findViewById(R.id.tvQuxiao);
        }
    }
    private BlackListBroadAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(BlackListBroadAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position);
    }
}
