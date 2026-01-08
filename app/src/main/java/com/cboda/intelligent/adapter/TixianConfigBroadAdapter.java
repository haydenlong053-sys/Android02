package com.cboda.intelligent.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.intelligent.bean.DataListBroadBean;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/26
 * @Type:
 */
public class TixianConfigBroadAdapter extends RecyclerView.Adapter<TixianConfigBroadAdapter.MyHolder> {
   private Context context;
   private List<DataListBroadBean> list;
   public TixianConfigBroadAdapter(Context context, List<DataListBroadBean> list) {
      this.context = context;
      this.list = list;
   }
   @Override
   public TixianConfigBroadAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(context).inflate(R.layout.item_ti_broad_xian_config, parent, false);
      return new TixianConfigBroadAdapter.MyHolder(view);
   }

   @Override
   public void onBindViewHolder(TixianConfigBroadAdapter.MyHolder holder, final int position) {
      holder.setIsRecyclable(false);
      holder.tvTitle.setText(list.get(position).title);
      holder.etContent.setHint(list.get(position).info);
      holder.etContent.addTextChangedListener(new TextWatcher() {
         @Override
         public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

         }

         @Override
         public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

         }

         @Override
         public void afterTextChanged(Editable editable) {
            onItemClickListener.OnItemClickListener(position,editable.toString());
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
      EditText etContent;
      public MyHolder(View itemView) {
         super(itemView);
         tvTitle = itemView.findViewById(R.id.tvTitle);
         etContent = itemView.findViewById(R.id.etContent);
      }
   }
   private TixianConfigBroadAdapter.OnItemClickListener onItemClickListener;

   public void setOnItemClickListener(TixianConfigBroadAdapter.OnItemClickListener onItemClickListener) {
      this.onItemClickListener = onItemClickListener;
   }

   public interface OnItemClickListener {
      void OnItemClickListener(int position,String content);
   }
}
