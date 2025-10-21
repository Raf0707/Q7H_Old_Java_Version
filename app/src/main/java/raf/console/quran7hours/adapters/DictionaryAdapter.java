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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import raf.console.quran7hours.R;
import raf.console.quran7hours.model.DictItem;

/**
 * 📘 Адаптер для отображения Коранического словаря.
 * - Цвета взяты из темы (colors.xml)
 * - При клике карточка копируется в буфер обмена
 */
public class DictionaryAdapter extends RecyclerView.Adapter<DictionaryAdapter.ViewHolder> {

    private final List<DictItem> items;

    public DictionaryAdapter(List<DictItem> items) {
        this.items = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView num, ar, root, translate, grammar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            num = itemView.findViewById(R.id.num);
            ar = itemView.findViewById(R.id.ar);
            root = itemView.findViewById(R.id.root);
            translate = itemView.findViewById(R.id.translate);
            grammar = itemView.findViewById(R.id.grammar);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dictionary_entry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DictItem item = items.get(position);
        Context context = holder.itemView.getContext();

        // 🎨 Цвета из темы
        int primary = ContextCompat.getColor(context, R.color.md_theme_primary);
        int onSurface = ContextCompat.getColor(context, R.color.md_theme_onSurface);
        int secondary = ContextCompat.getColor(context, R.color.md_theme_secondary);
        int background = ContextCompat.getColor(context, R.color.md_theme_surfaceContainerLow);

        holder.itemView.setBackgroundColor(background);

        holder.num.setTextColor(primary);
        holder.num.setText(item.getNum());

        holder.ar.setText(item.getAr());
        holder.ar.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onBackground));

        holder.translate.setText(item.getTranslate());
        holder.translate.setTextColor(onSurface);

        if (!item.getRoot().isEmpty()) {
            holder.root.setText("Корень: " + item.getRoot());
            holder.root.setTextColor(secondary);
            holder.root.setVisibility(View.VISIBLE);
        } else {
            holder.root.setVisibility(View.GONE);
        }

        if (!item.getGrammar().isEmpty()) {
            holder.grammar.setText(item.getGrammar());
            holder.grammar.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant));
            holder.grammar.setVisibility(View.VISIBLE);
        } else {
            holder.grammar.setVisibility(View.GONE);
        }

        // 💾 Копирование при нажатии
        holder.itemView.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);

            StringBuilder sb = new StringBuilder();
            if (!item.getNum().isEmpty()) {
                sb.append(item.getNum()).append("\n");
            }
            sb.append(item.getAr());
            if (!item.getTranslate().isEmpty()) {
                sb.append("\n").append(item.getTranslate());
            }
            if (!item.getRoot().isEmpty()) {
                sb.append("\nКорень: ").append(item.getRoot());
            }
            if (!item.getGrammar().isEmpty()) {
                sb.append("\n").append(item.getGrammar());
            }

            ClipData clip = ClipData.newPlainText("dict_entry", sb.toString());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(context, "Слово скопировано", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
