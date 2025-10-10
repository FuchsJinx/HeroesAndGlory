package com.HG.heroesglory.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.HG.heroesglory.R;

public class DiceTypeAdapter extends BaseAdapter {

    private Integer[] diceTypes;
    private LayoutInflater inflater;
    private OnDiceSelectedListener listener;
    private int selectedPosition = -1;

    public DiceTypeAdapter(android.content.Context context, Integer[] diceTypes, OnDiceSelectedListener listener) {
        this.diceTypes = diceTypes;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return diceTypes.length;
    }

    @Override
    public Object getItem(int position) {
        return diceTypes[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_dice_type, parent, false);
            holder = new ViewHolder();
            holder.diceButton = convertView.findViewById(R.id.diceButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int diceType = diceTypes[position];
        holder.diceButton.setText("d" + diceType);

        // Выделение выбранного кубика
        if (position == selectedPosition) {
            holder.diceButton.setBackgroundResource(R.drawable.dice_selected_background);
        } else {
            holder.diceButton.setBackgroundResource(R.drawable.dice_normal_background);
        }

        holder.diceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition = position;
                notifyDataSetChanged(); // Обновляем отображение

                if (listener != null) {
                    listener.onDiceSelected(diceType);
                }
            }
        });

        // Установка эмодзи для разных типов кубиков
        setDiceEmoji(holder.diceButton, diceType);

        return convertView;
    }

    private void setDiceEmoji(Button button, int diceType) {
        String emoji;
        switch (diceType) {
            case 4:
                emoji = "🔺"; // Тетраэдр
                break;
            case 6:
                emoji = "⬜"; // Куб
                break;
            case 8:
                emoji = "🔷"; // Октаэдр
                break;
            case 10:
                emoji = "🔶"; // Декаэдр
                break;
            case 12:
                emoji = "🔴"; // Додекаэдр
                break;
            case 20:
                emoji = "🎯"; // Икосаэдр (основной для D&D)
                break;
            case 100:
                emoji = "💯"; // Процентный кубик
                break;
            default:
                emoji = "🎲"; // Общий кубик
        }
        button.setText(emoji + "\nd" + diceType);
    }

    public void setSelectedDice(int diceType) {
        for (int i = 0; i < diceTypes.length; i++) {
            if (diceTypes[i] == diceType) {
                selectedPosition = i;
                notifyDataSetChanged();
                break;
            }
        }
    }

    private static class ViewHolder {
        Button diceButton;
    }

    public interface OnDiceSelectedListener {
        void onDiceSelected(int diceType);
    }
}
