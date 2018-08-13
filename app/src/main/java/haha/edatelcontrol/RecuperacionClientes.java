package haha.edatelcontrol;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import haha.edatelcontrol.clases.classClientesPDV;
import haha.edatelcontrol.clases.classJSONParser;
import haha.edatelcontrol.clases.classSQLHELPER;

import static haha.edatelcontrol.interfaceDefineVariables.TBL_PUNTOSDEVENTA;
import static haha.edatelcontrol.interfaceDefineVariables.URL_WS_GUARDAR_NUEVO_PUNTO;
import static haha.edatelcontrol.interfaceDefineVariables.URL_WS_RECUPERAR_PUNTO;

public class RecuperacionClientes extends AppCompatActivity {

    SharedPreferences setting;
    SQLiteDatabase db;
    ListView lvListaClientes;
    ArrayList<classClientesPDV> clientes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperacion_clientes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        setting = PreferenceManager.getDefaultSharedPreferences(this);
        db = new classSQLHELPER(this).getWritableDatabase();

        lvListaClientes = (ListView) findViewById(R.id.lvListaClientesRecuperar_RC);

        getData();


    }

    private void getData() {
        try {


            Cursor cCliente = db.rawQuery(String.format("select * from %s order by idpdv", TBL_PUNTOSDEVENTA), null);
            if (cCliente.getCount() > 0) {

                String[] mostrar = new String[cCliente.getCount()];

                for (cCliente.moveToFirst(); !cCliente.isAfterLast(); cCliente.moveToNext()) {
                    clientes.add(new classClientesPDV(
                            cCliente.getInt(cCliente.getColumnIndex("idpdv")),
                            cCliente.getString(cCliente.getColumnIndex("NombrePdV")),
                            cCliente.getString(cCliente.getColumnIndex("NombrePropietario")),
                            cCliente.getString(cCliente.getColumnIndex("NumeroPOS")),
                            cCliente.getString(cCliente.getColumnIndex("NumeroTelefono")),
                            cCliente.getString(cCliente.getColumnIndex("CoordenadasGPS")),
                            cCliente.getString(cCliente.getColumnIndex("Ciudad")),
                            cCliente.getInt(cCliente.getColumnIndex("idDepartamento"))
                    ));

                    mostrar[cCliente.getPosition()] = cCliente.getInt(cCliente.getColumnIndex("idpdv")) + ".:." + cCliente.getString(cCliente.getColumnIndex("NombrePdV")) + " - " + cCliente.getString(cCliente.getColumnIndex("NumeroPOS"));
                }


                // Create a List from String Array elements
                final List<String> clientes_list = new ArrayList<String>(Arrays.asList(mostrar));

                // Create an ArrayAdapter from List
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                        (this, android.R.layout.simple_list_item_1, clientes_list);

                // DataBind ListView with items from ArrayAdapter
                lvListaClientes.setAdapter(arrayAdapter);


                lvListaClientes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        new EnviarDatosDePdvAlServidor().execute(
                                "" + clientes.get(i).getIddDepto(),
                                clientes.get(i).getCiudad(),
                                clientes.get(i).getNombre(),
                                clientes.get(i).getPropietario(),
                                clientes.get(i).getNumero(),
                                clientes.get(i).getOtronumero(),
                                clientes.get(i).getCoordenadas(),
                                clientes.get(i).getId() + ""
                        );

                        clientes.remove(i);
                        clientes_list.remove(i);
                        arrayAdapter.notifyDataSetChanged();
                    }
                });
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class EnviarDatosDePdvAlServidor extends AsyncTask<String, String, JSONObject> {
        classJSONParser jsonParser = new classJSONParser();
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RecuperacionClientes.this);
            pDialog.setMessage("Enviando los datos al servidor. Espere...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    RecuperacionClientes.EnviarDatosDePdvAlServidor.this.cancel(true);
                    new AlertDialog.Builder(RecuperacionClientes.this).setMessage("Se ha cancelado la petición.").setIcon(R.drawable.ic_info).show();
                }
            });
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... strings) {

            // variables con los parametros a enviar
            String Departamento = strings[0];
            String Ciudad = strings[1];
            String NombrePdV = strings[2];
            String NombrePropiet = strings[3];
            String NumeroPOS = strings[4];
            String TelefonoPropiet = strings[5];
            String LocationGPS = strings[6];
            String idpdv = strings[7];
            String UserSave = "" + setting.getInt("codUsuario", 0);

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idpdv", idpdv));
            params.add(new BasicNameValuePair("idDepartamento", Departamento));
            params.add(new BasicNameValuePair("NombrePdV", NombrePdV));
            params.add(new BasicNameValuePair("NombrePropietario", NombrePropiet));
            params.add(new BasicNameValuePair("NumeroPOS", NumeroPOS));
            params.add(new BasicNameValuePair("NumeroTelefono", TelefonoPropiet));
            params.add(new BasicNameValuePair("Ciudad", Ciudad));
            params.add(new BasicNameValuePair("CoordenadasGPS", LocationGPS));
            params.add(new BasicNameValuePair("FechaRegistro", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
            params.add(new BasicNameValuePair("UsuarioGuardo", UserSave));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(URL_WS_RECUPERAR_PUNTO, "POST", params);
            return json;
        }

        protected void onPostExecute(JSONObject file_url) {
            // check for success tag
            try {

                SharedPreferences.Editor editor = setting.edit();

                if (file_url == null) {
                    new AlertDialog.Builder(RecuperacionClientes.this).setTitle("Error al Obtener.").setMessage("Sin repuesta del servidor, o revisa la conexión de datos.").setIcon(R.drawable.ic_error).show();
                } else {
                    int status = file_url.getInt("status");
                    String message = file_url.getString("message");
                    if (status == 1) {
                        //limpiarControles();
                        //si retorna un ok entonces se muestra el mensaje de guardado
                        new AlertDialog.Builder(RecuperacionClientes.this)
                                .setIcon(R.drawable.ic_success)
                                .setTitle("Guardado con éxito.")
                                .setMessage(message)
                                .setCancelable(true)
                                .show();
                    } else {
                        new AlertDialog.Builder(RecuperacionClientes.this)
                                .setIcon(R.drawable.ic_error)
                                .setTitle("Error al guardar.")
                                .setMessage(message)
                                .setCancelable(true)
                                .show();
                    }
                }
                pDialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                pDialog.dismiss();
            }
        }

    }
}
