package com.cboda.intelligent.adapter;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.cboda.baselibrary.bean.DataListBroadBean;
import com.cboda.intelligent.utils.StringBroadUtil;

import java.util.List;

/**
 * @author:李迪迦
 * @date: 2024/9/20
 * @Type:
 */
public class SiteBroadAdapter extends RecyclerView.Adapter<SiteBroadAdapter.MyHolder> {
    private Context context;
    private List<DataListBroadBean> list;
    public SiteBroadAdapter(Context context, List<DataListBroadBean> list) {
        this.context = context;
        this.list = list;
    }
    @Override
    public SiteBroadAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_broad_site, parent, false);
        return new SiteBroadAdapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(SiteBroadAdapter.MyHolder holder, final int position) {
        if (position == list.size()-1){
            holder.vlLine.setVisibility(View.GONE);
        }else {
            holder.vlLine.setVisibility(View.VISIBLE);
        }
        holder.tvTitle.setText(list.get(position).title);
        if (StringBroadUtil.isEmpty(list.get(position).content)){
            holder.tvContent.setVisibility(View.GONE);
        }else {
            holder.tvContent.setVisibility(View.VISIBLE);


            Location location1 = new Location("point1");
            location1.setLatitude(Double.parseDouble(list.get(position).minelat));
            location1.setLongitude(Double.parseDouble(list.get(position).minelng));

            Location location2 = new Location("point2");
            location2.setLatitude(Double.parseDouble(list.get(position).lat));
            location2.setLongitude(Double.parseDouble(list.get(position).lng));
            float distance = location1.distanceTo(location2);
            if (distance>=1000){
                holder.tvContent.setText(String.format("%.1f",distance/1000)+"km"+" | "+list.get(position).content);
            }else {
                holder.tvContent.setText(String.format("%.1f",distance)+"m"+" | "+list.get(position).content);
            }
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
        View vlLine;
        TextView tvTitle;
        TextView tvContent;
        public MyHolder(View itemView) {
            super(itemView);
            vlLine = itemView.findViewById(R.id.vlLine);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
    private SiteBroadAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(SiteBroadAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position);
    }
}
