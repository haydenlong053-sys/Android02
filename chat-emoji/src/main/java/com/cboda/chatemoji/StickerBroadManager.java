package com.cboda.chatemoji;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSDN_LQR
 * 贴图管理类
 */
public class StickerBroadManager {

    private static StickerBroadManager instance;

    //数据源
    private List<StickerBroadCategory> stickerCategories = new ArrayList<>();
    private Map<String, StickerBroadCategory> stickerCategoryMap = new HashMap<>();
//    private Map<String, Integer> stickerOrder = new HashMap<>();

    public static StickerBroadManager getInstance() {
        if (instance == null) {
            synchronized (StickerBroadManager.class) {
                if (instance == null) {
                    instance = new StickerBroadManager();
                }
            }
        }
        return instance;
    }

    public StickerBroadManager() {
//        initStickerOrder();
        loadStickerCategory();
    }

    private void loadStickerCategory() {
        File stickerDir = new File(LQREmotionBroadKit.getStickerPath());
        if (stickerDir.exists()) {
            File[] files = stickerDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                //当前的目录下同名的有文件和文件夹，只需要其中的一个取其名
                if (file.isDirectory()) {
                    String name = file.getName();
                    StickerBroadCategory category = new StickerBroadCategory(name, name, true, i);
                    stickerCategories.add(category);
                    stickerCategoryMap.put(name, category);
                }
            }

            //排序
            Collections.sort(stickerCategories, new Comparator<StickerBroadCategory>() {
                @Override
                public int compare(StickerBroadCategory o1, StickerBroadCategory o2) {
                    return o1.getOrder() - o2.getOrder();
                }
            });
        }
    }

    public synchronized List<StickerBroadCategory> getStickerCategories() {
        return stickerCategories;
    }

    public synchronized StickerBroadCategory getCategory(String name) {
        return stickerCategoryMap.get(name);
    }

    public String getStickerBitmapUri(String categoryName, String stickerName) {
        String path = getStickerBitmapPath(categoryName, stickerName);
        return "file://" + path;
    }

    @Nullable
    public String getStickerBitmapPath(String categoryName, String stickerName) {
        StickerBroadManager manager = StickerBroadManager.getInstance();
        StickerBroadCategory category = manager.getCategory(categoryName);
        if (category == null) {
            return null;
        }
        return LQREmotionBroadKit.getStickerPath() + File.separator + category.getName() + File.separator + stickerName;
    }


}
