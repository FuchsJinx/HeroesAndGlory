package com.HG.heroesglory.presentation.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.HG.heroesglory.R;
import com.bumptech.glide.Glide;

public class ItemDetailsDialog extends DialogFragment {

    private static final String ARG_ITEM_ID = "item_id";
    private static final String ARG_ITEM_NAME = "item_name";
    private static final String ARG_ITEM_TYPE = "item_type";
    private static final String ARG_ITEM_DESCRIPTION = "item_description";
    private static final String ARG_ITEM_IMAGE_URL = "item_image_url";
    private static final String ARG_ITEM_RARITY = "item_rarity";
    private static final String ARG_ITEM_QUANTITY = "item_quantity";
    private static final String ARG_ITEM_WEIGHT = "item_weight";
    private static final String ARG_ITEM_VALUE = "item_value";

    private ImageView itemImage;
    private TextView itemNameText;
    private TextView itemTypeText;
    private TextView itemRarityText;
    private TextView itemDescriptionText;
    private TextView itemStatsText;
    private TextView statsTitle;
    private TextView itemWeightText;
    private TextView itemValueText;
    private TextView itemQuantityText;
    private Button closeButton;

    public static ItemDetailsDialog newInstance(String itemId, String itemName, String itemType,
                                                String itemDescription, String itemImageUrl,
                                                String itemRarity, int quantity, double weight,
                                                int value) {
        ItemDetailsDialog dialog = new ItemDetailsDialog();
        Bundle args = new Bundle();
        args.putString(ARG_ITEM_ID, itemId);
        args.putString(ARG_ITEM_NAME, itemName);
        args.putString(ARG_ITEM_TYPE, itemType);
        args.putString(ARG_ITEM_DESCRIPTION, itemDescription);
        args.putString(ARG_ITEM_IMAGE_URL, itemImageUrl);
        args.putString(ARG_ITEM_RARITY, itemRarity);
        args.putInt(ARG_ITEM_QUANTITY, quantity);
        args.putDouble(ARG_ITEM_WEIGHT, weight);
        args.putInt(ARG_ITEM_VALUE, value);
        dialog.setArguments(args);
        return dialog;
    }

    // Альтернативная версия для работы с объектом Item
    public static ItemDetailsDialog newInstance(com.HG.heroesglory.core.entities.Item item) {
        ItemDetailsDialog dialog = new ItemDetailsDialog();
        Bundle args = new Bundle();
        args.putString(ARG_ITEM_ID, item.getId());
        args.putString(ARG_ITEM_NAME, item.getName());
        args.putString(ARG_ITEM_TYPE, item.getType());
        args.putString(ARG_ITEM_DESCRIPTION, item.getDescription());
        args.putString(ARG_ITEM_IMAGE_URL, item.getImageUrl());
        args.putString(ARG_ITEM_RARITY, item.getRarity());
        args.putInt(ARG_ITEM_QUANTITY, item.getQuantity() != -1 ? item.getQuantity() : 1);
        args.putDouble(ARG_ITEM_WEIGHT, item.getWeight() != -1 ? item.getWeight() : 0.0);
        args.putInt(ARG_ITEM_VALUE, item.getValue() != -1 ? item.getValue() : 0);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Item Details");
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_item_details, container, false);

        initViews(view);
        loadItemData();
        setupCloseButton();

