package com.lxkj.aiim.lj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lxkj.aiim.R;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/20
 * @Type:
 */
public class CircelMessageAdapter extends RecyclerView.Adapter<CircelMessageAdapter.MyHolder> {
    private Context context;
    private List<DataListBean> list;
    public CircelMessageAdapter(Context context, List<DataListBean> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public CircelMessageAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_circel_message, parent, false);
        return new CircelMessageAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(CircelMessageAdapter.MyHolder holder, final int position) {
        if (!StringUtil.isEmpty(list.get(position).dyImg)){
            holder.riThumb.setVisibility(View.VISIBLE);
            GlideUtil.setImag(context,list.get(position).dyImg,holder.riThumb);
            holder.tvWenan.setVisibility(View.GONE);
        }else {
            holder.riThumb.setVisibility(View.GONE);
            holder.tvWenan.setVisibility(View.VISIBLE);
            holder.tvWenan.setText(list.get(position).dyInfo);
        }
        GlideUtil.setHeadImag(context,list.get(position).icon,holder.riIcon);
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
    private CircelMessageAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(CircelMessageAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position,String content);
    }
}
