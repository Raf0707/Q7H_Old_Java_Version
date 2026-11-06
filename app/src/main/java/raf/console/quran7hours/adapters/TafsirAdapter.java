package raf.console.quran7hours.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.database.DatabaseProvider;
import androidx.media3.database.ExoDatabaseProvider;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.upstream.DefaultAllocator;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import raf.console.quran7hours.R;
import raf.console.quran7hours.model.TafsirItem;
import raf.console.quran7hours.ui.service.GlobalAudioManager;

/**
 * 📖 Адаптер для отображения тафсира с ExoPlayer и кешированием аудио.
 * Автоматически кэширует MP3 с azan.ru (Абу Бакр аш-Шатри),
 * при повторном воспроизведении — работает оффлайн.
 */
@UnstableApi
public class TafsirAdapter extends RecyclerView.Adapter<TafsirAdapter.ViewHolder> {

    private final List<TafsirItem> items;
    private final Context context;
    private ExoPlayer player;
    private int playingPosition = -1;
    private final Handler handler = new Handler();

    // 🎧 ExoPlayer Cache
    private static SimpleCache simpleCache;
    private static CacheDataSource.Factory cacheDataSourceFactory;

    // 🔸 Наш собственный кеш и список скачанных аятов
    private final File audioCacheDir;
    private final SharedPreferences audioPrefs;
    private final OkHttpClient httpClient = new OkHttpClient();

    public TafsirAdapter(Context context, List<TafsirItem> items) {
        this.context = context;
        this.items = items;

        // 📂 Каталог и prefs для MP3
        audioCacheDir = new File(context.getExternalFilesDir(null), "audio_cache");
        if (!audioCacheDir.exists()) audioCacheDir.mkdirs();
        audioPrefs = context.getSharedPreferences("audio_cache_prefs", Context.MODE_PRIVATE);

        initCache(context);
    }

