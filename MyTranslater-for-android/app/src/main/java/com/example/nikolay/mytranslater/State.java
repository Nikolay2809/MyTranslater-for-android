package com.example.nikolay.mytranslater;

/**
 * Класс для кастомного адаптера, StateAdapter
 */

public class State {
    private String text,textTranslate,lang;   //поля для TextViews
    private boolean check;                    //поле для CheckBox

    public State (String text, String textTranslate, String lang, boolean check){
        this.text = text;
        this.textTranslate = textTranslate;
        this.check = check;
        this.lang = lang;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTextTranslate() {
        return textTranslate;
    }

    public void setTextTranslate(String textTranslate) {
        this.textTranslate = textTranslate;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }
}
