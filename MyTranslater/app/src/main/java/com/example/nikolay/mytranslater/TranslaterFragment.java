package com.example.nikolay.mytranslater;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.content.Context.MODE_PRIVATE;
import static android.widget.Toast.*;
import static com.example.nikolay.mytranslater.R.id.checkBoxFavorite;
import static com.example.nikolay.mytranslater.R.id.text;
import static com.example.nikolay.mytranslater.R.id.textViewOptions;
import static com.example.nikolay.mytranslater.R.id.textViewTranslated;
//Фрагмент перевода
public class TranslaterFragment extends Fragment {

    private SQLiteDatabase db;              //база
    private TextView textViewLang,          //текстовое поле смены языка
                     textViewTranslated,    //текстовое поле с переводенным строкой
                     textViewOptions;       //текстовое поле с дополнительным переводом, синонимами
    private EditText editTextTranslate;
    private CheckBox checkBoxFavorite;
    private Button buttonDone,
                   buttonClear;
    private String lang = "ru-en";
    private String Lang = "Русский <-> Английский";
    //ключ переводчика
    private final String API_KEY_T = "trnsl.1.1.20170326T112649Z.29e0d54333d8fdcc.df37d853a191ae0460550bf79d06e7ecced710bf";
    //путь к переводчику
    private final String PATH_T = "https://translate.yandex.net/api/v1.5/tr.json/translate";
    //ключ словаря
    private final String API_KEY_D = "dict.1.1.20170419T140706Z.aa5e8c729cdab0f8.a3979c4bbb3c18c999579f313a2532f3567c772c";
    //путь к словарю
    private final String PATH_D = "https://dictionary.yandex.net/api/v1/dicservice.json/lookup";
    private String currentText = "",            //текущая переводимая строка
                   currentTranslation = "";     //текущая переведенная строка

    public TranslaterFragment() {

    }

    public static TranslaterFragment newInstance() {
        TranslaterFragment fragment = new TranslaterFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View translaterView = inflater.inflate(R.layout.fragment_translater, container, false);
        db = getContext().openOrCreateDatabase("Translatr.db",MODE_PRIVATE,null);
        textViewLang = (TextView)translaterView.findViewById(R.id.textViewLang);
        textViewLang.setText(Lang);
        buttonDone = (Button)translaterView.findViewById(R.id.buttonDone);
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToHistory();
            }
        });
        buttonClear = (Button)translaterView.findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearEditText();
                checkBoxFavorite.setChecked(false);
            }
        });
        checkBoxFavorite = (CheckBox)translaterView.findViewById(R.id.checkBoxFavorite);
        editTextTranslate = (EditText)translaterView.findViewById(R.id.editTextTranslate);
        textViewTranslated = (TextView)translaterView.findViewById(R.id.textViewTranslated);
        textViewLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeLang();
            }
        });
        textViewOptions = (TextView)translaterView.findViewById(R.id.textViewOptions);
        editTextTranslate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {            //если удалился текст из editText
                    textViewTranslated.setText("");                 //очищаем текстовые поля с переводми
                    textViewOptions.setText("");
                    buttonDone.setVisibility(View.INVISIBLE);       //делаем кнопку невидимой
                    return;                                         //выходим
                }
                buttonDone.setVisibility(View.VISIBLE);             //делаем кнопку видимой
                String text = charSequence.toString();
                String[] result = null;
                try {
                    OkHttpHandler okHttpHandler = new OkHttpHandler(API_KEY_D, lang, text); //запрашиваем перед у словаря
                    result = okHttpHandler.execute(PATH_D).get();                           //получаем перевод
                    if(result[0].isEmpty()){                                                //и если перевод = пустая строка
                        okHttpHandler = new OkHttpHandler(API_KEY_T, lang, text);           //запрашиваем перевод у переводчика
                        result = okHttpHandler.execute(PATH_T).get();                       //получаем перевод
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                currentText = text;                                                         //задаем текущую переводимую строку
                currentTranslation = result[0];                                             //задаем текущую переденную строку
                textViewTranslated.setText(text +"\n"+ result[0]);                          //отображаем перевод
                textViewOptions.setText(result.length>1?result[1]:"");                      //отображаем синонимы, если есть
                textViewOptions.append(getString(R.string.yandexTranslater));
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return translaterView;
    }
    private void addToHistory(){                                                // добавление в историю поиска
        if (    !currentText.isEmpty()&&                                        //если текущая переводимая строка не пустая
                !currentTranslation.isEmpty()){                                 //и текущее переведенная строка не пустаю
            ContentValues contentValues = new ContentValues();                  //создаем словарь
            contentValues.put("word",currentTranslation);                       //добавляем ключ + значени
            contentValues.put("wordTranslate",currentText);
            contentValues.put("lang",lang);
            contentValues.put("favorite",checkBoxFavorite.isChecked()?1:0);
            try {
                db.insertOrThrow("translation", null, contentValues);                 //добавляем в базу
            }catch (Exception e){e.printStackTrace();}
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(buttonDone.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);                        //скрываем клавитуру
            currentTranslation = currentText = "";                              //обнуляем текущие значения
            buttonDone.setVisibility(View.INVISIBLE);                           //делаем кнопку невидимой
        }
    }
    private void changeLang(){                      //инвертирование языка перевода
        if (lang.contains("ru-en")){
            lang = "en-ru";
            Lang = "Английский <-> Русский";
        }
        else{
            lang = "ru-en";
            Lang = "Русский <-> Английский";
        }
        textViewLang.setText(Lang);
        if(!editTextTranslate.getText().toString().isEmpty()){          //обновляем поле ввода, если не пустое
            editTextTranslate.append(" ");
            editTextTranslate.getText().delete(editTextTranslate.getText().length()-1,editTextTranslate.getText().length());
        }
    }
    private void clearEditText(){               //очистка поля ввода
        editTextTranslate.setText("");
    }
}
