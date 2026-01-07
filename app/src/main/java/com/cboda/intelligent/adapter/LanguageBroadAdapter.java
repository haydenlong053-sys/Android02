package com.cboda.intelligent.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.bean.DataListBroadBean;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/20
 * @Type:
 */
public class LanguageBroadAdapter extends RecyclerView.Adapter<LanguageBroadAdapter.MyHolder> {
    private Context context;
    private List<DataListBroadBean> list;
    public LanguageBroadAdapter(Context context, List<DataListBroadBean> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public LanguageBroadAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_language, parent, false);
        return new LanguageBroadAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(LanguageBroadAdapter.MyHolder holder, final int position) {
        holder.tvTitle.setText(list.get(position).name);
        if (list.get(position).isChecked){
            holder.imCheck.setVisibility(View.VISIBLE);
            holder.tvTitle.setTextColor(context.getResources().getColor(R.color.color_language_select));
        }else {
            holder.imCheck.setVisibility(View.GONE);
            holder.tvTitle.setTextColor(Color.parseColor("#666666"));
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
        ImageView imCheck;
        TextView tvTitle;
        public MyHolder(View itemView) {
            super(itemView);
            imCheck = itemView.findViewById(R.id.imCheck);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
    private LanguageBroadAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(LanguageBroadAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position);
    }
}
