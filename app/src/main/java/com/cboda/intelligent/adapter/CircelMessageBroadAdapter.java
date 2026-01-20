package com.cboda.intelligent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.select.picture.entity.DataListBroadBean;
import com.cboda.intelligent.utils.GlideBroadUtil;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/20
 * @Type:
 */
public class CircelMessageBroadAdapter extends RecyclerView.Adapter<CircelMessageBroadAdapter.MyHolder> {
    private Context context;
    private List<DataListBroadBean> list;
    public CircelMessageBroadAdapter(Context context, List<DataListBroadBean> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public CircelMessageBroadAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_circle_broad_message, parent, false);
        return new CircelMessageBroadAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(CircelMessageBroadAdapter.MyHolder holder, final int position) {
        if (!StringBroadUtil.isEmpty(list.get(position).dyImg)){
            holder.riThumb.setVisibility(View.VISIBLE);
            GlideBroadUtil.setImag(context,list.get(position).dyImg,holder.riThumb);
            holder.tvWenan.setVisibility(View.GONE);
        }else {
            holder.riThumb.setVisibility(View.GONE);
            holder.tvWenan.setVisibility(View.VISIBLE);
            holder.tvWenan.setText(list.get(position).dyInfo);
        }
        GlideBroadUtil.setHeadImag(context,list.get(position).icon,holder.riIcon);
        holder.tvName.setText(list.get(position).name);
        holder.tvTime.setText(list.get(position).time);
        if (list.get(position).type.equals("1")){
            holder.imZan.setVisibility(View.VISIBLE);
            holder.tvContent.setVisibility(View.GONE);
        }else {
            holder.imZan.setVisibility(View.GONE);
            holder.tvContent.setVisibility(View.VISIBLE);
            holder.tvContent.setText(list.get(position).info);
        }
        holder.riIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.OnItemClickListener(position,"主页");
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.OnItemClickListener(position,"详情");
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
        RoundedImageView riThumb;
        RoundedImageView riIcon;
        TextView tvWenan;
        TextView tvName;
        TextView tvContent;
        TextView tvTime;
        ImageView imZan;
        public MyHolder(View itemView) {
            super(itemView);
            riThumb = itemView.findViewById(R.id.riThumb);
            tvWenan = itemView.findViewById(R.id.tvWenan);
            riIcon = itemView.findViewById(R.id.riIcon);
            imZan = itemView.findViewById(R.id.imZan);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
    private CircelMessageBroadAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(CircelMessageBroadAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position,String content);
    }
}
