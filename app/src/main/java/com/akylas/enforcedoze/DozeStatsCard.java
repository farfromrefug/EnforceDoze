package com.akylas.enforcedoze;

import android.graphics.drawable.Drawable;

/**
 * Data model for a Doze stats card
 */
public class DozeStatsCard {
    private String title;
    private String description;
    private Drawable drawable;

    public DozeStatsCard(String title, String description, Drawable drawable) {
        this.title = title;
        this.description = description;
        this.drawable = drawable;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Drawable getDrawable() {
        return drawable;
    }
}
