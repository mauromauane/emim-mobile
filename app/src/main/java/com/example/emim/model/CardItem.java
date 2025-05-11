package com.example.emim.model;

public class CardItem {
    private String title;
    private int iconRes;

    public CardItem(String title, int iconRes) {
        this.title = title;
        this.iconRes = iconRes;
    }

    public String getTitle() {
        return title;
    }

    public int getIconRes() {
        return iconRes;
    }
}
