package com.lxkj.aiim.lj.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lxkj.aiim.R;
import com.lxkj.aiim.lj.bean.DataListBean;
import com.lxkj.aiim.lj.configuration.AppConsts;
import com.lxkj.aiim.lj.utils.GlideUtil;
import com.lxkj.aiim.lj.utils.ListUtil;
import com.lxkj.aiim.lj.utils.SharePrefUtil;
import com.lxkj.aiim.lj.utils.StringUtil;
import com.lxkj.aiim.lj.utils.ToastUtil;
import com.lxkj.aiim.lj.view.ExpandableTextView;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.shinichi.library.ImagePreview;

/**
 * @author:李迪迦
 * @date: 2024/9/20
 * @Type:
 */
public class CiecelAdapter extends RecyclerView.Adapter<CiecelAdapter.MyHolder> {

    private Context context;
    private List<DataListBean> list;
    private HuiFuAdapter huiFuAdapter;
    private ImageAdapter imageAdapter;

    public CiecelAdapter(Context context, List<DataListBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_circle, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, final int position) {
        GlideUtil.setHeadImag(context,list.get(position).icon,holder.riIcon);
        holder.tvName.setText(list.get(position).name);
        holder.tvContent.setText(list.get(position).info);
        holder.tvContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ToastUtil.show("已复制");
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", list.get(position).info);
                clipboard.setPrimaryClip(clip);
                return false;
            }
        });
        if (list.get(position).type.equals("1")){//图片
            if (list.get(position).imgs.size() == 1){
                holder.rlVideo.setVisibility(View.VISIBLE);
                holder.imVideo.setVisibility(View.GONE);
                holder.ryImage.setVisibility(View.GONE);
                GlideUtil.setImag(context,list.get(position).imgs.get(0),holder.riVideo);
            }else {
                holder.rlVideo.setVisibility(View.GONE);
                holder.ryImage.setVisibility(View.VISIBLE);
                imageAdapter = new ImageAdapter(context, list.get(position).imgs, 147, 3);
                holder.ryImage.setLayoutManager(new GridLayoutManager(context, 3));
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
            }

        }else {
            holder.rlVideo.setVisibility(View.VISIBLE);
            holder.imVideo.setVisibility(View.VISIBLE);
            holder.ryImage.setVisibility(View.GONE);
            GlideUtil.setImag(context,list.get(position).vimg,holder.riVideo);
        }

        if (StringUtil.isEmpty(list.get(position).address)){
            holder.tvSite.setVisibility(View.GONE);
        }else {
            holder.tvSite.setVisibility(View.VISIBLE);
            holder.tvSite.setText(list.get(position).address);
        }
        holder.tvTime.setText(list.get(position).time);
        if (list.get(position).userId.equals(SharePrefUtil.getString(context, AppConsts.UID,""))){
            holder.imDelate.setVisibility(View.VISIBLE);
        }else {
            holder.imDelate.setVisibility(View.GONE);
        }
        if (list.get(position).isChecked){
            holder.llMoreShow.setVisibility(View.VISIBLE);
        }else {
            holder.llMoreShow.setVisibility(View.GONE);
        }

        if (list.get(position).isDz.equals("0")){
            holder.imDianzan.setImageResource(R.mipmap.ic_circle_zan_false);
            holder.imDianzanAll.setImageResource(R.mipmap.ic_dianzan_num_false);
        }else {
            holder.imDianzan.setImageResource(R.mipmap.ic_circle_zan_true);
            holder.imDianzanAll.setImageResource(R.mipmap.ic_circle_zan_true);
        }

