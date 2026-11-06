package raf.console.quran7hours.model;

import android.graphics.Color;

import com.google.gson.annotations.SerializedName;

public class Bookmark {
    private int position;
    @SerializedName("fillColor") // Указываем имя поля в JSON
    private int fillColor;

    // Статический список доступных цветов
    public static final int[] COLORS = {
            Color.WHITE, // Белый
            Color.RED,   // Красный
            Color.GREEN, // Зеленый
            Color.BLACK,  // Черный
            Color.YELLOW // Желтый
    };

    public Bookmark(int position, int fillColor) {
        this.position = position;
        this.fillColor = fillColor;
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    // Метод для получения цвета по индексу
    public static int getColorByIndex(int index) {
        if (index >= 0 && index < COLORS.length) {
            return COLORS[index];
        }
        return Color.WHITE; // Возвращаем белый цвет по умолчанию
    }

    // Метод для получения индекса цвета
    public static int getColorIndex(int color) {
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == color) {
                return i;
            }
        }
        return -1; // Если цвет не найден
    }
}