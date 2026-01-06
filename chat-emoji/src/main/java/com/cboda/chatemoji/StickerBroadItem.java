package com.cboda.chatemoji;


public class StickerBroadItem {
    private String category;//类别名
    private String name;

    public StickerBroadItem(String category, String name) {
        this.category = category;
        this.name = name;
    }

    public String getIdentifier() {
        return category + "/" + name;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof StickerBroadItem) {
            StickerBroadItem item = (StickerBroadItem) obj;
            return item.getCategory().equals(category) && item.getName().equals(name);
        }
        return false;
    }
}
