package com.lxkj.aiim.lj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.lxkj.aiim.lj.utils.AppUtil;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.lxkj.aiim.lj.utils.MediaFile;
import com.lxkj.aiim.lj.utils.ScreenBroadUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by kxn on 2021/12/7 0007.
 */
public class UpLoadImageAdapter extends RecyclerView.Adapter<UpLoadImageAdapter.ViewHolder> {

    private Context context;
    private List<String> list;
    private OnItemClickListener onItemClickListener;
    private int maxNum;
    private int space = 54;

    public UpLoadImageAdapter(Context context, List<String> list, int maxNum) {
        this.context = context;
        this.maxNum = maxNum;
        change(list);
    }

    public UpLoadImageAdapter(Context context, List<String> list, int maxNum, int space) {
        this.context = context;
        this.maxNum = maxNum;
        this.space = space;
        change(list);
    }

    public void change(List<String> list) {
        if (list == null) {
            this.list = new ArrayList<>();
        } else {
            this.list = list;
        }
    }
    public void setMaxNum(int maxNum) {
        this.maxNum = maxNum;
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_upload, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != onItemClickListener) {
                    if (position == list.size()) {
                        onItemClickListener.OnAddClick();
                    }else {
                    }
                }
            }
        });
        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.OnDeleteClick(position);
            }
        });
        ViewGroup.LayoutParams layoutParams = holder.ivImage.getLayoutParams();
        layoutParams.width = (ScreenBroadUtil.getScreenWidth(context) - AppUtil.dip2px(context, space)) / 3;
        layoutParams.height = layoutParams.width;
        holder.ivImage.setLayoutParams(layoutParams);
        if (position == list.size()) {
            holder.ivImage.setImageResource(R.mipmap.ic_add_image);
            holder.ivDelete.setVisibility(View.GONE);
            holder.ivVideo.setVisibility(View.GONE);
        } else {
            GlideUtil.setImag(context, list.get(position), holder.ivImage);
            if (MediaFile.isVideoFileType(list.get(position)))
                holder.ivVideo.setVisibility(View.VISIBLE);
            else
                holder.ivVideo.setVisibility(View.GONE);
            holder.ivDelete.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public int getItemCount() {
        if (maxNum > 0 && list.size() >= maxNum)
            return list.size();
        else
            return list.size() + 1;
    }

    public interface OnItemClickListener {
        void OnDeleteClick(int position);

        void OnAddClick();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView ivImage;
        ImageView ivDelete;
        ImageView ivVideo;

        public ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            ivVideo = itemView.findViewById(R.id.ivVideo);

        }
    }
}