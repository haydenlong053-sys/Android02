package com.cboda.agora.ui.ui.micseats.impl;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.agora.ui.R;
import com.cboda.agora.ui.basic.AUIBottomBroadDialog;
import com.cboda.agora.ui.basic.AUIRecyclerBroadView;
import com.cboda.agora.ui.micseats.IMicSeatItemBroadView;
import com.cboda.agora.ui.micseats.IMicSeatsBroadView;
import com.cboda.agora.ui.micseats.MicSeatBroadStatus;

public class AUIMicSeatsBroadView extends FrameLayout implements IMicSeatsBroadView {

    private RecyclerView.Adapter<RecyclerView.ViewHolder> mMicSeatsAdapter;
    private int micSeatCount = 8;
    private MicSeatItemViewWrap[] micSeatViewList = new MicSeatItemViewWrap[micSeatCount];
    private ActionDelegate actionDelegate;

    public AUIMicSeatsBroadView(@NonNull Context context) {
        this(context, null);
    }

    public AUIMicSeatsBroadView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUIMicSeatsBroadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        for (int i = 0; i < micSeatViewList.length; i++) {
            micSeatViewList[i] = new MicSeatItemViewWrap();
        }
        initView(context);
    }

    private void initView(@NonNull Context context) {
        View.inflate(context, R.layout.aui_micseats_view, this);
        AUIRecyclerBroadView rvMicSeats = findViewById(R.id.rv_mic_seats);
        mMicSeatsAdapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(new AUIMicSeatItemBroadView(parent.getContext())) {
                };
            }
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                AUIMicSeatItemBroadView seatItemView = (AUIMicSeatItemBroadView) holder.itemView;
                MicSeatItemViewWrap seatItemViewWrap = micSeatViewList[position];
                seatItemViewWrap.setView(seatItemView);
                if (position==0){
                    seatItemView.setRippleColor(context.getColor(R.color.aui_ripple_stroke_color_owner));
                }else {
                    seatItemView.setRippleColor(context.getColor(R.color.aui_ripple_stroke_color));
                }
                seatItemView.setOnClickListener(view -> {
                    showMicSeatDialog(position);
                });
            }
            @Override
            public int getItemCount() {
                return micSeatCount;
            }
        };
        rvMicSeats.setAdapter(mMicSeatsAdapter);
    }

    private void showMicSeatDialog(int index) {
        AUIBottomBroadDialog bottomSheetDialog = new AUIBottomBroadDialog(getContext());
        AUIMicSeatDialogBroadView contentView = new AUIMicSeatDialogBroadView(getContext());
        contentView.setEnterSeatClickListener(v -> {
            if (actionDelegate != null) {
                actionDelegate.onClickEnterSeat(index);
            }
            bottomSheetDialog.dismiss();
        });
        contentView.setLeaveSeatClickListener(v -> {
            if (actionDelegate != null) {
                actionDelegate.onClickLeaveSeat(index);
            }
            bottomSheetDialog.dismiss();
        });
        contentView.setKickSeatClickListener(v -> {
            if (actionDelegate != null) {
                actionDelegate.onClickKickSeat(index);
            }
            bottomSheetDialog.dismiss();
        });
        contentView.setCloseSeatClickListener(v -> {
            if (actionDelegate != null) {
                actionDelegate.onClickCloseSeat(index, !contentView.isSeatClosed());
            }
            bottomSheetDialog.dismiss();
        });
        contentView.setMuteAudioClickListener(v -> {
            if (actionDelegate != null) {
                actionDelegate.onClickMuteAudio(index, !contentView.isMuteAudio());
            }
            bottomSheetDialog.dismiss();
        });
        contentView.setMuteVideoClickListener(v -> {
            if (actionDelegate != null) {
                actionDelegate.onClickMuteVideo(index, !contentView.isMuteVideo());
            }
            bottomSheetDialog.dismiss();
        });
        contentView.setInvitedClickListener(v -> {
            if (actionDelegate != null) {
                actionDelegate.onClickInvited(index);
            }
            bottomSheetDialog.dismiss();
        });
        if (actionDelegate != null) {
            if (!actionDelegate.onClickSeat(index, contentView)) {
                return;
            }
        }
        bottomSheetDialog.setCustomView(contentView);
        bottomSheetDialog.show();
    }

    @Override
    public void setMicSeatCount(int count) {
        micSeatCount = count;
        micSeatViewList = new MicSeatItemViewWrap[micSeatCount];
        for (int i = 0; i < micSeatViewList.length; i++) {
            micSeatViewList[i] = new MicSeatItemViewWrap();
        }
        mMicSeatsAdapter.notifyDataSetChanged();
    }

    @Override
    public IMicSeatItemBroadView[] getMicSeatItemViewList() {
        return micSeatViewList;
    }

    @Override
    public void setMicSeatActionDelegate(ActionDelegate actionDelegate) {
        this.actionDelegate = actionDelegate;
    }

    @Override
    public void startRippleAnimation(int index) {
        MicSeatItemViewWrap micSeatItemViewWrap = micSeatViewList[index];
        if (micSeatItemViewWrap != null && micSeatItemViewWrap.view != null){
            AUIMicSeatItemBroadView view = (AUIMicSeatItemBroadView) micSeatItemViewWrap.view;
            view.startRippleAnimation();
        }
    }

    @Override
    public void stopRippleAnimation(int index) {
        MicSeatItemViewWrap micSeatItemViewWrap = micSeatViewList[index];
        if (micSeatItemViewWrap != null && micSeatItemViewWrap.view != null){
            AUIMicSeatItemBroadView view = (AUIMicSeatItemBroadView) micSeatItemViewWrap.view;
            view.stopRippleAnimation();
        }
    }

    private static class MicSeatItemViewWrap implements IMicSeatItemBroadView {
        private String titleText;
        private int titleIndex;
        private int audioMuteVisibility = View.GONE, videoMuteVisibility = View.GONE;
        private int audioMuteNewVisibility = View.GONE;
        private boolean audioMuteNewState = false;
        private int roomOwnerVisibility = View.GONE;
        private ChorusType chorusType = ChorusType.None;
        private Drawable userAvatarImageDrawable;
        private MicSeatBroadStatus seatStatus;
        private String userAvatarImageUrl;
        private IMicSeatItemBroadView view;

        private void setView(IMicSeatItemBroadView view) {
            this.view = view;
            setTitleText(titleText);
            setTitleIndex(titleIndex);
            setRoomOwnerVisibility(roomOwnerVisibility);
            setAudioMuteVisibility(audioMuteVisibility);
            setAudioMuteNewVisibility(audioMuteNewVisibility, audioMuteNewState);
            setVideoMuteVisibility(videoMuteVisibility);
            setUserAvatarImageDrawable(userAvatarImageDrawable);
            setMicSeatState(seatStatus);
            setUserAvatarImageUrl(userAvatarImageUrl);
        }

        @Override
        public void setTitleText(String text) {
            this.titleText = text;
            if (view != null) {
                view.setTitleText(text);
            }
        }
        @Override
        public void setRoomOwnerVisibility(int visible) {
            this.roomOwnerVisibility = visible;
            if (view != null) {
                view.setRoomOwnerVisibility(visible);
            }
        }
        @Override
        public void setTitleIndex(int index) {
            this.titleIndex = index;
            if (view != null) {
                view.setTitleIndex(index);
            }
        }
        @Override
        public void setAudioMuteVisibility(int visible) {
            this.audioMuteVisibility = visible;
            if (view != null) {
                view.setAudioMuteVisibility(visible);
            }
        }

        @Override
        public void setVideoMuteVisibility(int visible) {
            this.videoMuteVisibility = visible;
            if (view != null) {
                view.setVideoMuteVisibility(visible);
            }
        }

        @Override
        public void setUserAvatarImageDrawable(Drawable drawable) {
            this.userAvatarImageDrawable = drawable;
            if (view != null) {
                view.setUserAvatarImageDrawable(drawable);
            }
        }

        @Override
        public void setMicSeatState(MicSeatBroadStatus state) {
            this.seatStatus = state;
            if (view != null) {
                view.setMicSeatState(state);
            }
        }

        @Override
        public void setUserAvatarImageUrl(String url) {
            this.userAvatarImageUrl = url;
            if (view != null) {
                view.setUserAvatarImageUrl(url);
            }
        }

        @Override
        public void setChorusMicOwnerType(ChorusType type) {
            this.chorusType = type;
            if (view != null) {
                view.setChorusMicOwnerType(type);
            }
        }

        @Override
        public void setAudioMuteNewVisibility(int visible, boolean isMute) {
            this.audioMuteNewVisibility = visible;
            this.audioMuteNewState = isMute;
            if (view != null) {
                view.setAudioMuteNewVisibility(visible, isMute);
            }
        }
    }
}
