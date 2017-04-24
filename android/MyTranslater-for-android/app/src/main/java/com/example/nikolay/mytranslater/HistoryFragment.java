package com.example.nikolay.mytranslater;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class HistoryFragment extends Fragment {

    private EditText editTextFiltrHistory;
    private StateAdapter stateAdapterHistory;
    private ListView listViewHistory;
    private SQLiteDatabase db;
    private TabLayout tabLayout;
    private List<State> statesHistory = new ArrayList();    //список элементов истории
    public HistoryFragment() {
    }

    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View historyView = inflater.inflate(R.layout.fragment_history, container, false);

        return historyView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);                                    //получение ссылок
        db = getContext().openOrCreateDatabase("Translatr.db",MODE_PRIVATE,null);
        listViewHistory = (ListView)getActivity().findViewById(R.id.listViewHistory);
        stateAdapterHistory = new StateAdapter(getContext(),R.layout.state_layout, statesHistory);
        tabLayout= (TabLayout)getActivity().findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition()==1) {                           //перешли на данную вкладку
                    if (!editTextFiltrHistory.getText().toString().isEmpty())          //поле ввода текста поиска не пустое
                        stateAdapterHistory.getFilter().filter(editTextFiltrHistory.getText().toString());  //фильтруем
                    else
                        fillListHistory();                                              //заполняем адаптер
                    listViewHistory.setAdapter(stateAdapterHistory);                    //задаем адаптер
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        editTextFiltrHistory = (EditText)getActivity().findViewById(R.id.editTextHistory);
        editTextFiltrHistory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {  //поиск по истории
                if(i2<i1)                                                                  //если симвлов стало меньше чем было
                    fillListHistory();                                                     //заполняем адаптер полностью
                if (!charSequence.toString().isEmpty()){                                   //если не пуст измененный текс
                    stateAdapterHistory.getFilter().filter(charSequence);                  //фильтруем                                                //снимаем флаг, данные фильтрованы
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        listViewHistory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, int i, long l) {                    //длительное нажатие по элементу списка
                final int position = i;
                final AlertDialog.Builder alertDialogRem = new AlertDialog.Builder(getContext());    //создаем диалог удаления
                alertDialogRem.setTitle(R.string.removing)
                        .setMessage(R.string.checkRemove)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                State state = (State)adapterView.getItemAtPosition(position);        //находим в адаптере нажатый объект
                                Cursor cursor = db.rawQuery("select * from translation where word='" //находи в базе
                                        + state.getText() + "' and wordTranslate ='"
                                        + state.getTextTranslate() + "'", null);
                                cursor.moveToFirst();
                                db.delete("translation","id ="+cursor.getInt(0),null);               //по id удаляем
                                fillListHistory();                                                   //заполняем адаптер
                                listViewHistory.deferNotifyDataSetChanged();                         //обновляем список
                                dialogInterface.cancel();                                            //закрываем дилог
                                if (!editTextFiltrHistory.getText().toString().isEmpty())            //если поле поиска не пусто
                                    stateAdapterHistory.getFilter().filter(editTextFiltrHistory.getText().toString());//фильруем
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogRem.create();        //создаем диалог
                alertDialog.show();                                       //отображаем
                return false;
            }
        });
    }
    private void fillListHistory(){                                     //заполнение адаптера
        statesHistory.clear();                                          //очищаем список
        Cursor cursor = db.rawQuery("select * from translation",null);  //получаем курсор с элементами базы
        if(!cursor.moveToFirst())
            return;
        do{
            statesHistory.add(new State(cursor.getString(1),cursor.getString(2),    //в цикле добавляем в список
                    cursor.getString(3),cursor.getInt(4)==1));                      //элементы из базы
        }while (cursor.moveToNext());
        stateAdapterHistory.notifyDataSetChanged();                     //обновляем адаптер
    }
}
