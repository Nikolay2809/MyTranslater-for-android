package com.example.nikolay.mytranslater;

import android.os.AsyncTask;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


/**
 * Класс для осуществления перевода
 */

public class OkHttpHandler extends AsyncTask<String, Void, String[]> {
    private String key,     //поле api - ключ
                    lang,   //язык, с кого на какой
                    text;   //переводимая строка

    OkHttpClient client = new OkHttpClient();

    public OkHttpHandler(String key,String lang,String text){
        this.key = key;
        this.lang = lang;
        this.text = text;
    }
    @Override
    protected String[] doInBackground(String... strings) {

        RequestBody requestForm = new FormEncodingBuilder() //cоздание тела запроса
                .add("key", key)
                .add("lang", lang)
                .add("text", text)
                .build();
        Request request = new Request.Builder()             //сам запрос
                .url(strings[0]).post(requestForm)
                .build();
        try{
            Response response = client.newCall(request).execute(); //ответ
            String result = response.body().string();              //записываем ответ в строку
            if(key.contains("trns")){                              //проверяем ключ, если переводчик
                int start = result.indexOf("[")+2;
                int end = result.indexOf("]")-1;
                return  new String[]{result.substring(start,end)}; //возвращаем результат, который между скобками
            }
            else                                                   //иначе (ключ словарю)
                return parseJSONDictionary(result);                //парсим результат и возвращаем его

        } catch (IOException e) {
        }

        return null;
    }

    private String[] parseJSONDictionary(String json){                //парсинг json
        String trsltText = "",
                exTranslate = "";
        try {
            JSONObject dataJSONObj = new JSONObject(json);
            if(dataJSONObj.isNull("def"))
                return new String[]{""};
            JSONArray def = dataJSONObj.getJSONArray("def");        //находим массив def
            JSONObject text = def.getJSONObject(0);                 //у этого массива находим 0-ой объект
            JSONArray tr = text.getJSONArray("tr");                 //у этого объекта находим массив tr
            for (int i = 0; i < tr.length();i++){                   //в цикле проходим массив tr
                JSONObject words = tr.getJSONObject(i);             //находим объект
                if(trsltText.isEmpty())                             //находим основной перевод
                    trsltText= words.getString("text");             //записываем в trstText
                else
                    exTranslate += words.getString("text")+" \n";   //все остальные переводы в exTranslate
                JSONArray syns = words.getJSONArray("syn");         //находим массив syn
                for (int j = 0; j < syns.length();j++){             //обходим весь массив
                    JSONObject syn = syns.getJSONObject(j);
                    exTranslate+=syn.getString("text")+", ";        //добавляем к exTranslate
                }
                exTranslate+="\n";
                JSONArray means = words.getJSONArray("mean");       //то же самое с массивом mean
                for (int j = 0; j < means.length();j++){
                    JSONObject mean = means.getJSONObject(j);
                    exTranslate+=mean.getString("text")+", ";
                }

                exTranslate+="\n\n";
                JSONArray exs = words.getJSONArray("ex");           //и массивом ex
                for (int j = 0; j < exs.length();j++){
                    JSONObject ex = exs.getJSONObject(j);
                    exTranslate+=ex.getString("text") +" - ";
                    JSONArray tex = ex.getJSONArray("tr");
                    JSONObject te = tex.getJSONObject(0);
                    exTranslate+=te.getString("text") +"\n";
                }
                }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new String[]{trsltText,exTranslate};                 //возврат массива с найденными значениями
    }
}
