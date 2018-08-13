package haha.edatelcontrol.clases;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import haha.edatelcontrol.R;
import haha.edatelcontrol.interfaceDefineVariables;

import static android.content.Context.NOTIFICATION_SERVICE;
import static haha.edatelcontrol.interfaceDefineVariables.TBL_ABONOS_HECHOS;
import static haha.edatelcontrol.interfaceDefineVariables.TBL_ASISTENCIAS_MARCADAS;
import static haha.edatelcontrol.interfaceDefineVariables.TBL_PUNTOSDEVENTA;
import static haha.edatelcontrol.interfaceDefineVariables.TBL_VENTAS_REALIZADAS;
import static haha.edatelcontrol.interfaceDefineVariables.URL_WS_GUARDAR_ABONOS_REALIZADOS;
import static haha.edatelcontrol.interfaceDefineVariables.URL_WS_GUARDAR_ASISTENCIA;
import static haha.edatelcontrol.interfaceDefineVariables.URL_WS_GUARDAR_VENTAS_REALIZADAS;
import static haha.edatelcontrol.interfaceDefineVariables.URL_WS_VERIFICA_DEUDA_CLIENTE_ONLINE;
import static haha.edatelcontrol.interfaceDefineVariables.URL_WS_VERIFICA_NUEVA_VERSION_APLICACION;
import static haha.edatelcontrol.interfaceDefineVariables.URL_WS_VERIFICA_PUNTOS_NUEVOS;

/**
 * Created by User on 31/01/2018.
 */

public class classMetodosGenerales {

    private static Context mContext;

    //esta variable es para determinar si cuando se realice una peticion al servidor de claro, y este responda
    //abrir el resultado en la aplicacion, si es FALSE no es solicitado desde la app, si TRUE entonces es solicitado de la aplicacion, este valor se modifica en onresume, onpause y ondestroy
    public static boolean SolicitudDesdeLaAplicacion = false;

    SQLiteDatabase db;
    Float intDistanciaPdVMasCercvano = 1000f;
    SharedPreferences setting;
    classJSONParser jsonParser = new classJSONParser();
    String TAG = "MetodosGenerales";

    public static AccessibilityEvent event = null;

    public classMetodosGenerales(Context context) {
        mContext = context;
        setting = PreferenceManager.getDefaultSharedPreferences(context);
        db = new classSQLHELPER(context).getWritableDatabase();
    }

    public boolean TieneConexion(Context context) {
        boolean bConectado = false;
        try {
            ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] redes = connec.getAllNetworkInfo();
            for (int i = 0; i < 2; i++) {
                if (redes[i].getState() == NetworkInfo.State.CONNECTED && redes[i].isConnected() && redes[i].isAvailable()) {
                    bConectado = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(context).uncaughtException(e);
        }
        return bConectado;
    }

    public void Toast(Activity context, String Texto, int image) {
        LayoutInflater inflater = context.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_layout_toast, (ViewGroup) context.findViewById(R.id.toast_layout_root)); //"inflamos" nuestro layout
        TextView text = (TextView) layout.findViewById(R.id.text_toast);
        ImageView imgToast = (ImageView) layout.findViewById(R.id.imgToast);
        text.setText(Texto); //texto a mostrar y asignado al textView de nuestro layout
        imgToast.setImageResource(image);
        Toast toast = new Toast(context); //Instanciamos un objeto Toast
        toast.setGravity(Gravity.TOP, 10, 20); //lo situamos centrado arriba en la pantalla
        toast.setDuration(Toast.LENGTH_LONG); //duracion del toast
        toast.setView(layout); //asignamos nuestro layout personalizado al objeto Toast
        toast.show(); //mostramos el Toast en pantalla
    }

    //get the current version number and name
    public String getVersionInfo() {
        String versionName = "";
        int versionCode = -1;
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            versionName = packageInfo.versionName;
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return (String.format("%s", versionName));
    }

