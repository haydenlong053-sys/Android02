package com.cboda.agora.ui.ui.chatBottomBar.impl;


import com.cboda.agora.ui.R;
import com.cboda.agora.ui.ui.chatBottomBar.AUIChatBottomBarIcon;
import com.cboda.agora.ui.ui.chatBottomBar.AUIChatBottomBarIconType;

public class AUIDefaultEmojiBroadData {
    
    private static String[] emojis = new String[]{
        AUIEmojiBroadUtils.ee_1,
        AUIEmojiBroadUtils.ee_2,
        AUIEmojiBroadUtils.ee_3,
        AUIEmojiBroadUtils.ee_4,
        AUIEmojiBroadUtils.ee_5,
        AUIEmojiBroadUtils.ee_6,
        AUIEmojiBroadUtils.ee_7,
        AUIEmojiBroadUtils.ee_8,
        AUIEmojiBroadUtils.ee_9,
        AUIEmojiBroadUtils.ee_10,
        AUIEmojiBroadUtils.ee_11,
        AUIEmojiBroadUtils.ee_12,
        AUIEmojiBroadUtils.ee_13,
        AUIEmojiBroadUtils.ee_14,
        AUIEmojiBroadUtils.ee_15,
        AUIEmojiBroadUtils.ee_16,
        AUIEmojiBroadUtils.ee_17,
        AUIEmojiBroadUtils.ee_18,
        AUIEmojiBroadUtils.ee_19,
        AUIEmojiBroadUtils.ee_20,
        AUIEmojiBroadUtils.ee_21,
        AUIEmojiBroadUtils.ee_22,
        AUIEmojiBroadUtils.ee_23,
        AUIEmojiBroadUtils.ee_24,
        AUIEmojiBroadUtils.ee_25,
        AUIEmojiBroadUtils.ee_26,
        AUIEmojiBroadUtils.ee_27,
        AUIEmojiBroadUtils.ee_28,
        AUIEmojiBroadUtils.ee_29,
        AUIEmojiBroadUtils.ee_30,
        AUIEmojiBroadUtils.ee_31,
        AUIEmojiBroadUtils.ee_32,
        AUIEmojiBroadUtils.ee_33,
        AUIEmojiBroadUtils.ee_34,
        AUIEmojiBroadUtils.ee_35,
        AUIEmojiBroadUtils.ee_36,
        AUIEmojiBroadUtils.ee_37,
        AUIEmojiBroadUtils.ee_38,
        AUIEmojiBroadUtils.ee_39,
        AUIEmojiBroadUtils.ee_40,
        AUIEmojiBroadUtils.ee_41,
        AUIEmojiBroadUtils.ee_42,
        AUIEmojiBroadUtils.ee_43,
        AUIEmojiBroadUtils.ee_44,
        AUIEmojiBroadUtils.ee_45,
        AUIEmojiBroadUtils.ee_46,
        AUIEmojiBroadUtils.ee_47,
        AUIEmojiBroadUtils.ee_48,
        AUIEmojiBroadUtils.ee_49,
        AUIEmojiBroadUtils.ee_50,
        AUIEmojiBroadUtils.ee_51,
        AUIEmojiBroadUtils.ee_52,
    };
    
    private static int[] icons = new int[]{
        R.drawable.voice_ee_broad_1,
        R.drawable.voice_ee_broad_2,
        R.drawable.voice_ee_broad_3,
        R.drawable.voice_ee_broad_4,
        R.drawable.voice_ee_broad_5,
        R.drawable.voice_ee_broad_6,
        R.drawable.voice_ee_broad_7,
        R.drawable.voice_ee_broad_8,
        R.drawable.voice_ee_broad_9,
        R.drawable.voice_ee_broad_10,
        R.drawable.voice_ee_broad_11,
        R.drawable.voice_ee_broad_12,
        R.drawable.voice_ee_broad_13,
        R.drawable.voice_ee_broad_14,
        R.drawable.voice_ee_broad_15,
        R.drawable.voice_ee_broad_16,
        R.drawable.voice_ee_broad_17,
        R.drawable.voice_ee_broad_18,
        R.drawable.voice_ee_broad_19,
        R.drawable.voice_ee_broad_20,
        R.drawable.voice_ee_broad_21,
        R.drawable.voice_ee_broad_22,
        R.drawable.voice_ee_broad_23,
        R.drawable.voice_ee_broad_24,
        R.drawable.voice_ee_broad_25,
        R.drawable.voice_ee_broad_26,
        R.drawable.voice_ee_broad_27,
        R.drawable.voice_ee_broad_28,
        R.drawable.voice_ee_broad_29,
        R.drawable.voice_ee_broad_30,
        R.drawable.voice_ee_broad_31,
        R.drawable.voice_ee_broad_32,
        R.drawable.voice_ee_broad_33,
        R.drawable.voice_ee_broad_34,
        R.drawable.voice_ee_broad_35,
        R.drawable.voice_ee_broad_36,
        R.drawable.voice_ee_broad_37,
        R.drawable.voice_ee_broad_38,
        R.drawable.voice_ee_broad_39,
        R.drawable.voice_ee_broad_40,
        R.drawable.voice_ee_broad_41,
        R.drawable.voice_ee_broad_42,
        R.drawable.voice_ee_broad_43,
        R.drawable.voice_ee_broad_44,
        R.drawable.voice_ee_broad_45,
        R.drawable.voice_ee_broad_46,
        R.drawable.voice_ee_broad_47,
        R.drawable.voice_ee_broad_48,
        R.drawable.voice_ee_broad_49,
        R.drawable.voice_ee_broad_50,
        R.drawable.voice_ee_broad_51,
        R.drawable.voice_ee_broad_52,

    };
    
    
    private static final AUIChatBottomBarIcon[] DATA = createData();
    
    private static AUIChatBottomBarIcon[] createData(){
        AUIChatBottomBarIcon[] datas = new AUIChatBottomBarIcon[icons.length];
        for(int i = 0; i < icons.length; i++){
            datas[i] = new AUIChatBottomBarIcon(icons[i], "", emojis[i], AUIChatBottomBarIconType.NORMAL);
        }
        return datas;
    }
    
    public static AUIChatBottomBarIcon[] getData(){
        return DATA;
    }
}
