/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.ext;

import static com.cboda.baselibrary.utils.BugLyUtilKt.bgReport;
import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.cboda.baselibrary.utils.AppPermissionBroadUtils;
//import com.cboda.imgselect.ImagePicker;
//import com.cboda.imgselect.bean.ImageItem;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.cboda.chatui.R;
import com.cboda.chatui.annotation.ExtContextMenuItem;
import com.cboda.chatui.bridge.GlideBroadEngine;
import com.cboda.chatui.conversation.ext.core.ConversationExt;
import com.cboda.chatui.third.utils.ImageUtils;
import com.cboda.chatui.third.utils.UIUtils;
import cn.wildfirechat.message.TypingMessageContent;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;

public class ImageExt extends ConversationExt {

    /**
     * @param containerView 扩展view的container
     * @param conversation
     */
    @ExtContextMenuItem
    public void pickImage(View containerView, Conversation conversation) {
        AppPermissionBroadUtils.INSTANCE.applyAndCheckReadStoragePermission(activity, () -> {
            openGalley();
            return null;
        });
    }


    private void openGalley() {
        GlideBroadEngine glideEngine = GlideBroadEngine.createGlideEngine();
//        PictureSelectorStyle paramStyle = new PictureSelectorStyle();
//        BottomNavBarStyle bottomNavBarStyle=paramStyle.getBottomBarStyle();
//        bottomNavBarStyle.setBottomNarBarHeight(dp2px(BarUtils.getNavBarHeight()));
//        paramStyle.setBottomBarStyle(bottomNavBarStyle);
//                // 底部栏背景色
//                .setBottomBarBgColor(ContextCompat.getColor(this, R.color.white))
//                // 预览按钮文字颜色
//                .setPreviewTextColor(ContextCompat.getColor(this, R.color.black))
//                // 预览按钮不可用时颜色
//                .setPreviewTextDisabledColor(ContextCompat.getColor(this, R.color.gray))
//                // 下一步/完成按钮背景色
//                .setCompleteBgColor(ContextCompat.getColor(this, R.color.blue))
//                // 下一步/完成按钮文字颜色
//                .setCompleteTextColor(ContextCompat.getColor(this, R.color.white))
//                // 下一步/完成按钮不可用时背景色
//                .setCompleteDisabledBgColor(ContextCompat.getColor(this, R.color.light_gray))
//                // 下一步/完成按钮不可用时文字颜色
//                .setCompleteDisabledTextColor(ContextCompat.getColor(this, R.color.dark_gray));
        PictureSelector.create(activity).openGallery(SelectMimeType.ofAll()).
//        setSelectorUIStyle(paramStyle).
        setImageEngine(glideEngine).
                setMaxSelectNum(9).
                isPreviewVideo(true).
                setFilterVideoMinSecond(2).
                isMaxSelectEnabledMask(true).

        setImageSpanCount(3).
                isDisplayCamera(false).forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        if (result != null) {
                            for (LocalMedia imageItem : result) {
//                                Log.e("野火","选择相册路径---> "+imageItem.getRealPath());
                                if (imageItem.getRealPath().endsWith(".mp4")) {
                                    messageViewModel.sendVideoMsg(conversation, new File(imageItem.getRealPath()));
                                } else {
                                    boolean isGif = isGifFile(imageItem.getRealPath());
                                    if (isGif) {
                                        UIUtils.postTaskSafely(() -> messageViewModel.sendStickerMsg(conversation, imageItem.getRealPath(), null));
                                        continue;
                                    }
                                    File imageFileThumb;
                                    File imageFileSource = null;

                                    if (!imageItem.isCompressed()) {
                                        imageFileSource = ImageUtils.compressImage(imageItem.getRealPath());
                                    }
                                    imageFileSource = imageFileSource == null ? new File(imageItem.getRealPath()) : imageFileSource;
                                    imageFileThumb = ImageUtils.genThumbImgFile(imageItem.getRealPath());
                                    if (imageFileThumb == null) {
                                        Log.e("野火", "创建缩略图失败--->");
                                        return;
                                    }
                                    File finalImageFileSource = imageFileSource;
                                    UIUtils.postTaskSafely(() -> messageViewModel.sendImgMsg(conversation, imageFileThumb, finalImageFileSource));

                                }
                            }
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });

//        Intent intent = ImagePicker.picker().showCamera(true).enableMultiMode(9).buildPickIntent(activity);
//        startActivityForResult(intent, 100);
        TypingMessageContent content = new TypingMessageContent(TypingMessageContent.TYPING_CAMERA);
        messageViewModel.sendMessage(conversation, content);
    }

    private boolean isGifFile(String file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            int[] flags = new int[5];
            flags[0] = inputStream.read();
            flags[1] = inputStream.read();
            flags[2] = inputStream.read();
            flags[3] = inputStream.read();
            inputStream.skip(inputStream.available() - 1);
            flags[4] = inputStream.read();
            inputStream.close();
            return flags[0] == 71 && flags[1] == 73 && flags[2] == 70 && flags[3] == 56 && flags[4] == 0x3B;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == Activity.RESULT_OK) {
//            if (data != null) {
//
//                ChatManager.Instance().getWorkHandler().post(() -> {
//                    //是否发送原图
//                    boolean compress = data.getBooleanExtra(ImagePicker.EXTRA_COMPRESS, true);
//                    ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
//                    for (ImageItem imageItem : images) {
//                        boolean isGif = isGifFile(imageItem.path);
//                        if (isGif) {
//                            UIUtils.postTaskSafely(() -> messageViewModel.sendStickerMsg(conversation, imageItem.path, null));
//                            continue;
//                        }
//                        File imageFileThumb;
//                        File imageFileSource = null;
//                        // FIXME: 2018/11/29 压缩, 不是发原图的时候，大图需要进行压缩
//                        if (compress) {
//                            imageFileSource = ImageUtils.compressImage(imageItem.path);
//                        }
//                        imageFileSource = imageFileSource == null ? new File(imageItem.path) : imageFileSource;
////                    if (isOrig) {
////                    imageFileSource = new File(imageItem.path);
//                        imageFileThumb = ImageUtils.genThumbImgFile(imageItem.path);
//                        if (imageFileThumb == null) {
//                            Log.e("ImageExt", "gen image thumb fail");
//                            return;
//                        }
////                    } else {
////                        //压缩图片
////                        // TODO  压缩的有问题
////                        imageFileSource = ImageUtils.genThumbImgFileEx(imageItem.path);
////                        //imageFileThumb = ImageUtils.genThumbImgFile(imageFileSource.getAbsolutePath());
////                        imageFileThumb = imageFileSource;
////                    }
////                            messageViewModel.sendImgMsg(conversation, imageFileThumb, imageFileSource);
//                        File finalImageFileSource = imageFileSource;
//                        UIUtils.postTaskSafely(() -> messageViewModel.sendImgMsg(conversation, imageFileThumb, finalImageFileSource));
//
//                    }
//
//                });
//
//            }
//        }
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_pic;
    }

    @Override
    public String title(Context context) {
        return "相册";
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return title(context);
    }
}
