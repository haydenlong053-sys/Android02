package com.cboda.intelligent.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.bean.CircleCommListBroadBean;
import com.cboda.intelligent.utils.GlideBroadUtil;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/20
 * @Type:
 */
public class HuiFuBroadAdapter extends RecyclerView.Adapter<HuiFuBroadAdapter.MyHolder> {
   private Context context;
   private List<CircleCommListBroadBean> list;
   public HuiFuBroadAdapter(Context context, List<CircleCommListBroadBean> list) {
      this.context = context;
      this.list = list;
   }
   @Override
   public HuiFuBroadAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(context).inflate(R.layout.item_huifu, parent, false);
      return new HuiFuBroadAdapter.MyHolder(view);
   }

   @Override
   public void onBindViewHolder(HuiFuBroadAdapter.MyHolder holder, final int position) {
      GlideBroadUtil.setHeadImag(context,list.get(position).icon,holder.riIcon);
      holder.tvName.setText(list.get(position).name);
      holder.tvTime.setText(list.get(position).time);
      if (StringBroadUtil.isEmpty(list.get(position).huifu)){
         holder.tvContent.setText(list.get(position).info);
      }else {
         holder.tvContent.setText(Html.fromHtml("<font color=#B6B6B6><b>回复</b></font><font color=#7A7BF8><b>"+list.get(position).huifu+"</b></font><font color=#B6B6B6><b>：</b></font><font color=#333333><b>"+list.get(position).info+"</b></font>"));
      }
      if (position == list.size()-1){
         holder.vlLine.setVisibility(View.INVISIBLE);
      }else {
         holder.vlLine.setVisibility(View.VISIBLE);
      }
      holder.itemView.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            onItemClickListener.OnItemClickListener(position,"回复");
         }
      });
      holder.riIcon.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            onItemClickListener.OnItemClickListener(position,"主页");
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
      View vlLine;
      RoundedImageView riIcon;
      TextView tvName;
      TextView tvTime;
      TextView tvContent;
      public MyHolder(View itemView) {
         super(itemView);
         vlLine = itemView.findViewById(R.id.vlLine);
         riIcon = itemView.findViewById(R.id.riIcon);
         tvName = itemView.findViewById(R.id.tvName);
         tvTime = itemView.findViewById(R.id.tvTime);
         tvContent = itemView.findViewById(R.id.tvContent);
      }
   }
   private HuiFuBroadAdapter.OnItemClickListener onItemClickListener;

   public void setOnItemClickListener(HuiFuBroadAdapter.OnItemClickListener onItemClickListener) {
      this.onItemClickListener = onItemClickListener;
   }

   public interface OnItemClickListener {
      void OnItemClickListener(int position,String content);
   }
}
