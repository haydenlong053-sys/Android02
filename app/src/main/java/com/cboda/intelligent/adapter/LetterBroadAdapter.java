package com.cboda.intelligent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.cboda.intelligent.R;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/8/5
 * @Type:
 */
public class LetterBroadAdapter extends RecyclerView.Adapter<LetterBroadAdapter.MyHolder> {
    private Context context;
    private List<String> list;
    public LetterBroadAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_broad_letter, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, final int position) {
        holder.tvTitle.setText(list.get(position));
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
        public MyHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
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