package haha.edatelcontrol;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.Explode;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import haha.edatelcontrol.clases.classAlterDB;
import haha.edatelcontrol.clases.classJSONParser;
import haha.edatelcontrol.clases.classMetodosGenerales;
import haha.edatelcontrol.clases.classSQLHELPER;

import static haha.edatelcontrol.interfaceDefineVariables.TBL_PUNTOSDEVENTA;
import static haha.edatelcontrol.interfaceDefineVariables.TBL_VENTAS_REALIZADAS;
import static haha.edatelcontrol.interfaceDefineVariables.URL_WS_OBTENER_DATOS_USUARIO;

public class frmHome extends AppCompatActivity {

    final int REQUEST_CODE_ASK_PERMISSIONS = 3;

    SharedPreferences setting;
    SQLiteDatabase db;
    classMetodosGenerales cmg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //PruebaFormatosFecha();
        setting = PreferenceManager.getDefaultSharedPreferences(this);
        setToolbar(); // Reemplazar toolbar
        setupWindowAnimations(); // Añadir animaciones
        EnableRuntimePermission();

        if (setting.getInt("codUsuario", 0) > 0) {

            iniciarInstancias();

            eventosControles(); // carga  los eventos a los controles

            ModificarBD();

            //verifica si esta habilitadol la accesibilidad
            if (!cmg.isAccessibilitySettingsOn()) {
                new AlertDialog.Builder(this)
                        .setTitle("Permiso necesario")
                        .setMessage("El siguiente permiso es para capturar automaticamente el mensaje que devuelve el sistema de claro.\n" +
                                "Si desea conceder el permiso, presione CONCEDER y luego active EDATEL.")
                        .setPositiveButton("CONCEDER", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                            }
                        })
                        .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();

            }

        } else {
            DialogoIntroduccion();
        }
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupWindowAnimations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setReenterTransition(new Explode());
            getWindow().setExitTransition(new Explode().setDuration(500));
        }
    }

    private void iniciarInstancias() {

        db = new classSQLHELPER(this).getWritableDatabase();

        cmg = new classMetodosGenerales(this);

        if (cmg.TieneConexion(frmHome.this)) {
            VerificarNuevosPuntosguardados();
            cmg.VerificarNuevaActualizacion();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //VerificarNuevosPuntosguardados();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void eventosControles() {

        ((Button) findViewById(R.id.btnGuardarPdv_MA)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(frmHome.this, frmGuardarPuntoVenta.class), ActivityOptions
                            .makeSceneTransitionAnimation(frmHome.this).toBundle());
                } else {
                    startActivity(new Intent(frmHome.this, frmGuardarPuntoVenta.class));
                }
            }
        });

        ((Button) findViewById(R.id.btnVender)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(frmHome.this, frmVender.class), ActivityOptions
                            .makeSceneTransitionAnimation(frmHome.this).toBundle());
                } else {
                    startActivity(new Intent(frmHome.this, frmVender.class));
                }
            }
        });

        ((Button) findViewById(R.id.btnMarcar_MA)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(frmHome.this, frmMarcarVisitas.class), ActivityOptions
                            .makeSceneTransitionAnimation(frmHome.this).toBundle());
                } else {
                    startActivity(new Intent(frmHome.this, frmMarcarVisitas.class));
                }
            }
        });

        ((Button) findViewById(R.id.btnReportes_MA)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(frmHome.this, frmReportes.class), ActivityOptions
                            .makeSceneTransitionAnimation(frmHome.this).toBundle());
                } else {
                    startActivity(new Intent(frmHome.this, frmReportes.class));
                }
            }
        });

        ((Button) findViewById(R.id.btnConfiguracion_MA)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(frmHome.this, frmConfiguracion.class), ActivityOptions
                            .makeSceneTransitionAnimation(frmHome.this).toBundle());
                } else {
                    startActivity(new Intent(frmHome.this, frmConfiguracion.class));
                }
            }
        });
        ((Button) findViewById(R.id.btnEstadoCuenta_H)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(frmHome.this, frmEstadoCuenta.class), ActivityOptions
                            .makeSceneTransitionAnimation(frmHome.this).toBundle());
                } else {
                    startActivity(new Intent(frmHome.this, frmEstadoCuenta.class));
                }
            }
        });
    }


    /**
     * Dialogo  para guardar el codigo, nombre y asignacion del usuario la primera vez que entra a  la aplicacion.
     */
    public Dialog dialogIntro = null;

    public void DialogoIntroduccion() {
        dialogIntro = new Dialog(this);
        dialogIntro.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialogIntro.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialogIntro.getWindow();
        lp.copyFrom(window.getAttributes());
        dialogIntro.setCancelable(false);
        dialogIntro.setCanceledOnTouchOutside(false);
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        dialogIntro.setContentView(R.layout.dg_configuracion);

        ImageButton btnVerificar = (ImageButton) dialogIntro.findViewById(R.id.btnVerificarCorreo_MA);
        final EditText etCorreo = (EditText) dialogIntro.findViewById(R.id.etCorreo_MA);
        TextView tvCerrar = (TextView) dialogIntro.findViewById(R.id.tvCerrarDialogo);


        btnVerificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = TextUtils.isEmpty(etCorreo.getText()) ? "" : etCorreo.getText().toString();
                if (correo.length() > 0) {

                    if (new classMetodosGenerales(frmHome.this).TieneConexion(frmHome.this)) {

                        new ObtenerDatosdelUsuarioOnline().execute(correo);

                    } else {
                        new classMetodosGenerales(frmHome.this).Toast(frmHome.this, "No hay acceso a internet", R.drawable.ic_error);
                    }
                } else {
                    ((TextInputLayout) dialogIntro.findViewById(R.id.tilCorreo_MA)).setError("(*) Este campo es requerido");
                }
            }
        });

        Button close = (Button) dialogIntro.findViewById(R.id.btnCancelar_DGMA);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogIntro.cancel();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    frmHome.this.finishAffinity();
                } else {
                    frmHome.this.finish();
                }
            }
        });
        dialogIntro.show();

        tvCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (setting.getInt("codUsuario", 0) == 0) {
                    dialogIntro.cancel();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        frmHome.this.finishAffinity();
                    } else {
                        frmHome.this.finish();
                    }
                } else {
                    dialogIntro.cancel();
                }
            }
        });
    }

    private void VerificarNuevosPuntosguardados() {
        try {

            Cursor cMaxidGuardado = db.rawQuery("SELECT MAX(idpdv) AS idpdv FROM " + TBL_PUNTOSDEVENTA, null);
            if (cMaxidGuardado.getCount() > 0) {
                cMaxidGuardado.moveToFirst();
                new classMetodosGenerales(this).VerificarNuevosPuntosguardados(
                        false,
                        setting.getInt("codUsuario", 0),
                        cMaxidGuardado.getInt(0));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void EnableRuntimePermission() {


        ActivityCompat.requestPermissions(this, new String[]
                {
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.GET_ACCOUNTS,
                        android.Manifest.permission.READ_CONTACTS,
                        android.Manifest.permission.READ_PHONE_STATE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CALL_PHONE
                }, REQUEST_CODE_ASK_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:

                break;
        }
    }

    public class ObtenerDatosdelUsuarioOnline extends AsyncTask<String, String, JSONObject> {

        classJSONParser jsonParser = new classJSONParser();
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(frmHome.this);
            pDialog.setMessage("Recuperando los datos. Espere...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    ObtenerDatosdelUsuarioOnline.this.cancel(true);
                    new AlertDialog.Builder(frmHome.this).setMessage("Se ha cancelado la petición.").setIcon(R.drawable.ic_info).show();
                }
            });
            pDialog.show();
        }

        protected JSONObject doInBackground(String... args) {

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("Email", args[0]));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(URL_WS_OBTENER_DATOS_USUARIO, "POST", params);
            return json;
        }

        protected void onPostExecute(JSONObject file_url) {
            // check for success tag
            try {

                SharedPreferences.Editor editor = setting.edit();

                if (file_url == null) {
                    new AlertDialog.Builder(frmHome.this).setTitle("Error al Obtener.").setMessage("Sin repuesta del servidor, o revisa la conexión de datos.").setIcon(R.drawable.ic_error).show();
                } else {
                    int status = file_url.getInt("status");
                    if (status == 1) {
                        editor.putInt("codUsuario", file_url.getInt("IdUsuario"));
                        editor.putString("nombUsuario", file_url.getString("NombreUsuario"));
                        editor.commit();
                        recreate();
                        dialogIntro.dismiss();
                    } else {
                        new AlertDialog.Builder(frmHome.this).setTitle("Error al Recuperar.").setMessage(file_url.getString("message")).setIcon(R.drawable.ic_error).show();
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

    private void ModificarBD() {

        classAlterDB modificarDB = new classAlterDB(this);

        // una variable para cada tabla si necesitase actualizar  dato de esa tabla cuando se cambie un campo.
        boolean modificopuntosdeventa = false;

        if (!modificarDB.ExisteColumna(TBL_PUNTOSDEVENTA, "Ciudad")) {
            modificarDB.AgregarColumna(TBL_PUNTOSDEVENTA, "Ciudad ", "text");
            modificopuntosdeventa = true;
        }
        if (!modificarDB.ExisteColumna(TBL_PUNTOSDEVENTA, "idDepartamento")) {
            modificarDB.AgregarColumna(TBL_PUNTOSDEVENTA, "idDepartamento", "integer");
            modificopuntosdeventa = true;
        }

        if (!modificarDB.ExisteColumna(TBL_VENTAS_REALIZADAS,"TotalCobrarse")){
            modificarDB.AgregarColumna(TBL_VENTAS_REALIZADAS,"TotalCobrarse","decimal(10,2)");
        }
        //inicialmente no se habia creado, por eso se hace desde aqui, pero tambien en la clase helper
        modificarDB.AgregarTabla(new classSQLHELPER(this).tablaabonos);

        //si se modifico la tabla de puntos de venta
        if (modificopuntosdeventa) {
            cmg.VerificarNuevosPuntosguardados(true, setting.getInt("codUsuario", 0), 0);
        }
    }

    private void PruebaFormatosFecha() {
        try {
            Date date = null;
            String dtStart = "2018-03-14 14:34:56";
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = format.parse(dtStart);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            StringBuilder fehas = new StringBuilder();
            String[] formatos = new String[]{
                    "yyyy-MM-dd HH:mm:ss",
                    "EEE, MMMM d, ''yy",
                    "h:mm a",
                    "EEE, d MMM yyyy HH:mm:ss",

            };
            for (String formato : formatos) {

                fehas.append(new SimpleDateFormat(formato).format(date) + "\n");
            }

            fehas.append(DateFormat.getDateInstance().format(date) + "\n");
            fehas.append(DateFormat.getDateInstance(DateFormat.SHORT).format(date) + "\n");
            fehas.append(DateFormat.getDateInstance(DateFormat.MEDIUM).format(date) + "\n");
            fehas.append(DateFormat.getDateInstance(DateFormat.LONG).format(date) + "\n");
            fehas.append(DateFormat.getDateInstance(DateFormat.FULL).format(date) + "\n");
            fehas.append(DateFormat.getDateTimeInstance().format(date) + "\n");
            fehas.append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date) + "\n");
            fehas.append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(date) + "\n");
            fehas.append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(date) + "\n");
            fehas.append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.FULL).format(date) + "\n");
            fehas.append(DateFormat.getTimeInstance().format(date) + "\n");
            fehas.append(DateFormat.getTimeInstance(DateFormat.SHORT).format(date) + "\n");
            fehas.append(DateFormat.getTimeInstance(DateFormat.MEDIUM).format(date) + "\n");
            fehas.append(DateFormat.getTimeInstance(DateFormat.LONG).format(date) + "\n");
            fehas.append(DateFormat.getTimeInstance(DateFormat.FULL).format(date) + "\n");

            new AlertDialog.Builder(this)
                    .setTitle("Fechas")
                    .setMessage(fehas)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}