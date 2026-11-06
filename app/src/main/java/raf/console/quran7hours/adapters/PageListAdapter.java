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

public class PageListAdapter extends RecyclerView.Adapter<PageListAdapter.Holder> {

    private final Context ctx;
    private final List<Integer> pages;
    private final OnPageClick listener;

    // Текущая выделенная позиция в списке (0..N-1)
    private int currentPosition = -1;

    public interface OnPageClick {
        void onPageSelected(int pageNumber);
    }

    public PageListAdapter(Context context,
                           List<Integer> pageNumbers,
                           OnPageClick clickListener) {
        ctx = context;
        pages = pageNumbers;
        listener = clickListener;
    }

    static class Holder extends RecyclerView.ViewHolder {
        MaterialTextView text;
        View itemView;

        public Holder(View view) {
            super(view);
            itemView = view;
            text = view.findViewById(R.id.pageItemText);
        }
    }

    /**
     * Устанавливает текущую воспроизводимую страницу
     * @param pageNumber номер страницы (1..604)
     */
    public void setCurrentPlaying(int pageNumber) {
        Log.d("ADAPTER", "Получен номер страницы: " + pageNumber);

        int newPosition = -1;
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i) == pageNumber) {
                newPosition = i;
                break;
            }
        }

        Log.d("ADAPTER", "Найдена позиция в списке: " + newPosition);
        Log.d("ADAPTER", "Текущая позиция: " + currentPosition);

        if (newPosition != currentPosition) {
            int oldPosition = currentPosition;
            currentPosition = newPosition;

            if (oldPosition != -1) {
                Log.d("ADAPTER", "Обновляем старую позицию: " + oldPosition);
                notifyItemChanged(oldPosition);
            }
            if (currentPosition != -1) {
                Log.d("ADAPTER", "Обновляем новую позицию: " + currentPosition);
                notifyItemChanged(currentPosition);
            }
        } else {
            Log.d("ADAPTER", "Позиция не изменилась");
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx)
                .inflate(R.layout.item_page_list_single_text, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        int pageNumber = pages.get(position);
        boolean isSelected = (position == currentPosition);

        // Формируем текст с разметкой
        SpannableStringBuilder sb = createPageText(pageNumber, isSelected);
        holder.text.setText(sb);

        // Устанавливаем фон в зависимости от выделения
        if (isSelected) {
            holder.itemView.setBackgroundColor(Color.parseColor("#33AAFF"));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // Обработчик клика
        holder.itemView.setOnClickListener(v -> listener.onPageSelected(pageNumber));
    }

    /**
     * Создает форматированный текст для отображения страницы
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
     * Получает название суры для страницы
     */
    private String getSuraTitle(int pageNumber) {
        if (ctx instanceof MainActivity) {
            MainActivity activity = (MainActivity) ctx;
            return activity.getSuraTitle(pageNumber);
        }
        return "";
    }

    /**
     * Получает аяты на странице
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
        return pages.size();
    }

    /**
     * Очищает выделение текущей страницы
     */
    public void clearSelection() {
        if (currentPosition != -1) {
            int oldPosition = currentPosition;
            currentPosition = -1;
            notifyItemChanged(oldPosition);
        }
    }
}