package raf.console.quran7hours.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import raf.console.quran7hours.R;
import raf.console.quran7hours.ui.MainActivity;

public class UniversalListAdapter extends RecyclerView.Adapter<UniversalListAdapter.Holder> {

    public static final int TYPE_SURA = 0;
    public static final int TYPE_PAGE = 1;

    private final Context ctx;
    private final String[] suras; // Для сур
    private final List<Integer> pages; // Для страниц
    private final int contentType;
    private final OnItemClick listener;

    // Текущая выделенная позиция
    private int currentPosition = -1;

    public interface OnItemClick {
        void onItemSelected(int position, int contentType);
    }

    // Конструктор для сур
    public UniversalListAdapter(Context context,
                                String[] suraArray,
                                OnItemClick clickListener) {
        ctx = context;
        suras = suraArray;
        pages = null;
        contentType = TYPE_SURA;
        listener = clickListener;
    }

    // Конструктор для страниц
    public UniversalListAdapter(Context context,
                                List<Integer> pageNumbers,
                                OnItemClick clickListener) {
        ctx = context;
        suras = null;
        pages = pageNumbers;
        contentType = TYPE_PAGE;
        listener = clickListener;
    }

    static class Holder extends RecyclerView.ViewHolder {
        MaterialTextView text;
        View itemView;

        public Holder(View view) {
            super(view);
            itemView = view;
            text = view.findViewById(R.id.itemText); // Будем использовать один layout
        }
    }

    /**
     * Устанавливает текущую воспроизводимую суру (для TYPE_SURA)
     */
    public void setCurrentSura(int index) {
        Log.d("UNIVERSAL_ADAPTER", "setCurrentSura: " + index + ", тип: " + contentType);

        if (contentType != TYPE_SURA) return;

        if (index == currentPosition) {
            return;
        }

        int oldPosition = currentPosition;
        currentPosition = index;

        if (oldPosition != -1) {
            notifyItemChanged(oldPosition);
        }
        if (currentPosition != -1) {
            notifyItemChanged(currentPosition);
        }
    }

    /**
     * Устанавливает текущую воспроизводимую страницу (для TYPE_PAGE)
     */

