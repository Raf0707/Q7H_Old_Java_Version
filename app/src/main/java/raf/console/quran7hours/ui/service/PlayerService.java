package raf.console.quran7hours.ui.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import raf.console.quran7hours.R;


/**
 * 🎧 PlayerService — главный поток воспроизведения Корана.
 * Работает как ForegroundService, использует WakeLock/WifiLock,
 * управляется через уведомление и синхронизируется с тафсиром.
 */
public class PlayerService extends Service {

    private ExoPlayer exoPlayer;
    private MediaSessionCompat mediaSession;
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        // ⚙️ Инициализация ExoPlayer
        exoPlayer = new ExoPlayer.Builder(this).build();
        GlobalAudioManager.setMainPlayer(exoPlayer);

        // 🎛️ MediaSession
        mediaSession = new MediaSessionCompat(this, "PlayerService");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(new MediaSessionCallback());
        mediaSession.setActive(true);

        // ⚡ Не даем устройству и Wi-Fi уснуть
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Quran7Hours::AudioWakeLock");
        wakeLock.acquire();

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "Quran7Hours::WifiLock");
        wifiLock.acquire();

        // 🔁 Автовосстановление при ошибке
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                exoPlayer.prepare();
                exoPlayer.play();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case "PLAY":
                    GlobalAudioManager.stopTafsirIfPlaying();
                    exoPlayer.play();
                    break;
                case "PAUSE":
                    exoPlayer.pause();
                    break;
                case "STOP":
                    exoPlayer.stop();
                    stopSelf();
                    break;
            }
        }

        // 🪶 Обновляем уведомление
        showNotification();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        if (mediaSession != null) {
            mediaSession.release();
        }
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        if (wifiLock != null && wifiLock.isHeld()) wifiLock.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "player_channel",
                    "Player Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Аудиоплеер Корана");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private PendingIntent makeIntent(String action) {
        Intent i = new Intent(this, PlayerService.class);
        i.setAction(action);
        return PendingIntent.getService(this, action.hashCode(), i, PendingIntent.FLAG_IMMUTABLE);
    }

    public void showNotification() {
        MediaItem currentMediaItem = exoPlayer.getCurrentMediaItem();
        String title = (currentMediaItem != null && currentMediaItem.mediaMetadata.title != null)
                ? currentMediaItem.mediaMetadata.title.toString()
                : "Коран — воспроизведение";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "player_channel")
                .setSmallIcon(R.drawable.ic_ahmad_diban_transponent_n)
                .setContentTitle(title)
                .setContentText("Когда читается Коран, слушайте его и храните молчание — быть может, вас помилуют (7:204)")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.album_art))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .addAction(R.drawable.play_arrow_24px, "Play", makeIntent("PLAY"))
                .addAction(R.drawable.pause_24px, "Pause", makeIntent("PAUSE"))
                .addAction(R.drawable.stop_play_fill, "Stop", makeIntent("STOP"))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(exoPlayer.isPlaying());

        startForeground(1, builder.build());
    }

    class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            GlobalAudioManager.stopTafsirIfPlaying();
            exoPlayer.play();
            showNotification();
        }

        @Override
        public void onPause() {
            exoPlayer.pause();
            showNotification();
        }

        @Override
        public void onStop() {
            exoPlayer.stop();
            stopSelf();
        }
    }
}
