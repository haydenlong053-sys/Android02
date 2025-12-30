package com.lxkj.aiim.lj.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lxkj.aiim.R;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.lj.utils.StringUtil;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/21
 * @Type:
 */
public class BillAdapter extends RecyclerView.Adapter<BillAdapter.MyHolder> {
    private Context context;
    private List<DataListBean> list;
    public BillAdapter(Context context, List<DataListBean> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public BillAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bill, parent, false);
        return new BillAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(BillAdapter.MyHolder holder, final int position) {
        holder.tvTime.setText(list.get(position).time);
        holder.tvTitle.setText(list.get(position).title);
        if (StringUtil.isEmpty(list.get(position).info)){
            holder.tvTuihuan.setVisibility(View.GONE);
        }else {
            holder.tvTuihuan.setVisibility(View.VISIBLE);
            holder.tvTuihuan.setText(list.get(position).info);
        }
        if (list.get(position).state.equals("1")){
            holder.tvPrice.setText("+"+list.get(position).money+context.getString(R.string.yuanqian));
//            holder.tvPrice.setTextColor(Color.parseColor("#333333"));
        }else {
            holder.tvPrice.setText("-"+list.get(position).money+context.getString(R.string.yuanqian));
//            holder.tvPrice.setTextColor(Color.parseColor("#FF5145"));
        }
        switch (list.get(position).type){
            case "1":
                holder.imIcon.setImageResource(R.mipmap.ic_bill_hongbao);
                break;
            case "2":
                holder.imIcon.setImageResource(R.mipmap.ic_bill_zhuanzhang);
                break;
            case "3":
                holder.imIcon.setImageResource(R.mipmap.ic_bill_chongzhi);
                break;
            case "4":
                holder.imIcon.setImageResource(R.mipmap.ic_bill_qiandao);
            case "5":
                holder.imIcon.setImageResource(R.mipmap.ic_bill_meeting);
                break;
            case "6":
                holder.imIcon.setImageResource(R.mipmap.ic_bill_gift);
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
        ImageView imIcon;
        TextView tvTitle;
        TextView tvPrice;
        TextView tvTime;
        TextView tvTuihuan;
        public MyHolder(View itemView) {
            super(itemView);
            imIcon = itemView.findViewById(R.id.imIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTuihuan = itemView.findViewById(R.id.tvTuihuan);
        }
    }
    private BillAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(BillAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position);
    }
}
