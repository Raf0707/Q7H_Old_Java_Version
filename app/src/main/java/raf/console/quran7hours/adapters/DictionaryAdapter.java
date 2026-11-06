package raf.console.quran7hours.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import raf.console.quran7hours.R;
import raf.console.quran7hours.model.DictItem;

/**
 * 📘 Адаптер для отображения Коранического словаря.
 * Поддерживает динамическое изменение шрифта, видимости и пропорциональных отступов.
 */
public class DictionaryAdapter extends RecyclerView.Adapter<DictionaryAdapter.ViewHolder> {

    private final List<DictItem> items;
    private final Context context;

    public DictionaryAdapter(Context context, List<DictItem> items) {
        this.context = context;
        this.items = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        TextView num, ar, root, translate, grammar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container); // добавь id="container" в item_dictionary_entry root LinearLayout
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
        SharedPreferences prefs = context.getSharedPreferences("TextSettings", Context.MODE_PRIVATE);

        // 🎨 Цвета из темы
        int primary = ContextCompat.getColor(context, R.color.md_theme_primary);
        int onSurface = ContextCompat.getColor(context, R.color.md_theme_onSurface);
        int secondary = ContextCompat.getColor(context, R.color.md_theme_secondary);
        int background = ContextCompat.getColor(context, R.color.md_theme_surfaceContainerLow);
        int variant = ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant);

        holder.itemView.setBackgroundColor(background);

        // ====== Загрузка размеров шрифта ======
        float sizeNum = prefs.getFloat("dict_num_size", 16f);
        float sizeArabic = prefs.getFloat("dict_arabic_size", 22f);
        float sizeRoot = prefs.getFloat("dict_root_size", 18f);
        float sizeTranslate = prefs.getFloat("dict_translate_size", 18f);
        float sizeGrammar = prefs.getFloat("dict_grammar_size", 18f);

        // ====== Флаги отображения ======
        boolean showNum = prefs.getBoolean("dict_show_num", true);
        boolean showArabic = prefs.getBoolean("dict_show_arabic", true);
        boolean showRoot = prefs.getBoolean("dict_show_root", true);
        boolean showTranslate = prefs.getBoolean("dict_show_translate", true);
        boolean showGrammar = prefs.getBoolean("dict_show_grammar", true);

        // ====== Пропорциональные отступы ======
        float basePadding = Math.max(sizeArabic, sizeTranslate) * 0.25f;
        int pad = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                basePadding,
                context.getResources().getDisplayMetrics()
        );

        holder.container.setPadding(pad, pad * 2, pad, pad * 2);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        if (params != null) {
            params.bottomMargin = pad * 2;
            holder.itemView.setLayoutParams(params);
        }

        // ====== Номер ======
        if (item.getNum() != null && !item.getNum().isEmpty() && showNum) {
            holder.num.setVisibility(View.VISIBLE);
            holder.num.setText(item.getNum());
            holder.num.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeNum);
            holder.num.setTextColor(primary);
            holder.num.setPadding(0, 0, 0, pad / 2);
        } else {
            holder.num.setVisibility(View.GONE);
        }

        // ====== Арабский ======
        if (item.getAr() != null && !item.getAr().isEmpty() && showArabic) {
            holder.ar.setVisibility(View.VISIBLE);
            holder.ar.setText(item.getAr());
            holder.ar.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeArabic);
            holder.ar.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onBackground));
            holder.ar.setPadding(0, pad / 2, 0, pad / 2);
        } else {
            holder.ar.setVisibility(View.GONE);
        }

        // ====== Корень ======
        if (item.getRoot() != null && !item.getRoot().isEmpty() && showRoot) {
            holder.root.setVisibility(View.VISIBLE);
            holder.root.setText(String.format(Locale.getDefault(), "Корень: %s", item.getRoot()));
            holder.root.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeRoot);
            holder.root.setTextColor(secondary);
            holder.root.setPadding(0, pad / 2, 0, pad / 2);
        } else {
            holder.root.setVisibility(View.GONE);
        }

        // ====== Перевод ======
        if (item.getTranslate() != null && !item.getTranslate().isEmpty() && showTranslate) {
            holder.translate.setVisibility(View.VISIBLE);
            holder.translate.setText(item.getTranslate());
            holder.translate.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeTranslate);
            holder.translate.setTextColor(onSurface);
            holder.translate.setPadding(0, pad / 2, 0, pad / 2);
        } else {
            holder.translate.setVisibility(View.GONE);
        }

        // ====== Грамматика ======
        if (item.getGrammar() != null && !item.getGrammar().isEmpty() && showGrammar) {
            holder.grammar.setVisibility(View.VISIBLE);
            holder.grammar.setText(item.getGrammar());
            holder.grammar.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeGrammar);
            holder.grammar.setTextColor(variant);
            holder.grammar.setPadding(0, pad / 2, 0, pad / 2);
        } else {
            holder.grammar.setVisibility(View.GONE);
        }

        // 💾 Копирование при нажатии
        holder.itemView.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);

            StringBuilder sb = new StringBuilder();

            if (showNum && item.getNum() != null && !item.getNum().isEmpty())
                sb.append(item.getNum()).append("\n");

            if (showArabic && item.getAr() != null && !item.getAr().isEmpty())
                sb.append(item.getAr()).append("\n");

            if (showTranslate && item.getTranslate() != null && !item.getTranslate().isEmpty())
                sb.append(item.getTranslate()).append("\n");

            if (showRoot && item.getRoot() != null && !item.getRoot().isEmpty())
                sb.append("Корень: ").append(item.getRoot()).append("\n");

            if (showGrammar && item.getGrammar() != null && !item.getGrammar().isEmpty())
                sb.append(item.getGrammar());

            ClipData clip = ClipData.newPlainText("dict_entry", sb.toString().trim());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(context, "Слово скопировано", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
