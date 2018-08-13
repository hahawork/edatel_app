package haha.edatelcontrol;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import haha.edatelcontrol.clases.ListViewClienteAdapter;
import haha.edatelcontrol.clases.classClientesPDV;
import haha.edatelcontrol.clases.classSQLHELPER;

import static haha.edatelcontrol.interfaceDefineVariables.TBL_PUNTOSDEVENTA;


public class Fragmentestadocuentaclientes extends ListFragment {


    private OnArticuloSelectedListener listener;
    private ArrayList<classClientesPDV> stringArrayList;
    ListViewClienteAdapter adapter;
    SQLiteDatabase db;
    EditText etBuscar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_estadocuentaclientes, container, false);
        etBuscar = (EditText) view.findViewById(R.id.etBuscarcliente_EC);

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String criterio = TextUtils.isEmpty(etBuscar.getText()) ? "" : etBuscar.getText().toString();
                LlenarLista(criterio);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new classSQLHELPER(getContext()).getWritableDatabase();

        LlenarLista("");
        //setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, valores));
    }

    private void LlenarLista(String criterio) {
        setData(criterio);
        adapter = new ListViewClienteAdapter(getActivity(), R.layout.item_listview, stringArrayList);
        setListAdapter(adapter);
    }

    private void setData(String criterio) {

        stringArrayList = new ArrayList<>();

        try {

            Cursor cPdv = db.rawQuery("SELECT idpdv, NombrePdV, NumeroPOS FROM "+TBL_PUNTOSDEVENTA+" WHERE NombrePdV LIKE '%" + criterio +
                    "%' or NumeroPOS like '%" + criterio + "%' ORDER BY NombrePdV", null);

            if (cPdv.getCount() > 0) {

                for (cPdv.moveToFirst(); !cPdv.isAfterLast(); cPdv.moveToNext()) {

                    stringArrayList.add(new classClientesPDV(
                            cPdv.getInt(0),
                            cPdv.getString(1),
                            cPdv.getString(2)));

                }
            }
            //**********************************************************************

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public interface OnArticuloSelectedListener {
        public void onArticuloSelected(String str);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnArticuloSelectedListener) activity;
        } catch (ClassCastException e) {
        }
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        String jsonCliente = String.format("{'idpdv':'%s', 'NombrePdV':'%s', 'NumeroPOS':'%s'}",
                stringArrayList.get(position).getId(),
                stringArrayList.get(position).getNombre(),
                stringArrayList.get(position).getNumero());

        listener.onArticuloSelected(jsonCliente);
    }


}