    /** ✅ Инициализация кеша ExoPlayer */
    @OptIn(markerClass = UnstableApi.class)
    private void initCache(Context context) {
        if (simpleCache != null) return;
        try {
            File cacheDir = new File(context.getCacheDir(), "tafsir_audio_cache");
            if (!cacheDir.exists()) cacheDir.mkdirs();

            long maxCacheSize = 3000L * 1024 * 1024; // 3000 MB
            LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(maxCacheSize);
            DatabaseProvider databaseProvider = new ExoDatabaseProvider(context);

            simpleCache = new SimpleCache(cacheDir, evictor, databaseProvider);

            DefaultHttpDataSource.Factory httpSource = new DefaultHttpDataSource.Factory()
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(15000)
                    .setReadTimeoutMs(15000);

            // ✅ теперь используем DefaultDataSource, который умеет file://
            DefaultDataSource.Factory defaultSource =
                    new DefaultDataSource.Factory(context, httpSource);

            cacheDataSourceFactory = new CacheDataSource.Factory()
                    .setCache(simpleCache)
                    .setUpstreamDataSourceFactory(defaultSource)
                    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);

            Log.d("CACHE_INIT", "ExoPlayer Cache инициализирован успешно");
        } catch (Exception e) {
            Log.e("CACHE_INIT", "Ошибка инициализации кеша", e);
        }
    }


    /** 🔍 Проверка, есть ли аят в кеше */
    private boolean isAyahCached(int surah, int ayah) {
        String key = String.format(Locale.US, "%03d%03d", surah, ayah);
        File file = new File(audioCacheDir, key + ".mp3");
        return file.exists() && audioPrefs.getBoolean(key, false);
    }

    /** ✅ Пометить аят как скачанный */
    private void markAyahCached(int surah, int ayah) {
        String key = String.format(Locale.US, "%03d%03d", surah, ayah);
        audioPrefs.edit().putBoolean(key, true).apply();
    }

    /** 🌐 Скачивание и кеширование аята, если не скачан */
    private void cacheAyahIfNeeded(int surah, int ayah) {
        if (isAyahCached(surah, ayah)) return;

        String surahStr = String.format(Locale.US, "%03d", surah);
        String ayahStr = String.format(Locale.US, "%03d", ayah);
        String url = "https://azan.ru/upload/Quran/abu-bakr-al-shatri/" + surahStr + "/" + surahStr + ayahStr + ".mp3";
        File file = new File(audioCacheDir, surahStr + ayahStr + ".mp3");

        new Thread(() -> {
            try {
                Request request = new Request.Builder().url(url).build();
                Response response = httpClient.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    try (InputStream input = response.body().byteStream();
                         FileOutputStream output = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = input.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }
                        markAyahCached(surah, ayah);
                        Log.d("CACHE", "Скачан и сохранён аят: " + surah + ":" + ayah);
                    }
                } else {
                    Log.w("CACHE", "Ошибка при загрузке " + url);
                }

                response.close();
            } catch (Exception e) {
                Log.e("CACHE", "Ошибка загрузки", e);
            }
        }).start();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        TextView num, arabic, translate, tafsir;
        MaterialButton btnPlay, btnStop;
        ProgressBar audioProgress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container1);
            num = itemView.findViewById(R.id.num);
            arabic = itemView.findViewById(R.id.arabic);
            translate = itemView.findViewById(R.id.translate);
            tafsir = itemView.findViewById(R.id.tafsir);
            btnPlay = itemView.findViewById(R.id.btnPlay);
            btnStop = itemView.findViewById(R.id.btnStop);
            audioProgress = itemView.findViewById(R.id.audioProgress);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tafsir_ayah, parent, false);
        return new ViewHolder(v);
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TafsirItem item = items.get(position);
        SharedPreferences prefs = context.getSharedPreferences("TextSettings", Context.MODE_PRIVATE);

        float sizeArabic = prefs.getFloat("tafsir_arabic_size", 37f);
        float sizeTranslate = prefs.getFloat("tafsir_translation_size", 20f);
        float sizeComment = prefs.getFloat("tafsir_comment_size", 18f);

        boolean showArabic = prefs.getBoolean("tafsir_show_arabic", true);
        boolean showTranslation = prefs.getBoolean("tafsir_show_translation", true);
        boolean showComment = prefs.getBoolean("tafsir_show_comment", true);

        float baseSize = Math.max(sizeArabic, Math.max(sizeTranslate, sizeComment));
        float basePadding = baseSize * 0.25f;
        int pad = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, basePadding, context.getResources().getDisplayMetrics());

        if (holder.container != null)
            holder.container.setPadding(pad, pad * 2, pad, pad * 2);

        boolean isZeroAyah = item.getAyah() != null && item.getAyah().trim().equals("0");
        boolean isDuaAfterQuran = item.getSura() != null && item.getSura().trim().equals("115");

        // 🔸 Скрываем номер, если это дуа
        if (isZeroAyah || isDuaAfterQuran) {
            holder.num.setVisibility(View.GONE);
        } else {
            holder.num.setVisibility(View.VISIBLE);
            holder.num.setText(String.format(Locale.getDefault(), "%s:%s", item.getSura(), item.getAyah()));
        }

        holder.arabic.setText(item.getArabic());
        holder.arabic.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeArabic);
        holder.arabic.setVisibility(isZeroAyah ? View.VISIBLE : (showArabic ? View.VISIBLE : View.GONE));

        holder.translate.setText(item.getTranslation());
        holder.translate.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeTranslate);
        holder.translate.setVisibility(showTranslation ? View.VISIBLE : View.GONE);

        if (item.getTafsir() != null && !item.getTafsir().isEmpty()) {
            holder.tafsir.setVisibility(showComment ? View.VISIBLE : View.GONE);
            holder.tafsir.setText(item.getTafsir());
            holder.tafsir.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeComment);
        } else {
            holder.tafsir.setVisibility(View.GONE);
        }

        holder.audioProgress.setProgress(0);

        // 🎧 Автокеширование аята при отображении
        if (!isZeroAyah && !isDuaAfterQuran) {
            try {
                int surah = Integer.parseInt(item.getSura());
                int ayah = Integer.parseInt(item.getAyah());
                cacheAyahIfNeeded(surah, ayah);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ▶️ Воспроизведение
        holder.btnPlay.setOnClickListener(v -> {
            try {
                int surah = Integer.parseInt(item.getSura());
                int ayah = Integer.parseInt(item.getAyah());
                String surahStr = String.format(Locale.getDefault(), "%03d", surah);
                String ayahStr = String.format(Locale.getDefault(), "%03d", ayah);
                File cachedFile = new File(audioCacheDir, surahStr + ayahStr + ".mp3");
                Uri uri;

                if (cachedFile.exists()) {
                    uri = Uri.fromFile(cachedFile);
                    Log.d("AUDIO_PLAY", "🎧 Воспроизведение из кеша: " + cachedFile.getAbsolutePath());
                } else {
                    uri = Uri.parse("https://azan.ru/upload/Quran/abu-bakr-al-shatri/" +
                            surahStr + "/" + surahStr + ayahStr + ".mp3");
                    Log.d("AUDIO_PLAY", "🌐 Воспроизведение онлайн и автокеширование: " + uri);
                    cacheAyahIfNeeded(surah, ayah);
                }

                GlobalAudioManager.stopMainIfPlaying();

                if (player != null && playingPosition == position && player.isPlaying()) {
                    player.pause();
                    holder.btnPlay.setIconResource(R.drawable.ic_play_arrow);
                    Log.d("AUDIO_PLAY", "⏸️ Пауза на позиции " + position);
                    return;
                }

                releasePlayer();

                player = new ExoPlayer.Builder(context)
                        .setLoadControl(new DefaultLoadControl.Builder()
                                .setAllocator(new DefaultAllocator(true, 16))
                                .setBufferDurationsMs(2500, 5000, 1500, 2000)
                                .build())
                        .build();

                playingPosition = position;
                GlobalAudioManager.setTafsirPlayer(player);

                MediaItem mediaItem = MediaItem.fromUri(uri);
                ProgressiveMediaSource mediaSource =
                        new ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                                .createMediaSource(mediaItem);

                player.setMediaSource(mediaSource);
                player.prepare();
                player.play();

                holder.btnPlay.setIconResource(R.drawable.pause_24px);
                holder.audioProgress.setProgress(0);

                player.addListener(new Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int state) {
                        if (state == Player.STATE_READY || state == Player.STATE_BUFFERING) {
                            updateProgress(holder);
                        } else if (state == Player.STATE_ENDED) {
                            holder.audioProgress.setProgress(100);
                            holder.btnPlay.setIconResource(R.drawable.ic_play_arrow);
                            Log.d("AUDIO_PLAY", "✅ Завершено воспроизведение: " + uri);
                        }
                    }

                    @Override
                    public void onPlayerError(@NonNull androidx.media3.common.PlaybackException error) {
                        Log.e("AUDIO_PLAY", "❌ Ошибка воспроизведения", error);
                        Toast.makeText(context, "Ошибка: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        holder.btnPlay.setIconResource(R.drawable.ic_play_arrow);
                    }
                });
            } catch (Exception e) {
                Log.e("AUDIO_PLAY", "Ошибка при обработке нажатия", e);
                Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        holder.btnStop.setOnClickListener(v -> stopAudio(holder));

        // 📋 Копирование текста
        holder.itemView.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);
            StringBuilder sb = new StringBuilder();

            if (!isZeroAyah)
                sb.append("(").append(item.getSura()).append(":").append(item.getAyah()).append(")\n");
            if ((showArabic || isZeroAyah) && item.getArabic() != null)
                sb.append(item.getArabic());
            if (showTranslation && item.getTranslation() != null && !item.getTranslation().isEmpty())
                sb.append("\n").append(item.getTranslation());
            if (showComment && item.getTafsir() != null && !item.getTafsir().isEmpty())
                sb.append("\n\n").append(item.getTafsir());

            ClipData clip = ClipData.newPlainText("ayah", sb.toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Аят скопирован", Toast.LENGTH_SHORT).show();
        });

        // 🔸 Скрываем аудио, если это дуа
        if (isZeroAyah || isDuaAfterQuran) {
            holder.btnPlay.setVisibility(View.GONE);
            holder.btnStop.setVisibility(View.GONE);
            holder.audioProgress.setVisibility(View.GONE);
        } else {
            holder.btnPlay.setVisibility(View.VISIBLE);
            holder.btnStop.setVisibility(View.VISIBLE);
            holder.audioProgress.setVisibility(View.VISIBLE);
        }
    }

    private void updateProgress(ViewHolder holder) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying()) {
                    long pos = player.getCurrentPosition();
                    long dur = player.getDuration();
                    if (dur > 0) {
                        int progress = (int) (100f * pos / dur);
                        holder.audioProgress.setProgress(progress);
                    }
                    handler.postDelayed(this, 300);
                }
            }
        }, 300);
    }

    private void stopAudio(ViewHolder holder) {
        if (player != null) {
            player.stop();
            releasePlayer();
            holder.audioProgress.setProgress(0);
            holder.btnPlay.setIconResource(R.drawable.ic_play_arrow);
            playingPosition = -1;
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            if (GlobalAudioManager.getTafsirPlayer() == player) {
                GlobalAudioManager.setTafsirPlayer(null);
            }
            player = null;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
