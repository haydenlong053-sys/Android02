package com.cboda.intelligent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.bean.DataListBroadBean;
import com.cboda.intelligent.utils.GlideBroadUtil;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/22
 * @Type:
 */
public class MyCircleBroadAdapter extends RecyclerView.Adapter<MyCircleBroadAdapter.MyHolder> {
   private Context context;
   private List<DataListBroadBean> list;
   public MyCircleBroadAdapter(Context context, List<DataListBroadBean> list) {
      this.context = context;
      this.list = list;
   }
   @Override
   public MyCircleBroadAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(context).inflate(R.layout.item_my_broad_circle, parent, false);
      return new MyCircleBroadAdapter.MyHolder(view);
   }

   @Override
   public void onBindViewHolder(MyCircleBroadAdapter.MyHolder holder, final int position) {
      String time[] = list.get(position).time.split(" ");
      String year[] = time[0].split("-");
      if (position == 0){
         holder.tvYear.setVisibility(View.VISIBLE);
      }else {
         String time1[] = list.get(position-1).time.split(" ");
         String year1[] = time1[0].split("-");
         if (year1[0].equals(year[0])){
            holder.tvYear.setVisibility(View.GONE);
         }else {
            holder.tvYear.setVisibility(View.VISIBLE);
         }
      }
      holder.tvYear.setText(year[0]+context.getResources().getString(R.string.年));
      holder.tvYue.setText(year[1]+context.getResources().getString(R.string.月));
      if (Integer.parseInt(year[2])<10){
         holder.tvDay.setText("0"+year[2]);
      }else {
         holder.tvDay.setText(year[2]);
      }

      if (list.get(position).type.equals("1")){
         if (list.get(position).imgs.size()>0){
            GlideBroadUtil.setImag(context,list.get(position).imgs.get(0),holder.imImage);
         }
         holder.imVideo.setVisibility(View.GONE);
      }else {
         holder.imVideo.setVisibility(View.VISIBLE);
         GlideBroadUtil.setImag(context,list.get(position).vimg,holder.imImage);
      }

      holder.tvContent.setText(list.get(position).info);
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
      TextView tvYear;
      TextView tvDay;
      TextView tvYue;
      TextView tvContent;
      ImageView imImage;
      ImageView imVideo;
      public MyHolder(View itemView) {
         super(itemView);
         tvYear = itemView.findViewById(R.id.tvYear);
         tvDay = itemView.findViewById(R.id.tvDay);
         tvYue = itemView.findViewById(R.id.tvYue);
         imImage = itemView.findViewById(R.id.imImage);
         tvContent = itemView.findViewById(R.id.tvContent);
         imVideo = itemView.findViewById(R.id.imVideo);
      }
   }
   private MyCircleBroadAdapter.OnItemClickListener onItemClickListener;

   public void setOnItemClickListener(MyCircleBroadAdapter.OnItemClickListener onItemClickListener) {
      this.onItemClickListener = onItemClickListener;
   }

   public interface OnItemClickListener {
      void OnItemClickListener(int position);
   }
}
