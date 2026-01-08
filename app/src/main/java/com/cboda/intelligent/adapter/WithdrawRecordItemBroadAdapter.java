package com.cboda.intelligent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.bean.DataListBroadBean;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/26
 * @Type:
 */
public class WithdrawRecordItemBroadAdapter extends RecyclerView.Adapter<WithdrawRecordItemBroadAdapter.MyHolder> {
    private Context context;
    private List<DataListBroadBean> list;
    public WithdrawRecordItemBroadAdapter(Context context, List<DataListBroadBean> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public WithdrawRecordItemBroadAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_withdraw_record_broad_item, parent, false);
        return new WithdrawRecordItemBroadAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(WithdrawRecordItemBroadAdapter.MyHolder holder, final int position) {
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
    private WithdrawRecordItemBroadAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(WithdrawRecordItemBroadAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position);
    }
}
