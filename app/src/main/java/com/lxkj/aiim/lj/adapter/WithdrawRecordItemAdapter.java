package com.lxkj.aiim.lj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.lxkj.aiim.lj.bean.DataListBean;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/26
 * @Type:
 */
public class WithdrawRecordItemAdapter extends RecyclerView.Adapter<WithdrawRecordItemAdapter.MyHolder> {
    private Context context;
    private List<DataListBean> list;
    public WithdrawRecordItemAdapter(Context context, List<DataListBean> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public WithdrawRecordItemAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_withdraw_record_item, parent, false);
        return new WithdrawRecordItemAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(WithdrawRecordItemAdapter.MyHolder holder, final int position) {
        holder.tvTitle.setText(list.get(position).title);
        holder.tvContent.setText(list.get(position).context);
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
        TextView tvTitle;
        TextView tvContent;
        public MyHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
    private WithdrawRecordItemAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(WithdrawRecordItemAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position);
    }
}
