package com.example.nikolay.mytranslater;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.nikolay.mytranslater.R.id.listViewHistory;


public class FavoriteFragment extends Fragment {
    private EditText editTextFiltrFavorite;
    private TabLayout tabLayout;
    private SQLiteDatabase db;
    private StateAdapter stateAdapterFavorite;
    private ListView listViewFavorite;
    private List<State> statesFavorite = new ArrayList();
    public FavoriteFragment() {
    }

    public static FavoriteFragment newInstance() {
        FavoriteFragment fragment = new FavoriteFragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);                                //получаем ссылки на объекта
        db = getContext().openOrCreateDatabase("Translatr.db",MODE_PRIVATE,null);
        stateAdapterFavorite = new StateAdapter(getContext(),R.layout.state_layout, statesFavorite);
        editTextFiltrFavorite = (EditText)getActivity().findViewById(R.id.editTextFavorite);
        listViewFavorite = (ListView)getActivity().findViewById(R.id.listViewFavorite);
        tabLayout= (TabLayout)getActivity().findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition()==2){                                             //проверяем текущую вкладку
                        if (!editTextFiltrFavorite.getText().toString().isEmpty()){    //если есть фильтр
                            stateAdapterFavorite.getFilter().filter(editTextFiltrFavorite.getText().toString()); //филтруем адаптер
                        }
                        else{
                            fillListFavorite();                                         //заполняем адаптер
                        }
                        listViewFavorite.setAdapter(stateAdapterFavorite);              //подключаем адаптер
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        editTextFiltrFavorite.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(i2<i1)                                                   //если символов меньше чем было
                    fillListFavorite();                                     //заполняем адаптер заново
                if (!charSequence.toString().isEmpty())                     //если не пусто
                    stateAdapterFavorite.getFilter().filter(charSequence);  //фильтруем
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        listViewFavorite.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, int i, long l) {            //длительное нажатие на элемент списка
                final int position = i;
                final AlertDialog.Builder alertDialogRem = new AlertDialog.Builder(getContext());//создаем диалог
                alertDialogRem.setTitle(R.string.removing)
                        .setMessage(R.string.checkRemove)
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                State state = (State)adapterView.getItemAtPosition(position);   //получаем выбранный элемен в адаптере
                                Cursor cursor = db.rawQuery("select * from translation where word='"+   //нахдоим данный элемент в базе
                                        state.getText() + "' and wordTranslate ='" +
                                        state.getTextTranslate()+"'",null);
                                cursor.moveToFirst();
                                db.delete("translation","id ="+cursor.getInt(0),null);  //удаляем из базы
                                fillListFavorite();                                     //обновляем адаптер
                                listViewFavorite.deferNotifyDataSetChanged();           //обновляем список
                                dialogInterface.cancel();
                                if (!editTextFiltrFavorite.getText().toString().isEmpty())//если фильтр не пустой
                                    stateAdapterFavorite.getFilter().filter(editTextFiltrFavorite.getText().toString());//фильтруем
                            }
                        })
                        .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogRem.create();  //СОЗДАЕМ диалог
                alertDialog.show();                                 //показываем диалог
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View favoriteView = inflater.inflate(R.layout.fragment_favorite, container, false);
        return favoriteView;
    }

    private void fillListFavorite(){    //заполняем адаптер
        statesFavorite.clear();         //очищаем список с элементами
        Cursor cursor = db.rawQuery("select * from translation where favorite = 1",null); //находим в базе элементы, с 1 в favorite
        if(!cursor.moveToFirst())
            return;
        do{
            statesFavorite.add(new State(cursor.getString(1),                       //в цикле
                    cursor.getString(2),                                            //добавляем в список
                    cursor.getString(3),true));
        }while (cursor.moveToNext());
        stateAdapterFavorite.notifyDataSetChanged();                                //обновляем адаптер
    }
}
