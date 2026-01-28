package com.cboda.intelligent.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.select.picture.entity.DataListBroadBean;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/22
 * @Type:
 */
@Deprecated
public class IssueBroadAdapter2 extends RecyclerView.Adapter<IssueBroadAdapter2.MyHolder> {
    private Context context;
    private List<DataListBroadBean> list;
    public IssueBroadAdapter2(Context context, List<DataListBroadBean> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public IssueBroadAdapter2.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_broad_issue2, parent, false);
        return new IssueBroadAdapter2.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(IssueBroadAdapter2.MyHolder holder, final int position) {
//        holder.setIsRecyclable(false);
        holder.tvTitle.setText(list.get(position).title);
        holder.tvDes.setText(list.get(position).context);
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
        TextView tvDes;
        public MyHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDes = itemView.findViewById(R.id.tvDes);
        }
    }
    private IssueBroadAdapter2.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(IssueBroadAdapter2.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position);
    }
}
