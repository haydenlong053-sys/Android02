package com.lxkj.aiim.lj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.lxkj.aiim.R;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/22
 * @Type:
 */
public class EmjoyAdapter extends RecyclerView.Adapter<EmjoyAdapter.MyHolder> {
   private Context context;
   private List<DataListBean> list;
   public EmjoyAdapter(Context context, List<DataListBean> list) {
      this.context = context;
      this.list = list;
   }
   @Override
   public EmjoyAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(context).inflate(R.layout.item_emjoy, parent, false);
      return new EmjoyAdapter.MyHolder(view);
   }

   @Override
   public void onBindViewHolder(EmjoyAdapter.MyHolder holder, final int position) {
      if (list.get(position).isChecked){
         holder.imCheck.setImageResource(R.mipmap.btn_checkbox_press);
      }else {
         holder.imCheck.setImageResource(R.mipmap.btn_checkbox_normal);
      }
      if (list.get(position).isShowChecked){
         holder.imCheck.setVisibility(View.VISIBLE);
      }else {
         holder.imCheck.setVisibility(View.GONE);
      }
      GlideUtil.setImag(context,list.get(position).emoji,holder.riIcon);
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
   private EmjoyAdapter.OnItemClickListener onItemClickListener;

   public void setOnItemClickListener(EmjoyAdapter.OnItemClickListener onItemClickListener) {
      this.onItemClickListener = onItemClickListener;
   }

   public interface OnItemClickListener {
      void OnItemClickListener(int position);
   }
}