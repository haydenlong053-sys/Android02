package com.cboda.intelligent.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.bean.SvnBroadBean;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/23
 * @Type:
 */
public class VpnBroadAdapter extends RecyclerView.Adapter<VpnBroadAdapter.MyHolder> {
    private Context context;
    private List<SvnBroadBean.Vpn> list;
    public VpnBroadAdapter(Context context, List<SvnBroadBean.Vpn> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public VpnBroadAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_broad_vpn, parent, false);
        return new VpnBroadAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(VpnBroadAdapter.MyHolder holder, final int position) {
        holder.tvJiedian.setText(list.get(position).name);
//        holder.tvSudu.setText(list.get(position).port+"ms");
//        if (Integer.parseInt(list.get(position).port)<100){
//            holder.tvSudu.setTextColor(Color.parseColor("#54BD5F"));
//        }else if (Integer.parseInt(list.get(position).port)>=100&&Integer.parseInt(list.get(position).port)<300){
//            holder.tvSudu.setTextColor(Color.parseColor("#FCCA00"));
//        }else if (Integer.parseInt(list.get(position).port)>=300){
//            holder.tvSudu.setTextColor(Color.parseColor("#EE4C4A"));
//        }
        if (list.get(position).isCheck){
            holder.llItem.setBackgroundResource(R.drawable.bg_vpn_true);
            holder.tvJiedian.setTextColor(Color.parseColor("#5456F6"));
        }else {
            holder.llItem.setBackgroundResource(R.drawable.bg_rect_white_15dp);
            holder.tvJiedian.setTextColor(Color.parseColor("#01174F"));
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
        LinearLayout llItem;
        TextView tvJiedian;
        TextView tvSudu;
        public MyHolder(View itemView) {
            super(itemView);
            llItem = itemView.findViewById(R.id.llItem);
            tvJiedian = itemView.findViewById(R.id.tvJiedian);
            tvSudu = itemView.findViewById(R.id.tvSudu);
        }
    }
    private VpnBroadAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(VpnBroadAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position);
    }
}
