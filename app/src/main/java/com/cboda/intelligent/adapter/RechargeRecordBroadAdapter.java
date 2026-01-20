package com.cboda.intelligent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.baselibrary.bean.DataListBroadBean;
import com.cboda.intelligent.utils.GlideBroadUtil;

import java.util.List;

import cc.shinichi.library.ImagePreview;

/**
 * @author:李迪迦
 * @date: 2024/9/21
 * @Type:
 */
public class RechargeRecordBroadAdapter extends RecyclerView.Adapter<RechargeRecordBroadAdapter.MyHolder> {
    private Context context;
    private List<DataListBroadBean> list;
    public RechargeRecordBroadAdapter(Context context, List<DataListBroadBean> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public RechargeRecordBroadAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recharge_broad_record, parent, false);
        return new RechargeRecordBroadAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(RechargeRecordBroadAdapter.MyHolder holder, final int position) {
        holder.tvTitle.setText(list.get(position).title);
        holder.tvTime.setText(list.get(position).time);
        holder.tvShoukuanUser.setText(list.get(position).sname);
        holder.tvShoukuanZhanghu.setText(list.get(position).sno);
        holder.tvPrice.setText(context.getResources().getString(R.string.人民币)+list.get(position).money);
        GlideBroadUtil.setImag(context,list.get(position).pingzheng,holder.imPingzheng);
        if (list.get(position).isChecked){
            holder.llZhankai.setVisibility(View.VISIBLE);
        }else {
            holder.llZhankai.setVisibility(View.GONE);
        }
        holder.imPingzheng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePreview
                        .getInstance()
                        .setContext(context)
                        .setIndex(0)
                        .setImage(list.get(position).pingzheng)
                        .start();
            }
        });
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
        LinearLayout llButton;
        LinearLayout llZhankai;
        TextView tvTitle;
        TextView tvTime;
        TextView tvPrice;
        TextView tvShoukuanUser;
        TextView tvShoukuanZhanghu;
        ImageView imPingzheng;
        public MyHolder(View itemView) {
            super(itemView);
            llButton = itemView.findViewById(R.id.llButton);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvShoukuanUser = itemView.findViewById(R.id.tvShoukuanUser);
            imPingzheng = itemView.findViewById(R.id.imPingzheng);
            tvShoukuanZhanghu = itemView.findViewById(R.id.tvShoukuanZhanghu);
            llZhankai = itemView.findViewById(R.id.llZhankai);
        }
    }
    private RechargeRecordBroadAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(RechargeRecordBroadAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position);
    }
}