    /**
     * Este metodo es para quitar tildes y letras especiales por que el servidor no las admite
     *
     * @param input Es la cadena de texto con tildes recibida
     * @return La cadena de texto sin tilde ni caracteres especiales
     */
    public String quitarTildesEspeciales(String input) {

        String outpun = "";
        try {
            // Cadena de caracteres original a sustituir.
            String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
            // Cadena de caracteres ASCII que reemplazarán los originales.
            String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
            String output = input;
            for (int i = 0; i < original.length(); i++) {
                // Reemplazamos los caracteres especiales.
                output = output.replace(original.charAt(i), ascii.charAt(i));
            }//for i
            return output;

        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
            return "";
        }

    }

    public void VerificarNuevosPuntosguardados(final boolean mostrarinfo, final int idUsuario, final int idPdVActual) {

        class VerificarNuevosPuntosguardados extends AsyncTask<String, String, JSONObject> {

            ProgressDialog pDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                if (mostrarinfo) {
                    pDialog = new ProgressDialog(mContext);
                    pDialog.setMessage("Recuperando los datos. Espere...");
                    pDialog.setIndeterminate(true);
                    pDialog.setCancelable(true);
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            VerificarNuevosPuntosguardados.this.cancel(true);
                            new AlertDialog.Builder(mContext).setMessage("Se ha cancelado la petición.").setIcon(R.drawable.ic_info).show();
                        }
                    });
                    pDialog.show();
                }
            }

            @Override
            protected JSONObject doInBackground(String... strings) {

                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("idUsuario", String.valueOf(idUsuario)));
                params.add(new BasicNameValuePair("idPdVActual", String.valueOf(idPdVActual)));

                // getting JSON Object
                // Note that create product url accepts POST method
                JSONObject json = jsonParser.makeHttpRequest(URL_WS_VERIFICA_PUNTOS_NUEVOS, "POST", params);
                return json;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                try {

                    if (jsonObject == null) {
                        if (mostrarinfo) {
                            new AlertDialog.Builder(mContext).setTitle("Error al Obtener.").setMessage("No se ha podido actualizar clientes nuevos, pueda que no tengas internet.").setIcon(R.drawable.ic_error).show();
                            pDialog.dismiss();
                        }
                    } else {
                        int status = jsonObject.getInt("status");
                        String message = jsonObject.getString("message");
                        if (status == 1) {

                            if (idPdVActual == 0) {
                                //limpia la tabla de la base de datos
                                db.delete(TBL_PUNTOSDEVENTA, null, null);
                            }

                            JSONArray jsonArray = jsonObject.getJSONArray("Datos");

                            for (int i = 0; i < jsonArray.length(); i++) {        // PARA CADA PUNTO DE VENTA REGISTRADO

                                try {
                                    JSONObject c = jsonArray.getJSONObject(i);

                                    ContentValues values = new ContentValues();
                                    values.put("idpdv", c.getString("idpdv"));
                                    values.put("NombrePdV", c.getString("NombrePdV"));
                                    values.put("NombrePropietario", c.getString("NombrePropietario"));
                                    values.put("NumeroPOS", c.getString("NumeroPOS"));
                                    values.put("NumeroTelefono", c.getString("NumeroTelefono"));
                                    values.put("CoordenadasGPS", c.getString("CoordenadasGPS"));
                                    values.put("Ciudad", c.getString("Ciudad"));
                                    values.put("idDepartamento", c.getString("idDepartamento"));
                                    Long success = db.insert(TBL_PUNTOSDEVENTA, null, values); // se inserta en la base de datos local
                                    if (success > 0) {
                                        Log.w("guardar pdv", "Exito al guardar pdv " + c.getString("NombrePdV"));
                                    } else {
                                        Log.w("guardar pdv", "error al guardar pdv " + c.getString("NombrePdV"));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            if (mostrarinfo) {
                                pDialog.dismiss();
                                ((Activity) mContext).recreate();
                                Toast.makeText((Activity) mContext, "Se ha actualizado la lista con éxito", Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                } catch (JSONException e) {
                    pDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }

        new VerificarNuevosPuntosguardados().execute();
    }

    public void VerificarNuevaActualizacion() {

        class VerificarNuevaActualizacion extends AsyncTask<String, String, JSONObject> {

            @Override
            protected JSONObject doInBackground(String... strings) {

                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("versionActualInstalada", getVersionInfo()));

                // getting JSON Object
                // Note that create product url accepts POST method
                JSONObject jsonObject = jsonParser.makeHttpRequest(URL_WS_VERIFICA_NUEVA_VERSION_APLICACION, "POST", params);

                try {

                    if (jsonObject == null) {
                    } else {
                        int status = jsonObject.getInt("status");
                        String message = jsonObject.getString("message");
                        if (status == 1) {
                            try {
                                final String rutaapk = Environment.getExternalStorageDirectory().getAbsolutePath() + "/edatel/edatel" + jsonObject.getString("version") + ".apk";
                                File apk = new File(rutaapk);
                                if (apk.exists()) {

                                } else {
                                    int count;
                                    try {
                                        if (TieneConexion(mContext)) {
                                            URL url = new URL(jsonObject.getString("url_apk"));
                                            URLConnection conection = url.openConnection();
                                            conection.connect();
                                            int lenghtOfFile = conection.getContentLength();
                                            InputStream input = new BufferedInputStream(url.openStream(), 8192);

                                            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/edatel/");
                                            if (!file.exists()) {
                                                file.mkdirs();
                                            }

                                            OutputStream output = new FileOutputStream(rutaapk);
                                            byte data[] = new byte[1024];
                                            long total = 0;
                                            while ((count = input.read(data)) != -1) {
                                                total += count;
                                                publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                                                output.write(data, 0, count);
                                            }
                                            output.flush();
                                            output.close();
                                            input.close();
                                        } else {

                                        }
                                    } catch (Exception e) {
                                        Log.e("Error: ", e.getMessage());
                                    }

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonObject;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                try {
                    if (jsonObject == null) {
                    } else {
                        int status = jsonObject.getInt("status");
                        String message = jsonObject.getString("message");
                        if (status == 1) {

                            final String rutaapk = Environment.getExternalStorageDirectory().getAbsolutePath() + "/edatel/edatel" + jsonObject.getString("version") + ".apk";
                            File apk = new File(rutaapk);
                            if (apk.exists()) {

                                new AlertDialog.Builder(mContext)
                                        .setTitle(message)
                                        .setMessage(String.format(" La versión %s ha sido descargada y esta lista para instalarse\nVersión actual: %s", jsonObject.get("version"), getVersionInfo()))
                                        .setPositiveButton("INSTALAR AHORA", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Instalar(rutaapk);
                                            }
                                        })
                                        .show();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        new VerificarNuevaActualizacion().execute();
    }

    public void Instalar(String rutaapk) {

        SharedPreferences.Editor editor = setting.edit();
        editor.putInt("appversion", 0);
        editor.apply();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(rutaapk)), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

        ((Activity) mContext).finish();
    }

    /**
     * generada para guardar en la bese de datos online la asistencia del vendedor en un punto
     * de venta
     */
    public void CrearNuevaAsistenciaOnline(final boolean mostrarToast, final int IdAsistenciaMarcada, String... params) {

        final ProgressDialog pDialog = new ProgressDialog(mContext);

        class CrearNuevaAsistenciaOnline extends AsyncTask<String, String, JSONObject> {

            int idPdv;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (mostrarToast) {
                    pDialog.setMessage("Enviando los datos al servidor. Espere...");
                    pDialog.setIndeterminate(true);
                    pDialog.setCancelable(true);
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            CrearNuevaAsistenciaOnline.this.cancel(true);
                            new AlertDialog.Builder(mContext).setMessage("Se ha cancelado la petición.").setIcon(R.drawable.ic_info).show();
                        }
                    });
                    pDialog.show();
                }
            }

            @Override
            protected JSONObject doInBackground(String... strings) {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("IdUsuario", String.valueOf(strings[0])));
                params.add(new BasicNameValuePair("idPdV", String.valueOf(strings[1])));
                params.add(new BasicNameValuePair("Comentario", String.valueOf(strings[2])));
                params.add(new BasicNameValuePair("Fecharegistro", String.valueOf(strings[3])));

                idPdv = Integer.parseInt(strings[1]);

                // getting JSON Object
                JSONObject json = jsonParser.makeHttpRequest(URL_WS_GUARDAR_ASISTENCIA, "POST", params);
                return json;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                try {

                    if (jsonObject == null) {
                        if (mostrarToast) {
                            pDialog.dismiss();
                            Toast((Activity) mContext, "No se ha podido enviar la visita al servidor, puede que no tengas internet", R.drawable.ic_error);
                        }
                    } else {
                        int status = jsonObject.getInt("status");
                        String message = jsonObject.getString("message");
                        if (status == 1) {

                            int IdAsistencia = jsonObject.getInt("idGuardado");

                            ContentValues values = new ContentValues();
                            values.put("estado", 1);
                            values.put("IdEnviado", IdAsistencia);
                            int successUpdate = db.update(TBL_ASISTENCIAS_MARCADAS, values, "IdAsistencia = '" + IdAsistenciaMarcada + "'", null);
                            if (successUpdate > 0) {
                                if (mostrarToast) {
                                    pDialog.dismiss();
                                    Toast((Activity) mContext, message, R.drawable.ic_success);

                                    //verifica si el cliente tiene abonos pendientes de pagar
                                    Double saldo = VerificaClienteTieneSaldoPendientesDeAbonar(idPdv);
                                    if (saldo > 0) {
                                        NotificacionClienteTieneSaldoPendienteAbono(idPdv, saldo);
                                    }

                                    ((Activity) mContext).recreate();
                                }
                            }
                        } else {
                            if (mostrarToast) {
                                pDialog.dismiss();
                                Toast((Activity) mContext, message, R.drawable.ic_error);
                            }
                        }
                    }
                } catch (JSONException e) {
                    if (mostrarToast)
                        pDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }
        new CrearNuevaAsistenciaOnline().execute(params);
    }

    /**
     * Este metodo es para guardar en la bese de datos online la venta de la recarga realizada a un punto
     * de venta
     */
    public void EnviarVentaRealizadaaDBOnline(final boolean mostrarToast,
                                              final int IdVentarealizada,
                                              final String... params) {

        final ProgressDialog pDialog = new ProgressDialog(mContext);

        class EnviarVentaRealizadaaDBOnline extends AsyncTask<String, String, JSONObject> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (mostrarToast) {
                    pDialog.setMessage("Enviando los datos al servidor. Espere...");
                    pDialog.setIndeterminate(true);
                    pDialog.setCancelable(true);
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            EnviarVentaRealizadaaDBOnline.this.cancel(true);
                            new AlertDialog.Builder(mContext).setMessage("Se ha cancelado la petición.").setIcon(R.drawable.ic_info).show();
                        }
                    });
                    pDialog.show();
                }
            }

            @Override
            protected JSONObject doInBackground(String... strings) {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("IdUsuario", String.valueOf(strings[0])));
                params.add(new BasicNameValuePair("idPdV", String.valueOf(strings[1])));
                params.add(new BasicNameValuePair("NumeroPOS", String.valueOf(strings[2])));
                params.add(new BasicNameValuePair("Cantidad", String.valueOf(strings[3])));
                params.add(new BasicNameValuePair("FechaVenta", String.valueOf(strings[4])));
                params.add(new BasicNameValuePair("TipoVenta", String.valueOf(strings[5])));
                params.add(new BasicNameValuePair("TotalCobrarse", String.valueOf(strings[6])));

                // getting JSON Object
                JSONObject json = jsonParser.makeHttpRequest(URL_WS_GUARDAR_VENTAS_REALIZADAS, "POST", params);
                return json;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                try {
                    if (jsonObject == null) {
                        if (mostrarToast) {
                            pDialog.dismiss();
                            Toast((Activity) mContext, "No se ha podido enviar la venta al servidor, puede que no tengas internet", R.drawable.ic_error);
                        }
                    } else {
                        int status = jsonObject.getInt("status");
                        String message = jsonObject.getString("message");
                        if (status == 1) {

                            int IdVenta = jsonObject.getInt("idGuardado");

                            ContentValues values = new ContentValues();
                            values.put("estadoEnviado", 1);
                            values.put("IdEnviado", IdVenta);
                            int successUpdate = db.update(TBL_VENTAS_REALIZADAS, values, "idVenta = '" + IdVentarealizada + "'", null);
                            if (successUpdate > 0) {

                                if (mostrarToast)
                                    Toast((Activity) mContext, message, R.drawable.ic_success);
                                pDialog.dismiss();
                            }
                        } else {
                            if (mostrarToast)
                                pDialog.dismiss();
                            Toast((Activity) mContext, message, R.drawable.ic_error);
                        }
                    }
                } catch (JSONException e) {
                    if (mostrarToast)
                        pDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }
        new EnviarVentaRealizadaaDBOnline().execute(params);
    }


    public void EnviarAbonosRealizadosOnline(final boolean mostrarToast, final Long IdAbonorealizado, final String... params) {
        final ProgressDialog pDialog = new ProgressDialog(mContext);

        class EnviarAbonosRealizadosOnline extends AsyncTask<String, String, JSONObject> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (mostrarToast) {
                    pDialog.setMessage("Enviando los datos al servidor. Espere...");
                    pDialog.setIndeterminate(true);
                    pDialog.setCancelable(true);
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            EnviarAbonosRealizadosOnline.this.cancel(true);
                            new AlertDialog.Builder(mContext).setMessage("Se ha cancelado la petición.").setIcon(R.drawable.ic_info).show();
                        }
                    });
                    pDialog.show();
                }
            }

            @Override
            protected JSONObject doInBackground(String... strings) {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("IdUsuario", "" + setting.getInt("codUsuario", 0)));
                params.add(new BasicNameValuePair("idVenta", String.valueOf(strings[0])));
                params.add(new BasicNameValuePair("idCliente", String.valueOf(strings[1])));
                params.add(new BasicNameValuePair("cantidad_abono", String.valueOf(strings[2])));
                params.add(new BasicNameValuePair("cantidad_saldo", String.valueOf(strings[3])));
                params.add(new BasicNameValuePair("fecha_abono", String.valueOf(strings[4])));

                // getting JSON Object
                JSONObject json = jsonParser.makeHttpRequest(URL_WS_GUARDAR_ABONOS_REALIZADOS, "POST", params);
                return json;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                try {
                    if (jsonObject == null) {
                        if (mostrarToast) {
                            pDialog.dismiss();
                            Toast((Activity) mContext, "No se ha podido enviar el abono al servidor, puede que no tengas internet", R.drawable.ic_error);
                        }
                    } else {
                        int status = jsonObject.getInt("status");
                        String message = jsonObject.getString("message");
                        if (status == 1) {

                            int IdAbono = jsonObject.getInt("idGuardado");

                            ContentValues values = new ContentValues();
                            values.put("estadoEnviado", 1);
                            values.put("IdEnviado", IdAbono);
                            int successUpdate = db.update(TBL_ABONOS_HECHOS, values, "idAbono = '" + IdAbonorealizado + "'", null);
                            if (successUpdate > 0) {

                                if (mostrarToast)
                                    Toast((Activity) mContext, message, R.drawable.ic_success);
                                pDialog.dismiss();
                            }
                        } else {
                            if (mostrarToast)
                                pDialog.dismiss();
                            Toast((Activity) mContext, message, R.drawable.ic_error);
                        }
                    }
                } catch (JSONException e) {
                    if (mostrarToast)
                        pDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }
        new EnviarAbonosRealizadosOnline().execute(params);
    }

    /**
     * Este metodo es para verificar en la db online si el cliente tiene deudas
     *
     * @param params aqui viene el id del cliente
     */
    public void VerificarClienteDebeSaldoEnLinea(String... params) {

        final JSONObject joResutado = new JSONObject();

        class VerificarClienteDebeSaldoEnLinea extends AsyncTask<String, String, JSONObject> {

            private interfaceDefineVariables mCallback;

            public VerificarClienteDebeSaldoEnLinea() {
                this.mCallback = (interfaceDefineVariables) mContext;
            }

            @Override
            protected JSONObject doInBackground(String... strings) {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("IdUsuario", "" + setting.getInt("codUsuario", 0)));
                params.add(new BasicNameValuePair("idCliente", String.valueOf(strings[0])));

                // getting JSON Object
                JSONObject json = jsonParser.makeHttpRequest(URL_WS_VERIFICA_DEUDA_CLIENTE_ONLINE, "POST", params);
                return json;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                try {
                    if (jsonObject == null) {
                        Toast((Activity) mContext, "No se ha podido verificar la información o no tienes internet", R.drawable.ic_error);
                    } else {

                        int status = jsonObject.getInt("status");
                        String message = jsonObject.getString("message");
                        if (status == 1) {

                            /*int cantidapendientes = jsonObject.getInt("cantventapendcancelar");
                            double totalsaldo = jsonObject.getDouble("totalgnralsaldo");

                            joResutado.put("cantventapendcancelar", cantidapendientes);
                            joResutado.put("totalgnralsaldo", totalsaldo);*/

                            mCallback.onTaskComplete(jsonObject);

                        } else {
                            Toast((Activity) mContext, message, R.drawable.ic_error);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        new VerificarClienteDebeSaldoEnLinea().execute(params);

    }

    /**
     * Método creado para calcular la distancia entre mi dispositivo y los puntos de venta
     * obteniendo los puntos que tengan menor distancia
     *
     * @param LatLong Cadena con la latitud y longitud del dispositivo
     * @return True: si la distancia es menor que la seleccionada en configuracion de la app; False: si la
     * distancia es mayor que la seleccion de la configuracion dentro de la aplicacion
     */
    public boolean PdVCercano(String LatLong, Location mLastLocationMap) {
        try {
            if (mLastLocationMap != null) {
                String[] LatLng = LatLong.split(",");
                Location destino = new Location("dummyprovider");
                destino.setLatitude(Float.valueOf(LatLng[0]));
                destino.setLongitude(Float.valueOf(LatLng[1]));

                Float cerca = mLastLocationMap.distanceTo(destino);

                if (cerca < intDistanciaPdVMasCercvano) {
                    intDistanciaPdVMasCercvano = cerca;
                }

                Log.i("pdvcercano", "distancia: " + cerca + " metros. mas cercano a : " + intDistanciaPdVMasCercvano);

                if (cerca <= Integer.parseInt(setting.getString("stradiopdv", "100"))) {
                    return true;
                } else {
                    return false;
                }
            } else {
                Log.i("pdvcercano", "No se ha podido determinar la ubicación actual.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
            return false;
        }
    }

    // To check if service is enabled
    public boolean isAccessibilitySettingsOn() {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + USSDService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);

            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);

        } catch (Settings.SettingNotFoundException e) {
            String message = e.getMessage();
            Log.e(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
            e.printStackTrace();
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }

    public Double VerificaClienteTieneSaldoPendientesDeAbonar(int idcliente) {

        double saldototal = 0l;
        try {

            Cursor cpend = db.rawQuery("SELECT * FROM " + TBL_VENTAS_REALIZADAS + " WHERE Pdv = " + idcliente + " AND tipoVentaCntdCrdt = 'CREDITO'", null);

            for (cpend.moveToFirst(); !cpend.isAfterLast(); cpend.moveToNext()) {

                try {
                    //se obtine la cantidad a cobrarse
                    Double _totalCobrarse = cpend.getDouble(cpend.getColumnIndex("TotalCobrarse"));
                    if (_totalCobrarse < 1) {   // xcomo el campo no existia al inicios, puede que retorne un valor menor que 1

                        _totalCobrarse = (cpend.getDouble(cpend.getColumnIndex("Cantidad")) / 1.04); // entonces se le resta el 4%
                    }
                    //cursor para obtenerel total abonado
                    Cursor cTotalabono = db.rawQuery("select SUM(cantidad_abono) AS cantidad_abono from " + TBL_ABONOS_HECHOS + " WHERE idVentaLocal = " + cpend.getInt(cpend.getColumnIndex("idVenta")), null);
                    cTotalabono.moveToFirst();
                    saldototal += (_totalCobrarse - cTotalabono.getLong(0)); // total a cobrarse - total abonado

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
        }

        return saldototal;

    }

    public void NotificacionClienteTieneSaldoPendienteAbono(int idpdv, Double saldo) {

        // Sonido por defecto de notificaciones, podemos usar otro
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String titulo = "Has Marcado visita con éxito";
        String mensaje = "Se ha encontrado que este cliente tiene un saldo de C$" + saldo;

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(mContext);
        builder.setContentTitle(titulo)
                .setContentText(mensaje)
                .setContentInfo("ID CLIENTE: " + idpdv)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(defaultSound)
                .setLights(Color.RED, 1, 0)
                .setAutoCancel(true)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher));

        Notification.BigTextStyle n = new Notification.BigTextStyle(builder)
                .setBigContentTitle(titulo)
                .bigText(mensaje)
                .setSummaryText("Créditos");

        /*String[] lineList = new String[]{"", ""};
        for (String line : lineList) {
            n.addLine(line);
        }*/

        notificationManager.notify(idpdv, n.build());
    }

    public int CantidadDeCreditosQueTieneCliente(int idcliente) {

        int cantCreditos = 0;

        try {

            Cursor cpend = db.rawQuery("SELECT * FROM " + TBL_VENTAS_REALIZADAS + " WHERE Pdv = " + idcliente + " AND tipoVentaCntdCrdt = 'CREDITO'", null);

            for (cpend.moveToFirst(); !cpend.isAfterLast(); cpend.moveToNext()) {

                try {
                    //cursor para obtenerel total abonado
                    Cursor cTotalabono = db.rawQuery("select SUM(cantidad_abono) AS cantidad_abono from " + TBL_ABONOS_HECHOS + " WHERE idVentaLocal =" + cpend.getInt(cpend.getColumnIndex("idVenta")), null);
                    cTotalabono.moveToFirst();//pone el cursor en su primer ubicacion
                    Long saldo = (cpend.getInt(cpend.getColumnIndex("TotalCobrarse")) - cTotalabono.getLong(0)); // total vendido - total abonado
                    if (saldo > 0) {//si el saldo es mayor que cero es porq debe el cliente
                        cantCreditos++;    // aumenta la cantidad de creditos en 1
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
            return cantCreditos;
        }
        return cantCreditos;
    }

    public void clearData() {
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ((ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE)).clearApplicationUserData();
                ((Activity) mContext).recreate();
            } else {
                Toast(((Activity) mContext), "No soporta clear data memory", R.drawable.ic_error);
            }

        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
        }
    }

}
