package com.cboda.intelligent.adapter;

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

import com.cboda.intelligent.R;
import com.cboda.baselibrary.bean.DataListBroadBean;
import com.cboda.intelligent.utils.GlideBroadUtil;
import com.cboda.intelligent.utils.ListBroadUtil;
import com.cboda.intelligent.utils.StringBroadUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

import cc.shinichi.library.ImagePreview;

/**
 * @author:李迪迦
 * @date: 2024/9/20
 * @Type:
 */
public class HyyBroadAdapter extends RecyclerView.Adapter<HyyBroadAdapter.MyHolder> {


    private Context context;
    private List<DataListBroadBean> list;
    private ImageBroadAdapter imageAdapter;

    public HyyBroadAdapter(Context context, List<DataListBroadBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_broad_hyy, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, final int position) {
        holder.tvTitle.setText(list.get(position).title);
        if (!ListBroadUtil.isEmpty(list.get(position).imgs)){
            holder.ryImage.setVisibility(View.VISIBLE);
            imageAdapter = new ImageBroadAdapter(context, list.get(position).imgs, 87, 3);
            holder.ryImage.setLayoutManager(new GridLayoutManager(context,3));
            holder.ryImage.setAdapter(imageAdapter);
            imageAdapter.setOnItemClickListener(new ImageBroadAdapter.OnItemClickListener() {
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

        if (!StringBroadUtil.isEmpty(list.get(position).video)){
            holder.rlVideo.setVisibility(View.VISIBLE);
            GlideBroadUtil.setImag(context, list.get(position).video, holder.riVideo);
        }else
            holder.rlVideo.setVisibility(View.GONE);

        if (!StringBroadUtil.isEmpty(list.get(position).file)){
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
