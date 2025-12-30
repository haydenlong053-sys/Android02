package com.lxkj.aiim.lj.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lxkj.aiim.R;
import com.lxkj.aiim.lj.bean.CircleCommListBean;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/20
 * @Type:
 */
public class HuiFuAdapter extends RecyclerView.Adapter<HuiFuAdapter.MyHolder> {
   private Context context;
   private List<CircleCommListBean> list;
   public HuiFuAdapter(Context context, List<CircleCommListBean> list) {
      this.context = context;
      this.list = list;
   }
   @Override
   public HuiFuAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(context).inflate(R.layout.item_huifu, parent, false);
      return new HuiFuAdapter.MyHolder(view);
   }

   @Override
   public void onBindViewHolder(HuiFuAdapter.MyHolder holder, final int position) {
      GlideUtil.setHeadImag(context,list.get(position).icon,holder.riIcon);
      holder.tvName.setText(list.get(position).name);
      holder.tvTime.setText(list.get(position).time);
      if (StringUtil.isEmpty(list.get(position).huifu)){
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
   private HuiFuAdapter.OnItemClickListener onItemClickListener;

   public void setOnItemClickListener(HuiFuAdapter.OnItemClickListener onItemClickListener) {
      this.onItemClickListener = onItemClickListener;
   }

   public interface OnItemClickListener {
      void OnItemClickListener(int position,String content);
   }
}
