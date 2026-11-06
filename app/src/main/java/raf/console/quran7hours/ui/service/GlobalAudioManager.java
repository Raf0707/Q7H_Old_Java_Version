package raf.console.quran7hours.ui.service;

import androidx.media3.exoplayer.ExoPlayer;

/**
 * 🎧 GlobalAudioManager — глобальный менеджер для синхронизации плееров:
 * - Основной плеер (служба PlayerService)
 * - Тафсир-плеер (локальный ExoPlayer в TafsirAdapter)
 *
 * Гарантирует, что одновременно играет только один поток.
 */
public class GlobalAudioManager {

    private static ExoPlayer mainPlayer;
    private static ExoPlayer tafsirPlayer;

    /** Устанавливает основной плеер и гарантирует остановку тафсир-плеера */
    public static void setMainPlayer(ExoPlayer player) {
        // Если играет тафсир — останавливаем
        if (tafsirPlayer != null && tafsirPlayer.isPlaying()) {
            tafsirPlayer.stop();
        }
        mainPlayer = player;
    }

    /** Устанавливает плеер тафсира и гарантирует остановку основного */
    public static void setTafsirPlayer(ExoPlayer player) {
        // Если играет основной плеер — ставим на паузу
        if (mainPlayer != null && mainPlayer.isPlaying()) {
            mainPlayer.pause();
        }
        tafsirPlayer = player;
    }

    /** Возвращает активный плеер тафсира */
    public static ExoPlayer getTafsirPlayer() {
        return tafsirPlayer;
    }

    /** Возвращает активный основной плеер */
    public static ExoPlayer getMainPlayer() {
        return mainPlayer;
    }

    /** Останавливает основной плеер, если он играет */
    public static void stopMainIfPlaying() {
        if (mainPlayer != null && mainPlayer.isPlaying()) {
            mainPlayer.pause();
        }
    }

    /** Останавливает тафсир-плеер, если он играет */
    public static void stopTafsirIfPlaying() {
        if (tafsirPlayer != null && tafsirPlayer.isPlaying()) {
            tafsirPlayer.stop();
        }
    }

    /** Освобождает ссылки, когда приложение закрывается */
    public static void clear() {
        mainPlayer = null;
        tafsirPlayer = null;
    }
}
