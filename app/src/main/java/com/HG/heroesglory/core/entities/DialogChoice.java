package com.HG.heroesglory.core.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "dialog_choices",
        foreignKeys = @ForeignKey(
                entity = Dialog.class,
                parentColumns = "id",
                childColumns = "dialogId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("dialogId")}
)
public class DialogChoice {
    @PrimaryKey
    @NonNull
    private String id;
    private String dialogId;
    private String text;
    private String responseText;
    private String nextDialogId;
    private String requiredSkill;
    private int requiredLevel;
    private String requiredItem;

    public DialogChoice() {}

    public DialogChoice(@NonNull String id, String dialogId, String text) {
        this.id = id;
        this.dialogId = dialogId;
        this.text = text;
    }

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getDialogId() { return dialogId; }
    public void setDialogId(String dialogId) { this.dialogId = dialogId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getResponseText() { return responseText; }
    public void setResponseText(String responseText) { this.responseText = responseText; }

    public String getNextDialogId() { return nextDialogId; }
    public void setNextDialogId(String nextDialogId) { this.nextDialogId = nextDialogId; }

    public String getRequiredSkill() { return requiredSkill; }
    public void setRequiredSkill(String requiredSkill) { this.requiredSkill = requiredSkill; }

    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }

    public String getRequiredItem() { return requiredItem; }
    public void setRequiredItem(String requiredItem) { this.requiredItem = requiredItem; }
}