package com.cboda.intelligent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.select.picture.entity.DataListBroadBean;
import com.cboda.intelligent.utils.GlideBroadUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/22
 * @Type:
 */
public class EmjoyBroadAdapter extends RecyclerView.Adapter<EmjoyBroadAdapter.MyHolder> {
   private Context context;
   private List<DataListBroadBean> list;
   public EmjoyBroadAdapter(Context context, List<DataListBroadBean> list) {
      this.context = context;
      this.list = list;
   }
   @Override
   public EmjoyBroadAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(context).inflate(R.layout.item_em_broad_joy, parent, false);
      return new EmjoyBroadAdapter.MyHolder(view);
   }

   @Override
   public void onBindViewHolder(EmjoyBroadAdapter.MyHolder holder, final int position) {
      if (list.get(position).isChecked){
         holder.imCheck.setImageResource(R.mipmap.btn_checkbox_broad_press);
      }else {
         holder.imCheck.setImageResource(R.mipmap.btn_checkbox_broad_normal);
      }
      if (list.get(position).isShowChecked){
         holder.imCheck.setVisibility(View.VISIBLE);
      }else {
         holder.imCheck.setVisibility(View.GONE);
      }
      GlideBroadUtil.setImag(context,list.get(position).emoji,holder.riIcon);
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
      RoundedImageView riIcon;
      ImageView imCheck;
      public MyHolder(View itemView) {
         super(itemView);
         riIcon = itemView.findViewById(R.id.riIcon);
         imCheck = itemView.findViewById(R.id.imCheck);
      }
   }
   private EmjoyBroadAdapter.OnItemClickListener onItemClickListener;

   public void setOnItemClickListener(EmjoyBroadAdapter.OnItemClickListener onItemClickListener) {
      this.onItemClickListener = onItemClickListener;
   }

   public interface OnItemClickListener {
      void OnItemClickListener(int position);
   }
}