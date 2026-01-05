package com.example.asceneskit.binder;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.asceneskit.AUIVoiceRoomBroadService;
import com.example.asceneskit.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.auikit.model.AUIMicSeatInfo;
import io.agora.auikit.model.AUIMicSeatStatus;
import io.agora.auikit.model.AUIRoomContext;
import io.agora.auikit.model.AUIUserInfo;
import io.agora.auikit.model.AUIUserThumbnailInfo;
import io.agora.auikit.service.IAUIInvitationService;
import io.agora.auikit.service.IAUIMicSeatService;
import io.agora.auikit.service.IAUIUserService;
import io.agora.auikit.ui.basic.AUIAlertBroadDialog;
import io.agora.auikit.ui.micseats.IMicSeatDialogBroadView;
import io.agora.auikit.ui.micseats.IMicSeatItemBroadView;
import io.agora.auikit.ui.micseats.IMicSeatsBroadView;
import io.agora.auikit.ui.micseats.MicSeatBroadStatus;
import io.agora.auikit.utils.AUILogger;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;

public class AUIMicSeatsBroadBinder extends IRtcEngineEventHandler implements
        IAUIBroadBindable,
        IMicSeatsBroadView.ActionDelegate,
        IAUIMicSeatService.AUIMicSeatRespObserver,
        IAUIUserService.AUIUserRespObserver {
    private final IMicSeatsBroadView micSeatsView;
    private final IAUIUserService userService;
    private final IAUIMicSeatService micSeatService;
    private final IAUIInvitationService invitationService;
    private AUIRoomContext roomContext;
    private Handler mMainHandler;
    private RtcEngine mRtcEngine;
    private Map<Integer, String> mSeatMap = new HashMap();
    private Map<String,Integer> mVolumeMap = new HashMap();
    private Context context;
    private AUIAlertBroadDialog auiAlertDialog;


    public AUIMicSeatsBroadBinder(
            Context context,
            IMicSeatsBroadView micSeatsView,
            AUIVoiceRoomBroadService voiceService) {
        this.userService = voiceService.getUserService();
        this.micSeatsView = micSeatsView;
        this.micSeatService = voiceService.getMicSeatService();
        this.invitationService = voiceService.getInvitationService();
        this.mRtcEngine = voiceService.getRtcEngine();
        this.roomContext = AUIRoomContext.shared();
        this.context = context;
        mSeatMap.put(0,roomContext.getRoomOwner(micSeatService.getChannelName()));
        mVolumeMap.put(roomContext.getRoomOwner(micSeatService.getChannelName()),0);

        // update view  micSeatsView.getMicSeatItemViewList();
        IMicSeatItemBroadView[] seatViewList = micSeatsView.getMicSeatItemViewList();
        for (int seatIndex = 0; seatIndex < seatViewList.length; seatIndex++) {
            updateSeatView(seatIndex);
        }
    }

    @Override
    public void bind() {
        mMainHandler = new Handler(Looper.getMainLooper());
        userService.registerRespObserver(this);
        micSeatService.registerRespObserver(this);
        micSeatsView.setMicSeatActionDelegate(this);
        try {
            mRtcEngine.addHandler(this);
        } catch (Exception e) {
            // try : NullPointerException: Attempt to invoke virtual method
            // 'void io.agora.rtc2.internal.RtcEngineImpl.addHandler(io.agora.rtc2.IAgoraEventHandler)'
            // on a null object reference
        }
    }

    @Override
    public void unBind() {
        mMainHandler.removeCallbacksAndMessages(null);
        mMainHandler = null;
        userService.unRegisterRespObserver(this);
        micSeatService.unRegisterRespObserver(this);

        try {
            mRtcEngine.removeHandler(this);
        } catch (Exception e) {
            // do nothing
        }
        mRtcEngine.registerAudioFrameObserver(null);

        micSeatsView.setMicSeatActionDelegate(null);
    }
    private void runOnUiThread(@NonNull Runnable runnable) {
        if (mMainHandler != null) {
            if (mMainHandler.getLooper().getThread() == Thread.currentThread()) {
                runnable.run();
            } else {
                mMainHandler.post(runnable);
            }
        }
    }
    /** IAUIMicSeatService.AUIMicSeatRespDelegate implements. */

    @Override
    public void onAnchorEnterSeat(int seatIndex, @NonNull AUIUserThumbnailInfo userInfo) {
        if(seatIndex >=  micSeatsView.getMicSeatItemViewList().length){
            return;
        }
        AUIUserInfo auiUserInfo = userService.getUserInfo(userInfo.userId);
        mSeatMap.put(seatIndex,userInfo.userId);
        mVolumeMap.put(userInfo.userId,seatIndex);
        IMicSeatItemBroadView seatView = micSeatsView.getMicSeatItemViewList()[seatIndex];
        if (auiUserInfo != null){
            seatView.setTitleText(auiUserInfo.userName);
            seatView.setUserAvatarImageUrl(auiUserInfo.userAvatar);
        }
        // 调用 updateSeatView 确保新用户上麦时显示正确的麦位图标状态
        updateSeatView(seatIndex);
    }

    @Override
    public void onAnchorLeaveSeat(int seatIndex, @NonNull AUIUserThumbnailInfo userInfo) {
        if(seatIndex >=  micSeatsView.getMicSeatItemViewList().length){
            return;
        }
        String uid = mSeatMap.get(seatIndex - 1);
        if (uid != null && uid.equals(userInfo.userId)) {
            mSeatMap.remove(seatIndex);
        }
        mVolumeMap.remove(userInfo.userId);
        updateSeatView(seatIndex);
        micSeatsView.stopRippleAnimation(seatIndex);
    }

    @Override
    public void onSeatAudioMute(int seatIndex, boolean isMute) {
        if(seatIndex >=  micSeatsView.getMicSeatItemViewList().length){
            return;
        }
        IMicSeatItemBroadView seatView = micSeatsView.getMicSeatItemViewList()[seatIndex];
        // 使用旧的方法保持兼容性
        seatView.setAudioMuteVisibility(isMute ? View.VISIBLE : View.GONE);

        // 检查是否有用户在麦位上
        AUIMicSeatInfo micSeatInfo = micSeatService.getMicSeatInfo(seatIndex);
        boolean hasUser = micSeatInfo != null && micSeatInfo.user != null && !TextUtils.isEmpty(micSeatInfo.user.userId);

        // 使用新的麦位图标控制逻辑
        if (hasUser) {
            seatView.setAudioMuteNewVisibility(View.VISIBLE, isMute);
        } else {
            seatView.setAudioMuteNewVisibility(View.GONE, false);
        }
    }

    @Override
    public void onSeatVideoMute(int seatIndex, boolean isMute) {
        if(seatIndex >=  micSeatsView.getMicSeatItemViewList().length){
            return;
        }
        updateSeatView(seatIndex);
        micSeatsView.stopRippleAnimation(seatIndex);
    }

    @Override
    public void onSeatClose(int seatIndex, boolean isClose) {
        if(seatIndex >=  micSeatsView.getMicSeatItemViewList().length){
            return;
        }
        AUIMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(seatIndex);
        IMicSeatItemBroadView seatView = micSeatsView.getMicSeatItemViewList()[seatIndex];
        if (seatInfo.seatStatus == AUIMicSeatStatus.idle) {
            seatView.setMicSeatState(MicSeatBroadStatus.idle);
        } else if (seatInfo.seatStatus == AUIMicSeatStatus.locked) {
            seatView.setMicSeatState(MicSeatBroadStatus.locked);
        } else {
            seatView.setMicSeatState(MicSeatBroadStatus.used);
        }

        micSeatsView.stopRippleAnimation(seatIndex);
    }

    private void updateSeatView(int seatIndex) {
        if (seatIndex >= micSeatsView.getMicSeatItemViewList().length) {
            return;
        }
        AUIMicSeatInfo micSeatInfo = micSeatService.getMicSeatInfo(seatIndex);
        IMicSeatItemBroadView seatView = micSeatsView.getMicSeatItemViewList()[seatIndex];

        // 检查麦位是否为空
        boolean isSeatEmpty = micSeatInfo == null || micSeatInfo.seatStatus == AUIMicSeatStatus.idle ||
                micSeatInfo.user == null || TextUtils.isEmpty(micSeatInfo.user.userId);

        if (isSeatEmpty) {
            // 麦位为空时显示麦位编号
            seatView.setTitleIndex(seatIndex + 1);
            seatView.setUserAvatarImageDrawable(null);
            seatView.setChorusMicOwnerType(IMicSeatItemBroadView.ChorusType.None);
            seatView.setAudioMuteNewVisibility(View.GONE, false);
            seatView.setRoomOwnerVisibility((seatIndex == 0) ? View.VISIBLE : View.GONE);
            return;
        }

        // 麦位有用户，获取用户信息
        AUIUserInfo userInfo = userService.getUserInfo(micSeatInfo.user.userId);
        if (userInfo != null){
            mVolumeMap.put(userInfo.userId, seatIndex);
            micSeatInfo.user.userName = userInfo.userName;
            micSeatInfo.user.userAvatar = userInfo.userAvatar;
        }

        seatView.setRoomOwnerVisibility((seatIndex == 0) ? View.VISIBLE : View.GONE);

        if (micSeatInfo.seatStatus == AUIMicSeatStatus.locked){
            seatView.setMicSeatState(MicSeatBroadStatus.locked);
        }

        boolean isAudioMute = micSeatInfo.muteAudio;
        if (userInfo != null) {
            isAudioMute = isAudioMute || (userInfo.muteAudio == 1);
        }
        // 保持旧的方法兼容性
        seatView.setAudioMuteVisibility(isAudioMute ? View.VISIBLE : View.GONE);

        boolean isVideoMute = micSeatInfo.muteVideo;
        seatView.setVideoMuteVisibility(isVideoMute ? View.VISIBLE : View.GONE);

        // 新的麦位图标控制逻辑
        seatView.setAudioMuteNewVisibility(View.VISIBLE, isAudioMute);

        // 设置用户名称和头像
        String displayName = micSeatInfo.user.userName;
        if (TextUtils.isEmpty(displayName) && userInfo != null) {
            displayName = userInfo.userName;
        }

        if (!TextUtils.isEmpty(displayName)) {
            seatView.setTitleText(displayName);
        } else {
            // 如果用户名为空，显示默认文本而不是麦位编号
            seatView.setTitleText("用户");
        }

        seatView.setUserAvatarImageUrl(micSeatInfo.user.userAvatar);
        seatView.setChorusMicOwnerType(IMicSeatItemBroadView.ChorusType.None);
    }

    /** AUIMicSeatDialogBroadView.ActionDelegate implements. */
    @Override
    public boolean onClickSeat(int index, IMicSeatDialogBroadView dialogView) {
        AUIMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(index);
        if (seatInfo == null){ return false;}
        if(seatInfo.user != null){
            AUIUserInfo userInfo = userService.getUserInfo(seatInfo.user.userId);
            if (userInfo != null) {
                dialogView.setUserInfo(userInfo.userId);
                dialogView.setUserAvatar(userInfo.userAvatar);
                dialogView.setUserName(userInfo.userName);
            }
        }
        boolean isEmptySeat = (seatInfo.user == null || seatInfo.user.userId.length() == 0);
        boolean isCurrentUser = seatInfo.user != null && seatInfo.user.userId.equals(micSeatService.getRoomContext().currentUserInfo.userId);
        boolean isRoomOwner = micSeatService.getRoomContext().isRoomOwner(micSeatService.getChannelName());
        boolean inSeat = false;
        for (int i = 0; i <= 7; i++) {
            AUIMicSeatInfo info = micSeatService.getMicSeatInfo(i);
            if (info != null){
                if (info.user != null && info.user.userId.equals(micSeatService.getRoomContext().currentUserInfo.userId)) {
                    inSeat = true;
                    break;
                }
            }
        }

        // 计算实际的静音状态（包含房主静音和用户自己静音）
        boolean actualMuteState = seatInfo.muteAudio;
        if (!isEmptySeat && seatInfo.user != null) {
            AUIUserInfo userInfo = userService.getUserInfo(seatInfo.user.userId);
            if (userInfo != null) {
                actualMuteState = actualMuteState || (userInfo.muteAudio == 1);
            }
        }

        if (isRoomOwner) {
            if (isEmptySeat) {
                dialogView.addInvite((seatInfo.seatStatus != AUIMicSeatStatus.locked));
                dialogView.addMuteAudio(actualMuteState);
                dialogView.addCloseSeat((seatInfo.seatStatus == AUIMicSeatStatus.locked));
            } else {
                if (isCurrentUser) {
                    dialogView.addMuteAudio(actualMuteState);
                } else {
                    dialogView.addKickSeat();
                    dialogView.addMuteAudio(actualMuteState);
                    dialogView.addCloseSeat((seatInfo.seatStatus == AUIMicSeatStatus.locked));
                }
            }
        } else {
            if (isEmptySeat) {
                if (inSeat) {
                    return false;
                } else {
                    boolean seatUnLocked = seatInfo.seatStatus != AUIMicSeatStatus.locked;
                    if (seatUnLocked) {
                        dialogView.addEnterSeat(true);
                    } else {
                        return false;
                    }
                }
            } else {
                if (isCurrentUser) {
                    dialogView.addLeaveSeat();
                    dialogView.addMuteAudio(actualMuteState);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onClickEnterSeat(int index) {
        showEnterDialog(index);
    }

    public void showEnterDialog(int index) {
        auiAlertDialog = new AUIAlertBroadDialog(context);
        auiAlertDialog.setTitle(context.getString(R.string.voice_room_apply_action));
        auiAlertDialog.setMessage(context.getString(R.string.voice_room_apply_micSeat,index+1));
        auiAlertDialog.setPositiveButton(context.getString(R.string.voice_room_confirm), view -> {
            invitationService.sendApply(index, error -> {
                if (error == null){
                    Toast.makeText(context, R.string.voice_room_apply_send_success, Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context, R.string.voice_room_apply_send_failed, Toast.LENGTH_SHORT).show();
                }
            });
            auiAlertDialog.dismiss();
        });
        auiAlertDialog.show();
    }

    @Override
    public void onClickLeaveSeat(int index) {
        micSeatService.leaveSeat(null);
    }

    @Override
    public void onClickKickSeat(int index) {
        micSeatService.kickSeat(index, null);
    }

    @Override
    public void onClickCloseSeat(int index, boolean isClose) {
        micSeatService.closeSeat(index, isClose, null);
    }

    @Override
    public void onClickMuteAudio(int index, boolean mute) {
        // 获取当前用户信息
        String currentUserId = roomContext.currentUserInfo != null ? roomContext.currentUserInfo.userId : "";
        boolean isRoomOwner = roomContext.isRoomOwner(micSeatService.getChannelName());

        // 获取麦位信息
        AUIMicSeatInfo micSeatInfo = micSeatService.getMicSeatInfo(index);
        if (micSeatInfo == null) {
            return;
        }

        // 检查是否是自己的麦位
        boolean isOwnSeat = micSeatInfo.user != null &&
                currentUserId.equals(micSeatInfo.user.userId);

        // 权限检查逻辑
        if (!isRoomOwner) {
            // 非房主用户的权限检查

            if (!isOwnSeat) {
                // 不是自己的麦位，不允许操作
                Toast.makeText(context, "只能操作自己的麦位", Toast.LENGTH_SHORT).show();
                return;
            }

            // 如果是取消静音操作，需要检查麦位是否被房主静音
            // 关键修复：只有当麦位被房主静音时才阻止用户取消静音
            // micSeatInfo.muteAudio 表示房主对麦位的静音状态
            if (!mute && micSeatInfo.muteAudio) {
                // 麦位被房主静音，且用户想要取消静音，不允许
                Toast.makeText(context, "当前麦位被房主静音，无法取消静音", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 通过权限检查，执行静音操作
        // 注意：房主操作自己的麦位和操作他人麦位的逻辑不同
        if (isRoomOwner) {
            if (isOwnSeat) {
                // 房主操作自己的麦位：设置用户自己的静音状态
                // 这样不会触发"房主禁音"的提示
                userService.muteUserAudio(mute, null);
            } else {
                // 房主操作他人麦位：设置麦位静音状态（影响整个麦位）
                micSeatService.muteAudioSeat(index, mute, null);
            }
        } else {
            // 用户自己操作：设置用户自己的静音状态
            userService.muteUserAudio(mute, null);
        }
    }

    @Override
    public void onClickMuteVideo(int index, boolean mute) {
        micSeatService.muteVideoSeat(index, mute, null);
    }

    @Override
    public void onClickInvited(int index) {
        micSeatService.onClickInvited(index);
    }


    /** IAUIUserService.AUIUserRespDelegate implements. */
    @Override
    public void onRoomUserSnapshot(@NonNull String roomId, @Nullable List<AUIUserInfo> userList) {
        // 用户数据快照到达，需要延迟一下确保数据完全同步后再更新视图
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 延迟100ms确保用户数据完全同步到userService中
                mMainHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // update view
                        IMicSeatItemBroadView[] seatViewList = micSeatsView.getMicSeatItemViewList();
                        for (int seatIndex = 0; seatIndex < seatViewList.length; seatIndex++) {
                            updateSeatView(seatIndex);
                        }
                    }
                }, 100);
            }
        });
    }

    @Override
    public void onRoomUserEnter(@NonNull String roomId, @NonNull AUIUserInfo userInfo) {

    }

    @Override
    public void onRoomUserLeave(@NonNull String roomId, @NonNull AUIUserInfo userInfo) {

    }

    @Override
    public void onRoomUserUpdate(@NonNull String roomId, @NonNull AUIUserInfo userInfo) {
        // 当用户信息更新时，检查该用户是否在麦位上，如果在则更新麦位显示
        int micSeatIndex = micSeatService.getMicSeatIndex(userInfo.userId);
        if (micSeatIndex >= 0) {
            // 用户在麦位上，更新麦位视图以同步最新的用户信息
            updateSeatView(micSeatIndex);
        }
    }

    @Override
    public void onUserAudioMute(@NonNull String userId, boolean mute) {
        int micSeatIndex = micSeatService.getMicSeatIndex(userId);
        updateSeatView(micSeatIndex);
    }

    @Override
    public void onUserVideoMute(@NonNull String userId, boolean mute) {

    }


    @Override
    public void onUserOffline(int uid, int reason) {
        super.onUserOffline(uid, reason);
        AUILogger.Companion.logger().d("AUIMicSeatsBroadBinder", "onUserOffline >> uid=" + uid + ", reason=" + reason);
    }


    @Override
    public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
        for (AudioVolumeInfo speaker : speakers) {
            int uid = speaker.uid;
            if (uid == 0){// uid == 0 代表自己
                String userId = roomContext.currentUserInfo.userId;
                if (roomContext.isRoomOwner(userService.getChannelName())){
                    if (speaker.volume == 0) {
                        micSeatsView.stopRippleAnimation(0);
                    }else {
                        micSeatsView.startRippleAnimation(0);
                    }
                }else {
                    if (null != mVolumeMap && mVolumeMap.containsKey(userId)){
                        if (speaker.volume == 0) {
                            micSeatsView.stopRippleAnimation(mVolumeMap.get(userId));
                        }else {
                            micSeatsView.startRippleAnimation(mVolumeMap.get(userId));
                        }
                    }
                }
            }else {
                if (null != mVolumeMap && mVolumeMap.containsKey(String.valueOf(uid))){
                    if (speaker.volume == 0) {
                        micSeatsView.stopRippleAnimation(mVolumeMap.get(String.valueOf(uid)));
                    }else {
                        micSeatsView.startRippleAnimation(mVolumeMap.get(String.valueOf(uid)));
                    }
                }
            }
        }
    }

}
