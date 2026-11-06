package raf.console.quran7hours.adapters;

import static java.lang.String.format;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import raf.console.quran7hours.R;
import raf.console.quran7hours.model.QuranItemContent;


public class DrawerQuranContentAdapter extends RecyclerView.Adapter<DrawerQuranContentAdapter.ViewHolder> {
    private LayoutInflater inflater;
    private List<QuranItemContent> quranDrawer;
    private ViewPager2 quranPager;
    private Integer[] numPages; // Массив номеров страниц для текущей категории

    public DrawerQuranContentAdapter(Context context, List<QuranItemContent> quranDrawer, ViewPager2 quranPager, Integer[] numPages) {
        this.quranDrawer = quranDrawer;
        this.inflater = LayoutInflater.from(context);
        this.quranPager = quranPager;
        this.numPages = numPages; // Передаем массив номеров страниц
    }

    // Метод для обновления массива номеров страниц
    public void updateNumPages(Integer[] newNumPages) {
        this.numPages = newNumPages;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.quran_content_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        QuranItemContent newTitleSure = quranDrawer.get(position);

        holder.sure.setText(String.format("%s", newTitleSure.getTitle()));

        holder.sure.setOnClickListener(v -> {
            int targetPage = numPages[position] - 1; // Используем обновленный массив номеров страниц
            quranPager.setCurrentItem(targetPage, true);
        });
    }

    @Override
    public int getItemCount() {
        return quranDrawer.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView sure;

        ViewHolder(View view) {
            super(view);
            sure = view.findViewById(R.id.sure);
        }
    }
}
