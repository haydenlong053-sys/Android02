package com.freddy.chat.folder;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;

import com.cboda.intelligent.R;

/**
 * 会话分组本地图标（服务端不存）；0–6 可选，7 为「更多」占位。
 */
public final class ConvFolderIcons {

    public static final int COUNT_SELECTABLE = 7;
    public static final int TYPE_MORE = 7;

    private ConvFolderIcons() {
    }

    @DrawableRes
    public static int iconRes(int type) {
        switch (clamp(type)) {
            case 1:
                return R.drawable.ic_conv_folder_work;
            case 2:
                return R.drawable.ic_conv_folder_people;
            case 3:
                return R.drawable.ic_conv_folder_home;
            case 4:
                return R.drawable.ic_conv_folder_heart;
            case 5:
                return R.drawable.ic_conv_folder_game;
            case 6:
                return R.drawable.ic_conv_folder_flag;
            case TYPE_MORE:
                return R.drawable.ic_conv_folder_more;
            case 0:
            default:
                return R.drawable.ic_conv_folder_star;
        }
    }

    @ColorInt
    public static int bgColor(int type) {
        switch (clamp(type)) {
            case 1:
                return 0xFFE8F1FF;
            case 2:
                return 0xFFE6F7EF;
            case 3:
                return 0xFFFFF0E6;
            case 4:
                return 0xFFFFE8EC;
            case 5:
                return 0xFFE6F9FB;
            case 6:
                return 0xFFFFE9E9;
            case TYPE_MORE:
                return 0xFFF0F0F0;
            case 0:
            default:
                return 0xFFF0E8FF;
        }
    }

    @ColorInt
    public static int tintColor(int type) {
        switch (clamp(type)) {
            case 1:
                return 0xFF3B82F6;
            case 2:
                return 0xFF22C55E;
            case 3:
                return 0xFFF97316;
            case 4:
                return 0xFFEF4444;
            case 5:
                return 0xFF06B6D4;
            case 6:
                return 0xFFDC2626;
            case TYPE_MORE:
                return 0xFF9CA3AF;
            case 0:
            default:
                return 0xFF8B5CF6;
        }
    }

    private static int clamp(int type) {
        if (type < 0 || type > TYPE_MORE) {
            return 0;
        }
        return type;
    }
}
