package raf.console.quran7hours.model;

public class DictItem {
    private final String num;
    private final String ar;
    private final String root;
    private final String translate;
    private final String grammar;

    public DictItem(String num, String ar, String root, String translate, String grammar) {
        this.num = num;
        this.ar = ar;
        this.root = root;
        this.translate = translate;
        this.grammar = grammar;
    }

    public String getNum() {
        return num;
    }

    public String getAr() {
        return ar;
    }

    public String getRoot() {
        return root;
    }

    public String getTranslate() {
        return translate;
    }

    public String getGrammar() {
        return grammar;
    }
}
