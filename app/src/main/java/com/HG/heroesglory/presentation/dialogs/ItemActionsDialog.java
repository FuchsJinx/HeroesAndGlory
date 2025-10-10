package com.HG.heroesglory.presentation.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Item;

public class ItemActionsDialog extends DialogFragment {

    private static final String ARG_ITEM = "item";

    private Item item;
    private OnItemActionListener itemActionListener;

    public static ItemActionsDialog newInstance(Item item) {
        ItemActionsDialog dialog = new ItemActionsDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ITEM, item);
        dialog.setArguments(args);
        return dialog;
    }

    public interface OnItemActionListener {
        void onEquip(Item item);
        void onUse(Item item);
        void onDrop(Item item);
        void onInspect(Item item);
    }

    // Реализация по умолчанию ItemActionListener
    public static class SimpleItemActionListener implements OnItemActionListener {
        @Override
        public void onEquip(Item item) {
            // Пустая реализация по умолчанию
        }

        @Override
        public void onUse(Item item) {
            // Пустая реализация по умолчанию
        }

        @Override
        public void onDrop(Item item) {
            // Пустая реализация по умолчанию
        }

        @Override
        public void onInspect(Item item) {
            // Пустая реализация по умолчанию
        }
    }

    public void setItemActionListener(OnItemActionListener listener) {
        this.itemActionListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_item_actions, container, false);

        if (getArguments() != null) {
            item = (Item) getArguments().getSerializable(ARG_ITEM);
        }

        // Устанавливаем слушатель по умолчанию, если не установлен
        if (itemActionListener == null) {
            itemActionListener = new SimpleItemActionListener();
        }

        setupDialog(view);
        setupActionButtons(view);

        return view;
    }

    private void setupDialog(View view) {
        TextView titleText = view.findViewById(R.id.dialogTitle);
        TextView itemNameText = view.findViewById(R.id.itemNameText);
        TextView itemTypeText = view.findViewById(R.id.itemTypeText);

        if (item != null) {
            if (titleText != null) {
                titleText.setText("Item Actions");
            }
            if (itemNameText != null) {
                itemNameText.setText(item.getName());
            }
            if (itemTypeText != null) {
                itemTypeText.setText(getItemTypeDisplayName(item.getType()));
            }
        }

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private String getItemTypeDisplayName(String itemType) {
        switch (itemType) {
            case "WEAPON": return "Weapon";
            case "ARMOR": return "Armor";
            case "ACCESSORY": return "Accessory";
            case "CONSUMABLE": return "Consumable";
            case "POTION": return "Potion";
            case "SCROLL": return "Scroll";
            case "BOOK": return "Book";
            case "KEY": return "Key Item";
            case "MATERIAL": return "Material";
            case "QUEST": return "Quest Item";
            default: return "Item";
        }
    }

    private void setupActionButtons(View view) {
        Button equipButton = view.findViewById(R.id.equipButton);
        Button useButton = view.findViewById(R.id.useButton);
        Button dropButton = view.findViewById(R.id.dropButton);
        Button inspectButton = view.findViewById(R.id.inspectButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        if (item != null) {
            setupButtonVisibilityAndText(equipButton, useButton, dropButton, inspectButton);
            setupButtonClickListeners(equipButton, useButton, dropButton, inspectButton);
        } else {
            if (equipButton != null) equipButton.setVisibility(View.GONE);
            if (useButton != null) useButton.setVisibility(View.GONE);
            if (dropButton != null) dropButton.setVisibility(View.GONE);
            if (inspectButton != null) inspectButton.setVisibility(View.GONE);
        }

        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> dismiss());
        }
    }

    private void setupButtonVisibilityAndText(Button equipButton, Button useButton,
                                              Button dropButton, Button inspectButton) {
        String itemType = item.getType();

        if (equipButton != null) {
            boolean canEquip = "WEAPON".equals(itemType) || "ARMOR".equals(itemType) ||
                    "ACCESSORY".equals(itemType);
            equipButton.setVisibility(canEquip ? View.VISIBLE : View.GONE);

            if ("WEAPON".equals(itemType)) {
                equipButton.setText(getString(R.string.equip_weapon));
            } else if ("ARMOR".equals(itemType)) {
                equipButton.setText(getString(R.string.equip_armor));
            } else {
                equipButton.setText(getString(R.string.equip_item));
            }
        }

        if (useButton != null) {
            boolean canUse = "CONSUMABLE".equals(itemType) || "SCROLL".equals(itemType) ||
                    "POTION".equals(itemType) || "BOOK".equals(itemType);
            useButton.setVisibility(canUse ? View.VISIBLE : View.GONE);

            if ("POTION".equals(itemType)) {
                useButton.setText(getString(R.string.drink_potion));
            } else if ("SCROLL".equals(itemType)) {
                useButton.setText(getString(R.string.read_scroll));
            } else if ("BOOK".equals(itemType)) {
                useButton.setText(getString(R.string.read_book));
            } else {
                useButton.setText(getString(R.string.use_item));
            }
        }

        // Для ключевых и квестовых предметов скрываем кнопки выброса
        if (dropButton != null) {
            boolean canDrop = !"KEY".equals(itemType) && !"QUEST".equals(itemType);
            dropButton.setVisibility(canDrop ? View.VISIBLE : View.GONE);
            dropButton.setText(getString(R.string.drop_item));
        }

        if (inspectButton != null) {
            inspectButton.setVisibility(View.VISIBLE);
            inspectButton.setText(getString(R.string.inspect_item));
        }
    }

    private void setupButtonClickListeners(Button equipButton, Button useButton,
                                           Button dropButton, Button inspectButton) {
        if (equipButton != null && equipButton.getVisibility() == View.VISIBLE) {
            equipButton.setOnClickListener(v -> {
                if (itemActionListener != null) {
                    itemActionListener.onEquip(item);
                }
                dismiss();
            });
        }

        if (useButton != null && useButton.getVisibility() == View.VISIBLE) {
            useButton.setOnClickListener(v -> {
                if (itemActionListener != null) {
                    itemActionListener.onUse(item);
                }
                dismiss();
            });
        }

        if (dropButton != null && dropButton.getVisibility() == View.VISIBLE) {
            dropButton.setOnClickListener(v -> {
                if (itemActionListener != null) {
                    itemActionListener.onDrop(item);
                }
                dismiss();
            });
        }

        if (inspectButton != null) {
            inspectButton.setOnClickListener(v -> {
                if (itemActionListener != null) {
                    itemActionListener.onInspect(item);
                }
                dismiss();
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            getDialog().getWindow().setLayout(width, height);
        }
    }

    // Вспомогательные методы для быстрого создания диалога

    public static ItemActionsDialog newInstance(Item item, OnItemActionListener listener) {
        ItemActionsDialog dialog = newInstance(item);
        dialog.setItemActionListener(listener);
        return dialog;
    }

    // Методы для создания диалога с конкретными действиями

    public static ItemActionsDialog newInstanceWithEquipAction(Item item, OnEquipActionListener listener) {
        return newInstance(item, new SimpleItemActionListener() {
            @Override
            public void onEquip(Item item) {
                listener.onEquip(item);
            }
        });
    }

    public static ItemActionsDialog newInstanceWithUseAction(Item item, OnUseActionListener listener) {
        return newInstance(item, new SimpleItemActionListener() {
            @Override
            public void onUse(Item item) {
                listener.onUse(item);
            }
        });
    }

    public static ItemActionsDialog newInstanceWithDropAction(Item item, OnDropActionListener listener) {
        return newInstance(item, new SimpleItemActionListener() {
            @Override
            public void onDrop(Item item) {
                listener.onDrop(item);
            }
        });
    }

    // Специализированные интерфейсы для отдельных действий

    public interface OnEquipActionListener {
        void onEquip(Item item);
    }

    public interface OnUseActionListener {
        void onUse(Item item);
    }

    public interface OnDropActionListener {
        void onDrop(Item item);
    }

    public interface OnInspectActionListener {
        void onInspect(Item item);
    }

    // Метод для удобного использования с лямбда-выражениями
    public static ItemActionsDialog newInstance(Item item,
                                                OnEquipActionListener equipListener,
                                                OnUseActionListener useListener,
                                                OnDropActionListener dropListener,
                                                OnInspectActionListener inspectListener) {

        return newInstance(item, new SimpleItemActionListener() {
            @Override
            public void onEquip(Item item) {
                if (equipListener != null) equipListener.onEquip(item);
            }

            @Override
            public void onUse(Item item) {
                if (useListener != null) useListener.onUse(item);
            }

            @Override
            public void onDrop(Item item) {
                if (dropListener != null) dropListener.onDrop(item);
            }

            @Override
            public void onInspect(Item item) {
                if (inspectListener != null) inspectListener.onInspect(item);
            }
        });
    }
}