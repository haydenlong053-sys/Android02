package com.cboda.intelligent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.bean.DataListBroadBean;
import com.cboda.intelligent.utils.StringBroadUtils;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/21
 * @Type:
 */
public class SignInBroadAdapter extends RecyclerView.Adapter<SignInBroadAdapter.MyHolder> {
   private Context context;
   private List<DataListBroadBean> list;
   public SignInBroadAdapter(Context context, List<DataListBroadBean> list) {
      this.context = context;
      this.list = list;
   }
   @Override
   public SignInBroadAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(context).inflate(R.layout.item_sign_broad_in, parent, false);
      return new SignInBroadAdapter.MyHolder(view);
   }

   @Override
   public void onBindViewHolder(SignInBroadAdapter.MyHolder holder, final int position) {
//      holder.tvTitle.setText(list.get(position).title);
      holder.tvTime.setText(list.get(position).time);
      holder.tvMoney.setText("+"+ StringBroadUtils.removeDecimalPart(list.get(position).money));
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
   private SignInBroadAdapter.OnItemClickListener onItemClickListener;

   public void setOnItemClickListener(SignInBroadAdapter.OnItemClickListener onItemClickListener) {
      this.onItemClickListener = onItemClickListener;
   }

   public interface OnItemClickListener {
      void OnItemClickListener(int position);
   }
}