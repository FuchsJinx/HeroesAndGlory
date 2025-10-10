package com.HG.heroesglory.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.HG.heroesglory.R;
import com.HG.heroesglory.core.entities.Item;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private List<Item> inventoryItems;
    private OnItemClickListener listener;

    public InventoryAdapter(List<Item> inventoryItems, OnItemClickListener listener) {
        this.inventoryItems = inventoryItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        Item item = inventoryItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return inventoryItems != null ? inventoryItems.size() : 0;
    }

    public void updateItems(List<Item> newItems) {
        this.inventoryItems = newItems;
        notifyDataSetChanged();
    }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView itemImage;
        private TextView itemNameText;
        private TextView itemTypeText;
        private TextView itemStatsText;
        private TextView itemQuantityText;
        private Button useButton;
        private Button infoButton;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemNameText = itemView.findViewById(R.id.itemNameText);
            itemTypeText = itemView.findViewById(R.id.itemTypeText);
            itemStatsText = itemView.findViewById(R.id.itemStatsText);
            itemQuantityText = itemView.findViewById(R.id.itemQuantityText);
            useButton = itemView.findViewById(R.id.useButton);
            infoButton = itemView.findViewById(R.id.infoButton);
        }

        public void bind(Item item, OnItemClickListener listener) {
            itemNameText.setText(item.getName());
            itemTypeText.setText(getItemTypeDisplayName(item.getType()));
            itemStatsText.setText(getItemStatsSummary(item));

            // Отображение количества предметов
            if (item.getQuantity() != 0 && item.getQuantity() > 1) {
                itemQuantityText.setText(String.valueOf(item.getQuantity()));
                itemQuantityText.setVisibility(View.VISIBLE);
            } else {
                itemQuantityText.setVisibility(View.GONE);
            }

            // Загрузка изображения через Glide
            loadItemImage(item);

            useButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemUse(item);
                    }
                }
            });

            infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(item);
                    }
                }
            });

            // Клик на всей карточке
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(item);
                    }
                }
            });

            // Длинное нажатие для дополнительных действий
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listener != null) {
                        listener.onItemLongClick(item);
                    }
                    return true;
                }
            });
        }

        private void loadItemImage(Item item) {
            // Настройки для Glide
            RequestOptions requestOptions = new RequestOptions()
                    .centerCrop()
                    .placeholder(getPlaceholderDrawableByType(item.getType()))
                    .error(getPlaceholderDrawableByType(item.getType()))
                    .override(120, 120); // Оптимальный размер для изображений предметов

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                // Загрузка изображения из URL
                Glide.with(itemView.getContext())
                        .load(item.getImageUrl())
                        .apply(requestOptions)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(itemImage);
            } else {
                // Использование placeholder изображения
                Glide.with(itemView.getContext())
                        .load(getPlaceholderDrawableByType(item.getType()))
                        .apply(requestOptions)
                        .into(itemImage);
            }
        }

        private int getPlaceholderDrawableByType(String type) {
            switch (type) {
                case "WEAPON":
                    return R.drawable.item_sword;
                case "ARMOR":
                    return R.drawable.item_armor;
                case "CONSUMABLE":
                    return R.drawable.item_potion;
                case "ACCESSORY":
                    return R.drawable.item_ring;
                case "UTILITY":
                    return R.drawable.item_rope;
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

        private String getItemTypeDisplayName(String type) {
            switch (type) {
                case "WEAPON": return "Оружие";
                case "ARMOR": return "Броня";
                case "CONSUMABLE": return "Расходник";
                case "ACCESSORY": return "Аксессуар";
                case "UTILITY": return "Утилита";
                case "SCROLL": return "Свиток";
                case "KEY": return "Ключ";
                case "GOLD": return "Золото";
                case "QUEST": return "Квестовый предмет";
                default: return "Предмет";
            }
        }

        private String getItemStatsSummary(Item item) {
            if (item.getStats() == null || item.getStats().isEmpty()) {
                return item.getDescription() != null ?
                        item.getDescription() : "Особых свойств нет";
            }

            StringBuilder statsBuilder = new StringBuilder();

            switch (item.getType()) {
                case "WEAPON":
                    Object damage = item.getStats().get("damage");
                    Object damageType = item.getStats().get("damageType");
                    Object weaponType = item.getStats().get("weaponType");

                    if (damage != null) {
                        statsBuilder.append("Урон: ").append(damage);
                        if (damageType != null) {
                            statsBuilder.append(" (").append(damageType).append(")");
                        }
                    }
                    if (weaponType != null) {
                        if (statsBuilder.length() > 0) statsBuilder.append(" • ");
                        statsBuilder.append("Тип: ").append(weaponType);
                    }
                    break;

                case "ARMOR":
                    Object ac = item.getStats().get("armorClass");
                    Object armorType = item.getStats().get("armorType");

                    if (ac != null) {
                        statsBuilder.append("КД: ").append(ac);
                    }
                    if (armorType != null) {
                        if (statsBuilder.length() > 0) statsBuilder.append(" • ");
                        statsBuilder.append("Тип: ").append(armorType);
                    }
                    break;

                case "CONSUMABLE":
                    Object heal = item.getStats().get("heal");
                    Object mana = item.getStats().get("mana");
                    Object effect = item.getStats().get("effect");

                    if (heal != null) {
                        statsBuilder.append("Лечение: ").append(heal);
                    }
                    if (mana != null) {
                        if (statsBuilder.length() > 0) statsBuilder.append(" • ");
                        statsBuilder.append("Мана: ").append(mana);
                    }
                    if (effect != null) {
                        if (statsBuilder.length() > 0) statsBuilder.append(" • ");
                        statsBuilder.append("Эффект: ").append(effect);
                    }
                    break;

                case "ACCESSORY":
                    Object bonus = item.getStats().get("bonus");
                    Object attribute = item.getStats().get("attribute");

                    if (bonus != null) {
                        statsBuilder.append("Бонус: ").append(bonus);
                    }
                    if (attribute != null) {
                        if (statsBuilder.length() > 0) statsBuilder.append(" к ");
                        statsBuilder.append(attribute);
                    }
                    break;

                default:
                    // Для других типов предметов показываем первые 2 свойства
                    int count = 0;
                    for (String key : item.getStats().keySet()) {
                        if (count >= 2) break;
                        if (count > 0) statsBuilder.append(" • ");
                        statsBuilder.append(key).append(": ").append(item.getStats().get(key));
                        count++;
                    }
                    break;
            }

            // Если статистики нет, показываем описание или вес
            if (statsBuilder.length() == 0) {
                if (item.getWeight() != 0) {
                    statsBuilder.append("Вес: ").append(item.getWeight()).append(" кг");
                } else if (item.getDescription() != null) {
                    String desc = item.getDescription();
                    statsBuilder.append(desc.length() > 50 ? desc.substring(0, 47) + "..." : desc);
                }
            }

            return statsBuilder.toString();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Item item);
        void onItemUse(Item item);
        void onItemLongClick(Item item);
    }

    // Вспомогательные методы для управления данными
    public void addItem(Item item) {
        inventoryItems.add(item);
        notifyItemInserted(inventoryItems.size() - 1);
    }

    public void removeItem(Item item) {
        int position = inventoryItems.indexOf(item);
        if (position != -1) {
            inventoryItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateItem(Item item) {
        int position = inventoryItems.indexOf(item);
        if (position != -1) {
            inventoryItems.set(position, item);
            notifyItemChanged(position);
        }
    }

    public void clearItems() {
        int size = inventoryItems.size();
        inventoryItems.clear();
        notifyItemRangeRemoved(0, size);
    }

    public List<Item> getItems() {
        return inventoryItems;
    }

    public Item getItem(int position) {
        return inventoryItems.get(position);
    }
}