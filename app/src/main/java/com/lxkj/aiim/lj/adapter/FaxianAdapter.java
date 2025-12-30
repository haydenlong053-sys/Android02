package com.lxkj.aiim.lj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lxkj.aiim.R;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.lj.utils.GlideUtil;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/24
 * @Type:
 */
public class FaxianAdapter extends RecyclerView.Adapter<FaxianAdapter.MyHolder> {
    private Context context;
    private List<DataListBean> list;
    public FaxianAdapter(Context context, List<DataListBean> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public FaxianAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_faxian, parent, false);
        return new FaxianAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(FaxianAdapter.MyHolder holder, final int position) {
        GlideUtil.setImag(context, list.get(position).icon,holder.imIcon);
        holder.tvTitle.setText(list.get(position).title);

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
        ImageView imIcon;
        TextView tvTitle;
        public MyHolder(View itemView) {
            super(itemView);
            llItem = itemView.findViewById(R.id.llItem);
            imIcon = itemView.findViewById(R.id.imIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
    private FaxianAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(FaxianAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position);
    }
}