//        holder.tvDianzanName.initWidth(layoutParams.width);
        holder.tvDianzanName.setMovementMethod(LinkMovementMethod.getInstance());
        holder.tvDianzanName.setHighlightColor(ContextCompat.getColor(context,android.R.color.transparent));

        if (StringUtil.isEmpty(list.get(position).dzNames)){
            holder.llDianzanAll.setVisibility(View.GONE);
        }else {
            holder.llDianzanAll.setVisibility(View.VISIBLE);
            List<String> list1 = new ArrayList<>();
            for (int i = 0; i < list.get(position).likeList.size(); i++) {
                list1.add(list.get(position).likeList.get(i).name);
            }
            if (ListUtil.isEmpty(list.get(position).likeList)){
                holder.tvDianzanName.setOriginalText(list.get(position).dzNames);
            }else {
                holder.tvDianzanName.setOriginalText(showRequestRecord(list.get(position).dzNames,list1,position));
            }
        }



        if (!ListUtil.isEmpty(list.get(position).commList)){
            holder.llPinglunItem.setVisibility(View.VISIBLE);
            huiFuAdapter = new HuiFuAdapter(context, list.get(position).commList);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            holder.ryHuifu.setLayoutManager(linearLayoutManager);
            holder.ryHuifu.setAdapter(huiFuAdapter);
            huiFuAdapter.setOnItemClickListener(new HuiFuAdapter.OnItemClickListener() {
                @Override
                public void OnItemClickListener(int fposition, String content) {
                    onItemClickListener.OnItemClickListener(position,fposition,content);
                }
            });
        }else {
            holder.llPinglunItem.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.OnItemClickListener(position,"详情");
            }
        });
        holder.llMore.setOnClickListener(view -> {
            onItemClickListener.OnItemClickListener(position,"更多");
        });
        holder.imDelate.setOnClickListener(view -> {
            onItemClickListener.OnItemClickListener(position,"删除");
        });
        holder.llDianzan.setOnClickListener(view -> {
            onItemClickListener.OnItemClickListener(position,"点赞");
        });
        holder.llPinglun.setOnClickListener(view -> {
            onItemClickListener.OnItemClickListener(position,"评论");
        });
        holder.tvSite.setOnClickListener(view -> {
            onItemClickListener.OnItemClickListener(position,"地址");
        });
        holder.riIcon.setOnClickListener(view -> {
            onItemClickListener.OnItemClickListener(position,"主页");
        });
        holder.rlVideo.setOnClickListener(view -> {
            if (list.get(position).type.equals("1")){
                ImagePreview
                        .getInstance()
                        .setContext(context)
                        .setIndex(0)
                        .setImageList(list.get(position).imgs)
                        .start();
            }else {
                onItemClickListener.OnItemClickListener(position,"视频");
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
        RoundedImageView riIcon;
        TextView tvName;
        TextView tvContent;
        RoundedImageView riVideo;
        ImageView imVideo;
        RelativeLayout rlVideo;
        RecyclerView ryImage;
        TextView tvSite;
        TextView tvTime;
        ImageView imDelate;
        LinearLayout llMore;
        ImageView imDianzan;
        LinearLayout llDianzan;
        LinearLayout llPinglun;
        LinearLayout llMoreShow;
        ImageView imDianzanAll;
        ExpandableTextView tvDianzanName;
        LinearLayout llDianzanAll;
        RecyclerView ryHuifu;
        LinearLayout llPinglunItem;
        LinearLayout llPinglunAll;
        LinearLayout llMoreLayout;

        public MyHolder(View itemView) {
            super(itemView);
            ryImage = itemView.findViewById(R.id.ryImage);
            ryHuifu = itemView.findViewById(R.id.ryHuifu);
            riIcon = itemView.findViewById(R.id.riIcon);
            tvName = itemView.findViewById(R.id.tvName);
            tvContent = itemView.findViewById(R.id.tvContent);
            riVideo = itemView.findViewById(R.id.riVideo);
            imVideo = itemView.findViewById(R.id.imVideo);
            rlVideo = itemView.findViewById(R.id.rlVideo);
            tvSite = itemView.findViewById(R.id.tvSite);
            tvTime = itemView.findViewById(R.id.tvTime);
            imDelate = itemView.findViewById(R.id.imDelate);
            llMore = itemView.findViewById(R.id.llMore);
            imDianzan = itemView.findViewById(R.id.imDianzan);
            llDianzan = itemView.findViewById(R.id.llDianzan);
            llPinglun = itemView.findViewById(R.id.llPinglun);
            llMoreShow = itemView.findViewById(R.id.llMoreShow);
            imDianzanAll = itemView.findViewById(R.id.imDianzanAll);
            tvDianzanName = itemView.findViewById(R.id.tvDianzanName);
            llDianzanAll = itemView.findViewById(R.id.llDianzanAll);
            llPinglunItem = itemView.findViewById(R.id.llPinglunItem);
            llPinglunAll = itemView.findViewById(R.id.llPinglunAll);
            llMoreLayout = itemView.findViewById(R.id.llMoreLayout);
        }
    }
    public SpannableString showRequestRecord(String desc, List<String> stringlist, int position) {
        //创建 SpannableString 对象
        SpannableString mStyledText = new SpannableString(desc);
        for (int i = 0; i < stringlist.size(); i++) {
            Pattern mPattern = Pattern.compile(stringlist.get(i));
            Matcher mMatcher = mPattern.matcher(desc);
            while (mMatcher.find()) {
                int finalI = i;
                ClickableSpan what = new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        //这里的代码是对按钮点击事件的处理。
                        onItemClickListener.OnItemClickListener(position,"点赞",stringlist.get(finalI));
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);//去掉下划线
                        ds.setColor(ContextCompat.getColor(context,R.color.huati));//设置字体颜色
                        ds.setTypeface(Typeface.DEFAULT_BOLD);
                    }
                };
                mStyledText.setSpan(what, mMatcher.start(), mMatcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return mStyledText;
    }
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void OnItemClickListener(int position,String content);
        void OnItemClickListener(int position,int firstPosition,String content);
        void OnItemClickListener(int position,String type,String content);
    }
}
