package com.lxkj.aiim.lj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lxkj.aiim.R;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.utils.StringUtils;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/21
 * @Type:
 */
public class SignInAdapter extends RecyclerView.Adapter<SignInAdapter.MyHolder> {
   private Context context;
   private List<DataListBean> list;
   public SignInAdapter(Context context, List<DataListBean> list) {
      this.context = context;
      this.list = list;
   }
   @Override
   public SignInAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(context).inflate(R.layout.item_signin, parent, false);
      return new SignInAdapter.MyHolder(view);
   }

   @Override
   public void onBindViewHolder(SignInAdapter.MyHolder holder, final int position) {
      holder.tvTitle.setText(list.get(position).title);
      holder.tvTime.setText(list.get(position).time);
      holder.tvMoney.setText("+"+ StringUtils.removeDecimalPart(list.get(position).money));
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
      TextView tvTime;
      TextView tvMoney;
      public MyHolder(View itemView) {
         super(itemView);
         tvTitle = itemView.findViewById(R.id.tvTitle);
         tvTime = itemView.findViewById(R.id.tvTime);
         tvMoney = itemView.findViewById(R.id.tvMoney);
      }
   }
   private SignInAdapter.OnItemClickListener onItemClickListener;

   public void setOnItemClickListener(SignInAdapter.OnItemClickListener onItemClickListener) {
      this.onItemClickListener = onItemClickListener;
   }

   public interface OnItemClickListener {
      void OnItemClickListener(int position);
   }
}