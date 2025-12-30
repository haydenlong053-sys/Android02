package com.lxkj.aiim.lj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lxkj.aiim.R;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/22
 * @Type:
 */
public class MyCollectAdapter extends RecyclerView.Adapter<MyCollectAdapter.MyHolder> {
    private Context context;
    private List<DataListBean> list;
    public MyCollectAdapter(Context context, List<DataListBean> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public MyCollectAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_collect, parent, false);
        return new MyCollectAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyCollectAdapter.MyHolder holder, final int position) {
        holder.tvContent.setVisibility(View.GONE);
        holder.rlVideo.setVisibility(View.GONE);
        holder.llVoice.setVisibility(View.GONE);
        GlideUtil.setHeadImag(context,list.get(position).icon,holder.riIcon);
        holder.tvName.setText(list.get(position).name);
        holder.tvTime.setText(list.get(position).time);
        switch (list.get(position).type){
            case "1"://文字
                holder.tvContent.setVisibility(View.VISIBLE);
                holder.tvContent.setText(list.get(position).info);
                break;
            case "2"://图片
                holder.rlVideo.setVisibility(View.VISIBLE);
                holder.imVideo.setVisibility(View.GONE);
                GlideUtil.setImag(context,list.get(position).info,holder.riImage);
                break;
            case "3"://视频
                holder.rlVideo.setVisibility(View.VISIBLE);
                holder.imVideo.setVisibility(View.VISIBLE);
                GlideUtil.setImag(context,list.get(position).info,holder.riImage);
                break;
            case "4"://音频
                holder.llVoice.setVisibility(View.VISIBLE);
                holder.tvVoiceTime.setText(list.get(position).miao+"s");
                break;
            case "5"://位置
                holder.tvContent.setVisibility(View.VISIBLE);
                holder.tvContent.setText(list.get(position).info);
                break;
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
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
        TextView tvContent;
        TextView tvVoiceTime;
        TextView tvName;
        TextView tvTime;
        RelativeLayout rlVideo;
        RoundedImageView riImage;
        RoundedImageView riIcon;
        ImageView imVideo;
        LinearLayout llVoice;
        public MyHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvContent);
            rlVideo = itemView.findViewById(R.id.rlVideo);
            riImage = itemView.findViewById(R.id.riImage);
            imVideo = itemView.findViewById(R.id.imVideo);
            llVoice = itemView.findViewById(R.id.llVoice);
            tvVoiceTime = itemView.findViewById(R.id.tvVoiceTime);
            riIcon = itemView.findViewById(R.id.riIcon);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
    private MyCollectAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(MyCollectAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position);
    }
}
