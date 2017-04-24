package com.example.nikolay.mytranslater;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Кастомный адаптер
 */

public class StateAdapter extends ArrayAdapter<State>{
    private LayoutInflater inflater;
    private int layout;
    private List<State> states;
    private SQLiteDatabase db;        //экземляр базы, для обновления базы, во время изменения значения в CheckBox

    public StateAdapter(Context context, int resource, List<State> states){
        super(context,resource,states);
        this.states = states;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
        db = getContext().openOrCreateDatabase("Translatr.db",MODE_PRIVATE,null);
    }
    public View getView(final int position, View convertView, ViewGroup parent){
        View view = inflater.inflate(this.layout,parent,false);

        CheckBox checkView = (CheckBox) view.findViewById(R.id.checkbox);
        TextView textView = (TextView) view.findViewById(R.id.text);
        TextView textTranslate = (TextView) view.findViewById(R.id.textTranslate);
        TextView textLang = (TextView) view.findViewById(R.id.textLang);


        final State state = states.get(position);

        checkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox checkBox = (CheckBox) view;
                state.setCheck(checkBox.isChecked());
                ContentValues cv = new ContentValues();
                cv.put("favorite",state.isCheck()?1:0);
                //поиск в базе позиции по переведенному и переводимому словам
                Cursor cursor = db.rawQuery("select * from translation where word='"+state.getText()+"' and wordTranslate ='"+state.getTextTranslate()+"'",null);
                cursor.moveToFirst();
                //обновление значения в базе, после изменения состояния checkBox по id
                db.update("translation",cv,"id ="+cursor.getInt(0),null);
            }
        });
        checkView.setChecked(state.isCheck());
        textTranslate.setText(state.getTextTranslate());
        textView.setText(state.getText());
        textLang.setText(state.getLang());

        return view;
    }
    //переопределения фильтра, для поиска
    @NonNull
    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults fr = new FilterResults();
                List<State> temp = new ArrayList<>();
                if(charSequence == "" || charSequence.length() == 0) {
                    fr.values = states;
                    fr.count = states.size();
                }else{
                    for(State state: states){
                        //поиск по переводимым и переведенным словам
                        if(state.getTextTranslate().toLowerCase().contains(charSequence.toString().toLowerCase())
                                ||state.getText().toLowerCase().contains(charSequence.toString().toLowerCase())){
                            temp.add(state);
                        }
                    }
                    fr.values = temp;
                    fr.count = temp.size();
                }
                //возвращаем результат поиска
                return fr;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                //очищаем список
                states.clear();
                if (filterResults.count>0){                             //если фильр не пустой
                    states.addAll((List<State>) filterResults.values);  //добавляем в список
                    notifyDataSetChanged();                             //сохраняем изменения
                }
                else                                                    //иначе
                    notifyDataSetInvalidated();                         //не сохраняем

            }
        };
        return myFilter;
    }
}
