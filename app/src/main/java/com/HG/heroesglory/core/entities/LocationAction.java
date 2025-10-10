package com.HG.heroesglory.core.entities;

public class LocationAction {
    private String actionType;
    private String title;
    private String description;
    private String targetId;
    private int iconRes;

    public LocationAction(String actionType, String title, String description, String targetId, int iconRes) {
        this.actionType = actionType;
        this.title = title;
        this.description = description;
        this.targetId = targetId;
        this.iconRes = iconRes;
    }

    // Getters
    public String getActionType() { return actionType; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTargetId() { return targetId; }
    public int getIconRes() { return iconRes; }
}