    public void setCurrentPage(int pageNumber) {
        Log.d("UNIVERSAL_ADAPTER", "setCurrentPage: " + pageNumber + ", тип: " + contentType);

        if (contentType != TYPE_PAGE || pages == null) return;

        int newPosition = -1;
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i) == pageNumber) {
                newPosition = i;
                break;
            }
        }

        Log.d("UNIVERSAL_ADAPTER", "Найдена позиция в списке: " + newPosition);
        Log.d("UNIVERSAL_ADAPTER", "Текущая позиция: " + currentPosition);

        if (newPosition != currentPosition) {
            int oldPosition = currentPosition;
            currentPosition = newPosition;

            if (oldPosition != -1) {
                notifyItemChanged(oldPosition);
            }
            if (currentPosition != -1) {
                notifyItemChanged(currentPosition);
            }
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Используем один layout для обоих типов
        View view = LayoutInflater.from(ctx)
                .inflate(R.layout.item_universal_list, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        boolean isSelected = (position == currentPosition);

        if (contentType == TYPE_SURA && suras != null) {
            bindSuraItem(holder, position, isSelected);
        } else if (contentType == TYPE_PAGE && pages != null) {
            bindPageItem(holder, position, isSelected);
        }

        // Обработчик клика
        holder.itemView.setOnClickListener(v -> listener.onItemSelected(position, contentType));
    }

    /**
     * Биндинг для сур (полностью сохраняем логику из SuraListAdapter)
     */
    private void bindSuraItem(Holder holder, int position, boolean isSelected) {
        String full = suras[position];
        String[] parts = full.split("\n", 2);
        String title = parts[0];
        String subtitle = parts.length > 1 ? parts[1] : "";

        SpannableStringBuilder sb = new SpannableStringBuilder();

        // ===== TITLE =====
        int tStart = sb.length();
        sb.append(title);
        sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), tStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new AbsoluteSizeSpan(18, true), tStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Цвет текста в зависимости от выделения
        if (isSelected) {
            sb.setSpan(new ForegroundColorSpan(Color.WHITE), tStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            sb.setSpan(new ForegroundColorSpan(Color.BLACK), tStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // ===== SUBTITLE =====
        if (!subtitle.isEmpty()) {
            int sStart = sb.length();
            sb.append("\n").append(subtitle);
            sb.setSpan(new AbsoluteSizeSpan(14, true), sStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Цвет подзаголовка в зависимости от выделения
            if (isSelected) {
                sb.setSpan(new ForegroundColorSpan(Color.WHITE), sStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                sb.setSpan(new ForegroundColorSpan(Color.parseColor("#555555")), sStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        holder.text.setText(sb);

        // Устанавливаем фон в зависимости от выделения
        if (isSelected) {
            holder.itemView.setBackgroundColor(Color.parseColor("#33AAFF"));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    /**
     * Биндинг для страниц (полностью сохраняем логику из PageListAdapter)
     */
    private void bindPageItem(Holder holder, int position, boolean isSelected) {
        int pageNumber = pages.get(position);

        // Формируем текст с разметкой (как в PageListAdapter)
        SpannableStringBuilder sb = createPageText(pageNumber, isSelected);
        holder.text.setText(sb);

        // Устанавливаем фон в зависимости от выделения
        if (isSelected) {
            holder.itemView.setBackgroundColor(Color.parseColor("#33AAFF"));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    /**
     * Создает форматированный текст для отображения страницы (из PageListAdapter)
     */
    private SpannableStringBuilder createPageText(int pageNumber, boolean isSelected) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        int textColor, secondaryColor, tertiaryColor;

        // Выбираем цвета в зависимости от выделения
        if (isSelected) {
            textColor = Color.WHITE;
            secondaryColor = Color.WHITE;
            tertiaryColor = Color.WHITE;
        } else {
            textColor = Color.BLACK;
            secondaryColor = Color.parseColor("#555555");
            tertiaryColor = Color.GRAY;
        }

        // Заголовок: "Страница X"
        int titleStart = sb.length();
        sb.append("Страница ").append(String.valueOf(pageNumber));

        // Стили для заголовка
        sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                titleStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new AbsoluteSizeSpan(18, true),
                titleStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(textColor),
                titleStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Название суры (если доступно)
        String suraTitle = getSuraTitle(pageNumber);
        if (!suraTitle.isEmpty()) {
            int suraStart = sb.length();
            sb.append("\n").append(suraTitle);

            sb.setSpan(new AbsoluteSizeSpan(15, true),
                    suraStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new ForegroundColorSpan(secondaryColor),
                    suraStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Аяты на странице (если доступно)
        String ayats = getAyatsOnPage(pageNumber);
        if (!ayats.isEmpty()) {
            int ayatsStart = sb.length();
            sb.append("\n").append(ayats);

            sb.setSpan(new AbsoluteSizeSpan(14, true),
                    ayatsStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new ForegroundColorSpan(tertiaryColor),
                    ayatsStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return sb;
    }

    /**
     * Получает название суры для страницы (из PageListAdapter)
     */
    private String getSuraTitle(int pageNumber) {
        if (ctx instanceof MainActivity) {
            MainActivity activity = (MainActivity) ctx;
            return activity.getSuraTitle(pageNumber);
        }
        return "";
    }

    /**
     * Получает аяты на странице (из PageListAdapter)
     */
    private String getAyatsOnPage(int pageNumber) {
        if (ctx instanceof MainActivity) {
            MainActivity activity = (MainActivity) ctx;
            return activity.getAyatsOnPage(pageNumber);
        }
        return "";
    }

    @Override
    public int getItemCount() {
        if (contentType == TYPE_SURA && suras != null) {
            return suras.length;
        } else if (contentType == TYPE_PAGE && pages != null) {
            return pages.size();
        }
        return 0;
    }

    /**
     * Очищает выделение
     */
    public void clearSelection() {
        if (currentPosition != -1) {
            int oldPosition = currentPosition;
            currentPosition = -1;
            notifyItemChanged(oldPosition);
        }
    }
}