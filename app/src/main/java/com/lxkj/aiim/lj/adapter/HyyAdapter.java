package com.lxkj.aiim.lj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lxkj.aiim.R;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.lxkj.aiim.lj.utils.ListUtil;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

import cc.shinichi.library.ImagePreview;

/**
 * @author:李迪迦
 * @date: 2024/9/20
 * @Type:
 */
public class HyyAdapter extends RecyclerView.Adapter<HyyAdapter.MyHolder> {


    private Context context;
    private List<DataListBean> list;
    private ImageAdapter imageAdapter;

    public HyyAdapter(Context context, List<DataListBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hyy, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, final int position) {
        holder.tvTitle.setText(list.get(position).title);
        if (!ListUtil.isEmpty(list.get(position).imgs)){
            holder.ryImage.setVisibility(View.VISIBLE);
            imageAdapter = new ImageAdapter(context, list.get(position).imgs, 87, 3);
            holder.ryImage.setLayoutManager(new GridLayoutManager(context,3));
            holder.ryImage.setAdapter(imageAdapter);
            imageAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
                @Override
                public void OnItemClick(int firstposition) {
                    ImagePreview
                            .getInstance()
                            .setContext(context)
                            .setIndex(firstposition)
                            .setImageList(list.get(position).imgs)
                            .start();
                }
            });
        }else
            holder.ryImage.setVisibility(View.GONE);

        if (!StringUtil.isEmpty(list.get(position).video)){
            holder.rlVideo.setVisibility(View.VISIBLE);
            GlideUtil.setImag(context, list.get(position).video, holder.riVideo);
        }else
            holder.rlVideo.setVisibility(View.GONE);

        if (!StringUtil.isEmpty(list.get(position).file)){
            holder.tvFile.setVisibility(View.VISIBLE);
            holder.tvFile.setText(list.get(position).name);
        }else
            holder.tvFile.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.OnItemClickListener(position);
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.OnItemDeleteClickListener(position);
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
        RoundedImageView riVideo;
        RelativeLayout rlVideo;
        RecyclerView ryImage;
        TextView tvFile;
        LinearLayout llItem;
        ImageView btnDelete;

        public MyHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            riVideo = itemView.findViewById(R.id.riVideo);
            rlVideo = itemView.findViewById(R.id.rlVideo);
            ryImage = itemView.findViewById(R.id.ryImage);
            tvFile = itemView.findViewById(R.id.tvFile);
            llItem = itemView.findViewById(R.id.llItem);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            ryImage = itemView.findViewById(R.id.ryImage);
        }
    }


    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position);

        void OnItemDeleteClickListener(int position);
    }
}
