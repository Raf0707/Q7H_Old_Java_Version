package raf.console.quran7hours.model;

public class TafsirItem {
    public String sura;
    public String ayah;
    public String arabic;
    public String translation;
    public String tafsir;

    public TafsirItem(String sura, String ayah, String arabic, String translation, String tafsir) {
        this.sura = sura;
        this.ayah = ayah;
        this.arabic = arabic;
        this.translation = translation;
        this.tafsir = tafsir;
    }

    public String getSura() {
        return sura;
    }

    public String getAyah() {
        return ayah;
    }

    public String getArabic() {
        return arabic;
    }

    public String getTranslation() {
        return translation;
    }

    public String getTafsir() {
        return tafsir;
    }
}
