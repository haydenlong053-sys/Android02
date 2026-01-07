package com.cboda.intelligent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.cboda.intelligent.R;
import com.cboda.intelligent.bean.DataListBroadBean;

import java.lang.reflect.Type;
import java.util.List;

import cc.shinichi.library.ImagePreview;

/**
 * @author:李迪迦
 * @date: 2024/9/21
 * @Type:
 */
public class WithdrawRecordBroadAdapter extends RecyclerView.Adapter<WithdrawRecordBroadAdapter.MyHolder> {
    private Context context;
    private List<DataListBroadBean> list;
    private WithdrawRecordItemBroadAdapter withdrawRecordItemAdapter;
    public WithdrawRecordBroadAdapter(Context context, List<DataListBroadBean> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public WithdrawRecordBroadAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_withdraw_record, parent, false);
        return new WithdrawRecordBroadAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(WithdrawRecordBroadAdapter.MyHolder holder, final int position) {
        // 定义类型
        Type userListType = new TypeToken<List<DataListBroadBean>>() {}.getType();
        List<DataListBroadBean> listBeans = (List<DataListBroadBean>) new Gson().fromJson(list.get(position).info,userListType);

        withdrawRecordItemAdapter = new WithdrawRecordItemBroadAdapter(context, listBeans);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        holder.ryItem.setLayoutManager(linearLayoutManager);
        holder.ryItem.setAdapter(withdrawRecordItemAdapter);
        withdrawRecordItemAdapter.setOnItemClickListener(new WithdrawRecordItemBroadAdapter.OnItemClickListener() {
            @Override
            public void OnItemClickListener(int position) {
            }
        });
        holder.tvTitle.setText(list.get(position).title);
        holder.tvTime.setText(list.get(position).time);
        holder.tvPingzhengTitle.setText(list.get(position).tutitle);
        holder.tvPrice.setText(context.getResources().getString(R.string.人民币)+list.get(position).money);
        holder.imPingzheng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePreview
                        .getInstance()
                        .setContext(context)
                        .setIndex(0)
                        .setImage(list.get(position).tu)
                        .start();
            }
        });
        if (list.get(position).isChecked){
            holder.llZhankai.setVisibility(View.VISIBLE);
        }else {
            holder.llZhankai.setVisibility(View.GONE);
        }
        holder.llButton.setOnClickListener(new View.OnClickListener() {
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
        LinearLayout llButton;
        LinearLayout llZhankai;
        TextView tvTitle;
        TextView tvTime;
        TextView tvPrice;
        TextView tvPingzhengTitle;
        ImageView imPingzheng;
        RecyclerView ryItem;
        public MyHolder(View itemView) {
            super(itemView);
            llButton = itemView.findViewById(R.id.llButton);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvPingzhengTitle = itemView.findViewById(R.id.tvPingzhengTitle);
            imPingzheng = itemView.findViewById(R.id.imPingzheng);
            ryItem = itemView.findViewById(R.id.ryItem);
            llZhankai = itemView.findViewById(R.id.llZhankai);
        }
    }
    private WithdrawRecordBroadAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(WithdrawRecordBroadAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position);
    }
}
