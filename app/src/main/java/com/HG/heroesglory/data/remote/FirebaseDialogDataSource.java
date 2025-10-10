package com.HG.heroesglory.data.remote;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.HG.heroesglory.core.entities.Dialog;
import com.HG.heroesglory.core.entities.DialogChoice;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDialogDataSource {
    private final FirebaseFirestore firestore;
    private final CollectionReference dialogsCollection;

    public FirebaseDialogDataSource() {
        this.firestore = FirebaseFirestore.getInstance();
        this.dialogsCollection = firestore.collection("dialogs");
    }

    public interface DialogCallback {
        void onSuccess(Dialog dialog);
        void onError(String error);
    }

    public interface ChoicesCallback {
        void onSuccess(List<DialogChoice> choices);
        void onError(String error);
    }

    public interface SaveCallback {
        void onSuccess();
        void onError(String error);
    }

    public void getDialog(String dialogId, DialogCallback callback) {
        dialogsCollection.document(dialogId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Dialog dialog = convertToDialog(document);
                            callback.onSuccess(dialog);
                        } else {
                            callback.onError("Dialog not found");
                        }
                    } else {
                        callback.onError("Failed to load dialog: " + task.getException().getMessage());
                    }
                });
    }

    public void getChoicesForDialog(String dialogId, ChoicesCallback callback) {
        dialogsCollection.document(dialogId)
                .collection("choices")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DialogChoice> choices = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DialogChoice choice = convertToChoice(document);
                            choices.add(choice);
                        }
                        callback.onSuccess(choices);
                    } else {
                        callback.onError("Failed to load choices: " + task.getException().getMessage());
                    }
                });
    }

    public void getDialogsByLocation(String locationId, DialogCallback callback) {
        dialogsCollection.whereEqualTo("locationId", locationId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Dialog> dialogs = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Dialog dialog = convertToDialog(document);
                            dialogs.add(dialog);
                        }
                        // Для упрощения возвращаем первый диалог или null
                        callback.onSuccess(dialogs.isEmpty() ? null : dialogs.get(0));
                    } else {
                        callback.onError("Failed to load location dialogs: " + task.getException().getMessage());
                    }
                });
    }

    private Dialog convertToDialog(DocumentSnapshot document) {
        Dialog dialog = new Dialog();
        dialog.setId(document.getId());
        dialog.setSpeakerName(document.getString("speakerName"));
        dialog.setSpeakerImageUrl(document.getString("speakerImageUrl"));
        dialog.setText(document.getString("text"));
        dialog.setEndText(document.getString("endText"));
        dialog.setLocationId(document.getString("locationId"));
        dialog.setQuestId(document.getString("questId"));

        Boolean isInitial = document.getBoolean("isInitial");
        dialog.setInitial(isInitial != null ? isInitial : false);

        return dialog;
    }

    private DialogChoice convertToChoice(QueryDocumentSnapshot document) {
        DialogChoice choice = new DialogChoice();
        choice.setId(document.getId());
        choice.setDialogId(document.getString("dialogId"));
        choice.setText(document.getString("text"));
        choice.setResponseText(document.getString("responseText"));
        choice.setNextDialogId(document.getString("nextDialogId"));
        choice.setRequiredSkill(document.getString("requiredSkill"));

        Long requiredLevel = document.getLong("requiredLevel");
        choice.setRequiredLevel(requiredLevel != null ? requiredLevel.intValue() : 0);

        choice.setRequiredItem(document.getString("requiredItem"));

        return choice;
    }
}