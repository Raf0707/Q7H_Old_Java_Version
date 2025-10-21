package raf.console.quran7hours.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import raf.console.quran7hours.R;
import raf.console.quran7hours.model.TafsirItem;

/**
 * Адаптер для отображения тафсира (арабский текст, перевод и пояснение)
 * с возможностью копировать аят при нажатии.
 */
public class TafsirAdapter extends RecyclerView.Adapter<TafsirAdapter.ViewHolder> {

    private final List<TafsirItem> items;

    public TafsirAdapter(List<TafsirItem> items) {
        this.items = items;
    }

    // ---------------------------
    // 🔹 ViewHolder
    // ---------------------------
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView num, arabic, translate, tafsir;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            num = itemView.findViewById(R.id.num);
            arabic = itemView.findViewById(R.id.arabic);
            translate = itemView.findViewById(R.id.translate);
            tafsir = itemView.findViewById(R.id.tafsir);
        }
    }

    // ---------------------------
    // 🔹 Создание элемента
    // ---------------------------
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tafsir_ayah, parent, false);
        return new ViewHolder(v);
    }

    // ---------------------------
    // 🔹 Заполнение данных
    // ---------------------------
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TafsirItem item = items.get(position);

        // 🔸 Скрываем подпись для аятов 0 (описание суры)
        if (item.getAyah() != null && item.getAyah().trim().equals("0")) {
            holder.num.setVisibility(View.GONE);
        } else {
            holder.num.setVisibility(View.VISIBLE);
            holder.num.setText(item.getSura() + ":" + item.getAyah());
        }

        holder.arabic.setText(item.getArabic());
        holder.translate.setText(item.getTranslation());

        // 🔹 Показываем тафсир (если есть)
        if (item.getTafsir() != null && !item.getTafsir().isEmpty()) {
            holder.tafsir.setVisibility(View.VISIBLE);
            holder.tafsir.setText(item.getTafsir());
        } else {
            holder.tafsir.setVisibility(View.GONE);
        }

        // ---------------------------
        // 🔹 Копирование при нажатии
        // ---------------------------
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            ClipboardManager clipboard = (ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);

            StringBuilder sb = new StringBuilder();

            if (item.getAyah() != null && !item.getAyah().equals("0")) {
                sb.append("(").append(item.getSura()).append(":").append(item.getAyah()).append(")\n");
            }

            sb.append(item.getArabic());
            if (item.getTranslation() != null && !item.getTranslation().isEmpty()) {
                sb.append("\n").append(item.getTranslation());
            }
            if (item.getTafsir() != null && !item.getTafsir().isEmpty()) {
                sb.append("\n\n").append(item.getTafsir());
            }

            ClipData clip = ClipData.newPlainText("ayah", sb.toString());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(context, "Аят скопирован", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
