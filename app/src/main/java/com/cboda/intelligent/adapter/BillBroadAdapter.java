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
import com.cboda.intelligent.utils.StringBroadUtil;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/21
 * @Type:
 */
public class BillBroadAdapter extends RecyclerView.Adapter<BillBroadAdapter.MyHolder> {
    private Context context;
    private List<DataListBroadBean> list;
    public BillBroadAdapter(Context context, List<DataListBroadBean> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public BillBroadAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_broad_bill, parent, false);
        return new BillBroadAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(BillBroadAdapter.MyHolder holder, final int position) {
        holder.tvTime.setText(list.get(position).time);
        holder.tvTitle.setText(list.get(position).title);
        if (StringBroadUtil.isEmpty(list.get(position).info)){
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
                holder.imIcon.setImageResource(R.mipmap.ic_bill_broad_hongbao);
                break;
            case "2":
                holder.imIcon.setImageResource(R.mipmap.ic_bill_broad_zhuanzhang);
                break;
            case "3":
                holder.imIcon.setImageResource(R.mipmap.ic_bill_broad_chongzhi);
                break;
            case "4":
                holder.imIcon.setImageResource(R.mipmap.ic_bill_broad_qiandao);
            case "5":
                holder.imIcon.setImageResource(R.mipmap.ic_bill_broad_meeting);
                break;
            case "6":
                holder.imIcon.setImageResource(R.mipmap.ic_bill_broad_gift);
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
    private BillBroadAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(BillBroadAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position);
    }
}
