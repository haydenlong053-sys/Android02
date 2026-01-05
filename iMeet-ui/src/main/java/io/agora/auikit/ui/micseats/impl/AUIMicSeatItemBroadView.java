package io.agora.auikit.ui.micseats.impl;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.cboda.agora.ui.R;
import io.agora.auikit.ui.micseats.IMicSeatItemBroadView;
import io.agora.auikit.ui.micseats.MicSeatBroadStatus;

public class AUIMicSeatItemBroadView extends FrameLayout implements IMicSeatItemBroadView, ViewTreeObserver.OnGlobalLayoutListener {
    private String titleIdleText;
    private ImageView ivStateIdle;
    private ImageView ivStateLock;
    private TextView tvTitle;
    private TextView tvRoomOwner;
    private TextView tvLeadSinger;
    private TextView tvChorus;
    private ImageView ivAudioMute;
    private ImageView ivAudioMuteNew;
    private ImageView ivVideoMute;
    private View bgSeat;
    private ImageView ivUserAvatar;
    private AUIRippleAnimationBroadView ripple;
    private int itemWidth;
    private Context context;

    public AUIMicSeatItemBroadView(@NonNull Context context) {
        this(context, null);
    }

    public AUIMicSeatItemBroadView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUIMicSeatItemBroadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        TypedArray themeTa = context.obtainStyledAttributes(attrs, R.styleable.AUIMicSeatItemBroadView, defStyleAttr, 0);
        int appearanceId = themeTa.getResourceId(R.styleable.AUIMicSeatItemBroadView_aui_micSeatItem_appearance, 0);
        themeTa.recycle();
        initView(context, appearanceId);
    }

    private void initView(@NonNull Context context, @StyleRes int appearanceId) {
        View.inflate(context, R.layout.aui_micseat_item_view, this);

        TypedArray typedArray = context.obtainStyledAttributes(appearanceId, R.styleable.AUIMicSeatItemBroadView);
        int audioMuteIconGravity = typedArray.getInt(R.styleable.AUIMicSeatItemBroadView_aui_micSeatItem_audioMuteIconGravity, 1);
        titleIdleText = typedArray.getString(R.styleable.AUIMicSeatItemBroadView_aui_micSeatItem_titleIdleText);
        typedArray.recycle();

        bgSeat = findViewById(R.id.bg_seat);
        ivStateIdle = findViewById(R.id.iv_state_idle);
        ivStateLock = findViewById(R.id.iv_state_lock);
        ivUserAvatar = findViewById(R.id.iv_user_avatar);
        tvTitle = findViewById(R.id.tv_title);
        tvRoomOwner = findViewById(R.id.tv_room_owner);
        tvLeadSinger = findViewById(R.id.tv_lead_singer);
        tvChorus = findViewById(R.id.tv_chorus);
        ivAudioMute = findViewById(R.id.iv_audio_mute);
        ivAudioMuteNew = findViewById(R.id.iv_audio_mute_new);
        ivVideoMute = findViewById(R.id.iv_video_mute);
        ripple = findViewById(R.id.iv_ripple);

        // 设置监听器以获取宽度
        getViewTreeObserver().addOnGlobalLayoutListener(this);

        ripple.setInterpolator(new AccelerateInterpolator(1.4f));
        ripple.setStyle(Paint.Style.STROKE);

        // 静音图标位置配置
        LayoutParams ivAudioMuteParams = (LayoutParams) ivAudioMute.getLayoutParams();
        switch (audioMuteIconGravity) {
            case 1: // bottomEnd
                ivAudioMuteParams.gravity = Gravity.BOTTOM | Gravity.END;
                ivAudioMute.setLayoutParams(ivAudioMuteParams);
                break;
            case 2: // center
                ivAudioMuteParams.gravity = Gravity.CENTER;
                ivAudioMute.setLayoutParams(ivAudioMuteParams);
                break;
        }
        // 关闭视频图标位置配置
        LayoutParams ivVideoMuteParams = (LayoutParams) ivVideoMute.getLayoutParams();
        switch (audioMuteIconGravity) {
            case 1: // bottomEnd
                ivVideoMuteParams.gravity = Gravity.BOTTOM | Gravity.END;
                ivVideoMute.setLayoutParams(ivVideoMuteParams);
                break;
            case 2: // center
                ivVideoMuteParams.gravity = Gravity.CENTER;
                ivVideoMute.setLayoutParams(ivVideoMuteParams);
                break;
        }
        Drawable rippleDrawable= ContextCompat.getDrawable(context,R.drawable.aui_mic_item_ripple);
        bgSeat.setForeground(rippleDrawable);
    }

    public void setRippleInitialRadius(float radius){
        ripple.setInitialRadius(radius);
    }

    public  void setRippleColor(int color){
        ripple.setColor(color);
    }

    public void setRippleInterpolator(float value){
        ripple.setInterpolator(new AccelerateInterpolator(value));
    }

    @Override
    public void setRoomOwnerVisibility(int visible) {
        tvRoomOwner.setVisibility(visible);
    }

    @Override
    public void setTitleIndex(int index) {
        if (titleIdleText.contains("%d")) {
            tvTitle.setText(String.format(titleIdleText, index));
        }
    }

    @Override
    public void setTitleText(String text) {
        tvTitle.setText(text);
    }

    public void startRippleAnimation(){
        ripple.startRippleAnimation();
    }

    public void stopRippleAnimation(){
        ripple.stopImmediatelyRippleAnimation();
    }

    @Override
    public void setAudioMuteVisibility(int visible) {
//        ivAudioMute.setVisibility(visible);
        ivAudioMute.setVisibility(View.GONE);
//        if (visible ==View.VISIBLE){
//            ivAudioMute.setVisibility(View.VISIBLE);
//        }else {
//            ivAudioMute.setVisibility(View.INVISIBLE);
//        }
//        if (visible == View.VISIBLE) {
//            ivAudioMuteNew.setImageResource(R.drawable.ic_seat_mic_off);
//        } else {
//            ivAudioMuteNew.setImageResource(R.drawable.ic_seat_mic_on);
//
//        }
    }

    @Override
    public void setAudioMuteNewVisibility(int visible, boolean isMute) {
        ivAudioMuteNew.setVisibility(visible);
        if (visible == View.VISIBLE) {
            if (isMute) {
                ivAudioMuteNew.setImageResource(R.drawable.ic_seat_mic_off);
            } else {
                ivAudioMuteNew.setImageResource(R.drawable.ic_seat_mic_on);
            }
        }
    }

    @Override
    public void setVideoMuteVisibility(int visible) {
        ivVideoMute.setVisibility(visible);
    }

    @Override
    public void setUserAvatarImageDrawable(Drawable drawable) {
        ivUserAvatar.setImageDrawable(drawable);
    }

    @Override
    public void setMicSeatState(MicSeatBroadStatus state) {
        if (state == MicSeatBroadStatus.locked) {
            bgSeat.setBackgroundResource(R.drawable.aui_micseat_item_seat_bg_lock);
            ivStateLock.setVisibility(View.VISIBLE);
            ivStateIdle.setVisibility(View.INVISIBLE);
        } else {
            bgSeat.setBackgroundResource(R.drawable.aui_micseat_item_seat_bg_normal);
            ivStateIdle.setVisibility(View.VISIBLE);
            ivStateLock.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setUserAvatarImageUrl(String url) {
        RequestOptions options = RequestOptions.circleCropTransform();
        Glide.with(getContext()).load(url).apply(options).into(ivUserAvatar);
    }

    @Override
    public void setChorusMicOwnerType(ChorusType type) {
        if (type == ChorusType.LeadSinger) {
            tvChorus.setVisibility(View.VISIBLE);
            Drawable drawable = getResources().getDrawable(R.drawable.aui_micseat_item_icon_leadsinger);
            tvChorus.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            tvChorus.setText(R.string.aui_micseat_leadsinger);
        } else if (type == ChorusType.SecondarySinger) {
            tvChorus.setVisibility(View.VISIBLE);
            Drawable drawable = getResources().getDrawable(R.drawable.aui_micseat_item_icon_chorus);
            tvChorus.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            tvChorus.setText(R.string.aui_micseat_chorus);
        } else {
            tvChorus.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onGlobalLayout() {
        // 当布局完成时，获取 ImageView 的宽度
        if (ivUserAvatar.getWidth() > 0) {
            itemWidth = ivUserAvatar.getWidth();
            // 在获取到宽度后，移除监听器
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
            ripple.setInitialRadius(itemWidth/2);
            ripple.setMaxRadius(itemWidth/2 + 25);
        }
    }
}
