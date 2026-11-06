package raf.console.quran7hours.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import raf.console.quran7hours.R;

public class SuraListAdapter extends RecyclerView.Adapter<SuraListAdapter.Holder> {

    private final Context ctx;
    private final String[] suras;
    private final OnSuraClick listener;

    private int currentIndex = -1;

    public interface OnSuraClick {
        void onSuraSelected(int index);
    }

    public SuraListAdapter(Context c, String[] s, OnSuraClick l) {
        ctx = c;
        suras = s;
        listener = l;
    }

    static class Holder extends RecyclerView.ViewHolder {
        MaterialTextView title;
        public Holder(View v) {
            super(v);
            title = v.findViewById(R.id.suraItemText);
        }
    }

    public void setCurrentPlaying(int index) {
        currentIndex = index;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.item_sura_list, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {

        String full = suras[pos];
        String[] parts = full.split("\n", 2);
        String title = parts[0];
        String subtitle = parts.length > 1 ? parts[1] : "";

        SpannableStringBuilder sb = new SpannableStringBuilder();

        // ===== TITLE =====
        int tStart = sb.length();
        sb.append(title);
        sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), tStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new AbsoluteSizeSpan(18, true), tStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(Color.BLACK), tStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // ===== SUBTITLE =====
        if (!subtitle.isEmpty()) {
            int sStart = sb.length();
            sb.append("\n").append(subtitle);
            sb.setSpan(new AbsoluteSizeSpan(14, true), sStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new ForegroundColorSpan(Color.parseColor("#555555")), sStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // ===== CURRENT SURAH HIGHLIGHT: BLUE =====
        if (pos == currentIndex) {
            // перекрываем предыдущие цвета
            sb.setSpan(
                    new ForegroundColorSpan(Color.parseColor("#33AAFF")),
                    0,
                    sb.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        h.title.setText(sb);

        h.itemView.setOnClickListener(v -> listener.onSuraSelected(pos));
    }


    @Override
    public int getItemCount() {
        return suras.length;
    }
}
