package com.lxkj.aiim.lj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.lxkj.aiim.R;
import com.lxkj.aiim.lj.utils.AppUtil;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.lxkj.aiim.lj.utils.ScreenBroadUtil;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;


/**
 * @author:李迪迦
 * @date: 2024/7/29
 * @Type:
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private Context context;
    private List<String> list;
    private OnItemClickListener onItemClickListener;
    private int space = 54;
    private int lineNum = 4;


    public ImageAdapter(Context context, List<String> list, int space, int lineNum) {
        this.context = context;
        this.space = space;
        this.lineNum = lineNum;
        change(list);
    }
    public void change(List<String> list) {
        if (list == null) {
            this.list = new ArrayList<>();
        } else {
            this.list = list;
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != onItemClickListener) {
                    onItemClickListener.OnItemClick(position);
                }
            }
        });
        ViewGroup.LayoutParams layoutParams = holder.ivImage.getLayoutParams();
        layoutParams.width = (ScreenBroadUtil.getScreenWidth(context) - AppUtil.dip2px(context, space)) / lineNum;
        layoutParams.height = layoutParams.width;
        holder.ivImage.setLayoutParams(layoutParams);
        GlideUtil.setImag(context, list.get(position), holder.ivImage);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemClickListener {
        void OnItemClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView ivImage;
        public ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);

        }
    }
}