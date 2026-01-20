package com.cboda.intelligent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.cboda.intelligent.R;
import com.cboda.baselibrary.bean.DataListBroadBean;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/8/5
 * @Type:
 */
public class BrandBroadAdapter extends RecyclerView.Adapter<BrandBroadAdapter.MyHolder> {
    private Context context;
    private List<DataListBroadBean> list;
    public BrandBroadAdapter(Context context, List<DataListBroadBean> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_broad_brand, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, final int position) {
        holder.tvName.setText(list.get(position).contry);
        holder.tvNo.setText(list.get(position).no);

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
        TextView tvLatter;
        TextView tvName;
        TextView tvNo;
        public MyHolder(View itemView) {
            super(itemView);
            tvLatter = itemView.findViewById(R.id.tvLatter);
            tvName = itemView.findViewById(R.id.tvName);
            tvNo = itemView.findViewById(R.id.tvNo);
        }
    }
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position);
    }
}
