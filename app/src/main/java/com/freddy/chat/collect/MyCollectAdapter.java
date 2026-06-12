package com.freddy.chat.collect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.baselibrary.utils.OnSingleClickBroadListener;
import com.cboda.intelligent.R;
import com.cboda.intelligent.utils.GlideBroadUtil;
import com.cboda.select.picture.entity.DataListBroadBean;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

/**
 * 自研收藏列表（展示解析 {@code info} JSON，逻辑自 {@link com.cboda.intelligent.adapter.MyCollectBroadAdapter}）。
 */
public class MyCollectAdapter extends RecyclerView.Adapter<MyCollectAdapter.MyHolder> {

    private final Context context;
    private final List<DataListBroadBean> list;

    public MyCollectAdapter(Context context, List<DataListBroadBean> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_broad_collect, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {
        DataListBroadBean item = list.get(position);
        String type = item.type != null ? item.type : "";
        String info = item.info;

        holder.tvContent.setVisibility(View.GONE);
        holder.rlVideo.setVisibility(View.GONE);
        holder.llVoice.setVisibility(View.GONE);

        holder.tvName.setText(item.name);
        holder.tvTime.setText(item.time);

        switch (type) {
            case CollectContentParser.TYPE_TEXT:
            case CollectContentParser.TYPE_LOCATION:
                holder.tvContent.setVisibility(View.VISIBLE);
                holder.tvContent.setText(CollectContentParser.resolveDisplayText(type, info));
                break;
            case CollectContentParser.TYPE_IMAGE:
                holder.rlVideo.setVisibility(View.VISIBLE);
                holder.imVideo.setVisibility(View.GONE);
                bindImage(holder.riImage, CollectContentParser.resolveImageDisplayUrl(type, info));
                break;
            case CollectContentParser.TYPE_VIDEO:
                holder.rlVideo.setVisibility(View.VISIBLE);
                holder.imVideo.setVisibility(View.VISIBLE);
                bindImage(holder.riImage, CollectContentParser.resolveImageDisplayUrl(type, info));
                break;
            case CollectContentParser.TYPE_VOICE:
                holder.llVoice.setVisibility(View.VISIBLE);
                int sec = CollectContentParser.resolveVoiceDurationSec(type, info, item.miao);
                holder.tvVoiceTime.setText(sec + "s");
                break;
            default:
                if (!TextUtils.isEmpty(info)) {
                    holder.tvContent.setVisibility(View.VISIBLE);
                    holder.tvContent.setText(CollectContentParser.resolveDisplayText(type, info));
                }
                break;
        }

        holder.itemView.setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(position);
                }
            }
        });
        holder.mIvShare.setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemChildClick(v, position);
                }
            }
        });
        holder.mIvDel.setOnClickListener(new OnSingleClickBroadListener() {
            @Override
            public void onSingleClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemDelClick(v, position);
                }
            }
        });
    }

    private void bindImage(@NonNull RoundedImageView imageView, @NonNull String url) {
        if (TextUtils.isEmpty(url)) {
            imageView.setImageDrawable(null);
            return;
        }
        GlideBroadUtil.setImag(context, url, imageView);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        final TextView tvContent;
        final TextView tvVoiceTime;
        final TextView tvName;
        final TextView tvTime;
        final RelativeLayout rlVideo;
        final RoundedImageView riImage;
        final ImageView imVideo;
        final ImageView mIvShare;
        final ImageView mIvDel;
        final LinearLayout llVoice;

        MyHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvContent);
            rlVideo = itemView.findViewById(R.id.rlVideo);
            riImage = itemView.findViewById(R.id.riImage);
            imVideo = itemView.findViewById(R.id.imVideo);
            llVoice = itemView.findViewById(R.id.llVoice);
            tvVoiceTime = itemView.findViewById(R.id.tvVoiceTime);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            mIvShare = itemView.findViewById(R.id.mIvShare);
            mIvDel = itemView.findViewById(R.id.mIvDel);
        }
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onItemChildClick(View view, int position);

        void onItemDelClick(View view, int position);
    }
}
