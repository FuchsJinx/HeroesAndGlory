package com.HG.heroesglory.core.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "dialogs")
public class Dialog {
    @PrimaryKey
    @NonNull
    private String id;
    private String speakerName;
    private String speakerImageUrl;
    private String text;
    private String endText;
    private String locationId;
    private String questId;
    private boolean isInitial;

    public Dialog() {}

    public Dialog(@NonNull String id, String speakerName, String text) {
        this.id = id;
        this.speakerName = speakerName;
        this.text = text;
        this.isInitial = false;
    }

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getSpeakerName() { return speakerName; }
    public void setSpeakerName(String speakerName) { this.speakerName = speakerName; }

    public String getSpeakerImageUrl() { return speakerImageUrl; }
    public void setSpeakerImageUrl(String speakerImageUrl) { this.speakerImageUrl = speakerImageUrl; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getEndText() { return endText; }
    public void setEndText(String endText) { this.endText = endText; }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }

    public String getQuestId() { return questId; }
    public void setQuestId(String questId) { this.questId = questId; }

    public boolean isInitial() { return isInitial; }
    public void setInitial(boolean initial) { isInitial = initial; }
}