        return view;
    }

    private void initViews(View view) {
        itemImage = view.findViewById(R.id.itemImage);
        itemNameText = view.findViewById(R.id.itemNameText);
        itemTypeText = view.findViewById(R.id.itemTypeText);
        itemRarityText = view.findViewById(R.id.itemRarityText);
        itemDescriptionText = view.findViewById(R.id.itemDescriptionText);
        itemStatsText = view.findViewById(R.id.itemStatsText);
        statsTitle = view.findViewById(R.id.statsTitle);
        itemWeightText = view.findViewById(R.id.itemWeightText);
        itemValueText = view.findViewById(R.id.itemValueText);
        itemQuantityText = view.findViewById(R.id.itemQuantityText);
        closeButton = view.findViewById(R.id.closeButton);
    }

    private void loadItemData() {
        Bundle args = getArguments();
        if (args == null) return;

        // Основная информация
        String itemName = args.getString(ARG_ITEM_NAME, "Unknown Item");
        String itemType = args.getString(ARG_ITEM_TYPE, "ITEM");
        String itemDescription = args.getString(ARG_ITEM_DESCRIPTION, "No description available");
        String itemRarity = args.getString(ARG_ITEM_RARITY, "COMMON");
        String imageUrl = args.getString(ARG_ITEM_IMAGE_URL);

        // Числовые значения
        int quantity = args.getInt(ARG_ITEM_QUANTITY, 1);
        double weight = args.getDouble(ARG_ITEM_WEIGHT, 0.0);
        int value = args.getInt(ARG_ITEM_VALUE, 0);

        // Устанавливаем значения
        itemNameText.setText(itemName);
        itemTypeText.setText(getItemTypeDisplayName(itemType));
        itemRarityText.setText(getRarityDisplayName(itemRarity));
        itemDescriptionText.setText(itemDescription);

        // Устанавливаем цвет редкости
        setRarityColor(itemRarityText, itemRarity);

        // Загрузка изображения
        loadItemImage(imageUrl, itemType);

        // Детали
        itemWeightText.setText(String.format("%.1f kg", weight));
        itemValueText.setText(String.format("%d gold", value));
        itemQuantityText.setText(String.valueOf(quantity));

        // Статистика (можно расширить для разных типов предметов)
        setupItemStats(itemType);
    }

    private void loadItemImage(String imageUrl, String itemType) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(getPlaceholderForType(itemType))
                    .error(getPlaceholderForType(itemType))
                    .into(itemImage);
        } else {
            itemImage.setImageResource(getPlaceholderForType(itemType));
        }
    }

    private int getPlaceholderForType(String itemType) {
        switch (itemType) {
            case "WEAPON":
                return R.drawable.item_sword;
            case "ARMOR":
                return R.drawable.item_armor;
            case "CONSUMABLE":
                return R.drawable.item_potion;
            case "ACCESSORY":
                return R.drawable.item_ring;
            case "SCROLL":
                return R.drawable.item_scroll;
            case "KEY":
                return R.drawable.item_key;
            case "GOLD":
                return R.drawable.item_gold;
            case "QUEST":
                return R.drawable.item_quest;
            default:
                return R.drawable.item_default;
        }
    }

    private void setupItemStats(String itemType) {
        // Здесь можно добавить логику для отображения статистики по типам предметов
        String statsText = "No special properties";

        switch (itemType) {
            case "WEAPON":
                statsText = "• One-handed weapon\n• Base damage: 1d6\n• Versatile combat use";
                break;
            case "ARMOR":
                statsText = "• Light armor\n• Provides basic protection\n• No movement penalty";
                break;
            case "CONSUMABLE":
                statsText = "• Restores 10 hit points\n• Can be used in combat\n• Single use";
                break;
            case "ACCESSORY":
                statsText = "• +1 to all saving throws\n• Magical enhancement\n• Does not require attunement";
                break;
        }

        if (!statsText.equals("No special properties")) {
            statsTitle.setVisibility(View.VISIBLE);
        }

        itemStatsText.setText(statsText);
    }

    private String getItemTypeDisplayName(String type) {
        switch (type) {
            case "WEAPON": return "Weapon";
            case "ARMOR": return "Armor";
            case "CONSUMABLE": return "Consumable";
            case "ACCESSORY": return "Accessory";
            case "UTILITY": return "Utility";
            case "SCROLL": return "Scroll";
            case "KEY": return "Key";
            case "GOLD": return "Gold";
            case "QUEST": return "Quest Item";
            default: return "Item";
        }
    }

    private String getRarityDisplayName(String rarity) {
        switch (rarity) {
            case "COMMON": return "Common";
            case "UNCOMMON": return "Uncommon";
            case "RARE": return "Rare";
            case "EPIC": return "Epic";
            case "LEGENDARY": return "Legendary";
            case "ARTIFACT": return "Artifact";
            default: return "Common";
        }
    }

    private void setRarityColor(TextView textView, String rarity) {
        int colorRes;
        switch (rarity) {
            case "UNCOMMON":
                colorRes = R.color.rarity_uncommon;
                break;
            case "RARE":
                colorRes = R.color.rarity_rare;
                break;
            case "EPIC":
                colorRes = R.color.rarity_epic;
                break;
            case "LEGENDARY":
                colorRes = R.color.rarity_legendary;
                break;
            case "ARTIFACT":
                colorRes = R.color.rarity_artifact;
                break;
            default:
                colorRes = R.color.rarity_common;
                break;
        }

        if (getContext() != null) {
            textView.setTextColor(getResources().getColor(colorRes));
        }
    }

    private void setupCloseButton() {
        closeButton.setOnClickListener(v -> dismiss());
    }
}