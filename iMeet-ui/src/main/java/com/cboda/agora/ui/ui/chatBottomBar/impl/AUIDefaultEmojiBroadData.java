package com.cboda.agora.ui.ui.chatBottomBar.impl;


import com.cboda.agora.ui.R;
import com.cboda.agora.ui.chatBottomBar.AUIChatBottomBarIcon;
import com.cboda.agora.ui.chatBottomBar.AUIChatBottomBarIconType;

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
        R.drawable.voice_ee_1,
        R.drawable.voice_ee_2,
        R.drawable.voice_ee_3,
        R.drawable.voice_ee_4,
        R.drawable.voice_ee_5,
        R.drawable.voice_ee_6,
        R.drawable.voice_ee_7,
        R.drawable.voice_ee_8,
        R.drawable.voice_ee_9,
        R.drawable.voice_ee_10,
        R.drawable.voice_ee_11,
        R.drawable.voice_ee_12,
        R.drawable.voice_ee_13,
        R.drawable.voice_ee_14,
        R.drawable.voice_ee_15,
        R.drawable.voice_ee_16,
        R.drawable.voice_ee_17,
        R.drawable.voice_ee_18,
        R.drawable.voice_ee_19,
        R.drawable.voice_ee_20,
        R.drawable.voice_ee_21,
        R.drawable.voice_ee_22,
        R.drawable.voice_ee_23,
        R.drawable.voice_ee_24,
        R.drawable.voice_ee_25,
        R.drawable.voice_ee_26,
        R.drawable.voice_ee_27,
        R.drawable.voice_ee_28,
        R.drawable.voice_ee_29,
        R.drawable.voice_ee_30,
        R.drawable.voice_ee_31,
        R.drawable.voice_ee_32,
        R.drawable.voice_ee_33,
        R.drawable.voice_ee_34,
        R.drawable.voice_ee_35,
        R.drawable.voice_ee_36,
        R.drawable.voice_ee_37,
        R.drawable.voice_ee_38,
        R.drawable.voice_ee_39,
        R.drawable.voice_ee_40,
        R.drawable.voice_ee_41,
        R.drawable.voice_ee_42,
        R.drawable.voice_ee_43,
        R.drawable.voice_ee_44,
        R.drawable.voice_ee_45,
        R.drawable.voice_ee_46,
        R.drawable.voice_ee_47,
        R.drawable.voice_ee_48,
        R.drawable.voice_ee_49,
        R.drawable.voice_ee_50,
        R.drawable.voice_ee_51,
        R.drawable.voice_ee_52,

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
