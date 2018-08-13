package haha.edatelcontrol;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import haha.edatelcontrol.clases.ListViewClienteAdapter;
import haha.edatelcontrol.clases.classClientesPDV;
import haha.edatelcontrol.clases.classExceptionHandler;
import haha.edatelcontrol.clases.classMetodosGenerales;
import haha.edatelcontrol.clases.classSQLHELPER;
import haha.edatelcontrol.clases.classVenta;

import static haha.edatelcontrol.interfaceDefineVariables.TBL_PUNTOSDEVENTA;
import static haha.edatelcontrol.interfaceDefineVariables.TBL_VENTAS_REALIZADAS;

public class frmVender extends AppCompatActivity implements interfaceDefineVariables {

    final static int VENDER_RECARGA = 1, CONSULTAR_SALDO = 2, CONSULTAR_CLIENTE = 3, CAMBIAR_PIN = 5;
    private ListView listView;
    private ArrayList<classClientesPDV> stringArrayList;
    private ListViewClienteAdapter adapter;
    EditText etNumero;
    FloatingActionButton fab;
    int IdPDRseleccionado = 0;
    String miPINventa = "", NumeroSeleccionado = "", NombreSeleccionado;

    SQLiteDatabase db;
    SharedPreferences setting;
    classVenta venta;
    classMetodosGenerales cmg;
    static Context mContext;
    static StringBuilder EventosRespondidosPorServidor = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vender);
        try {

            setToolBar();
            setupWindowAnimations();
            iniciarInstancias();
            eventosControles();


            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String texto = bundle.getString("message");
                if (texto != null) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(frmVender.this);
                    builder.setCancelable(false);
                    builder.setTitle("Guardar");
                    builder.setMessage(texto);
                    builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });

                    Dialog dialog = builder.create();
                    dialog.show();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
        }
    }

    private void setToolBar() {
        try {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
        }
    }

    private void iniciarInstancias() {
        try {
            cmg = new classMetodosGenerales(this);
            fab = (FloatingActionButton) findViewById(R.id.fab);
            etNumero = (EditText) findViewById(R.id.etNumeroTelefono_V);
            listView = (ListView) findViewById(R.id.list_clientes);
            db = new classSQLHELPER(this).getWritableDatabase();
            setting = PreferenceManager.getDefaultSharedPreferences(this);
            mContext = this;
            setData();

            adapter = new ListViewClienteAdapter(this, R.layout.item_listview, stringArrayList);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
        }
    }

    private void eventosControles() {

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpcionesDeComandos();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    TextView tvId = (TextView) view.findViewById(R.id.id);
                    TextView tvNumero = (TextView) view.findViewById(R.id.number);
                    TextView tvNombre = (TextView) view.findViewById(R.id.text);


                    IdPDRseleccionado = Integer.parseInt(tvId.getText().toString());
                    etNumero.setText(tvNumero.getText().toString());
                    NombreSeleccionado = tvNombre.getText().toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupWindowAnimations() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition t3 = TransitionInflater.from(this)
                    .inflateTransition(R.transition.detail_enter_trasition);
            getWindow().setEnterTransition(t3);
            //getWindow().setExitTransition(t3);
        }
    }

    private void setData() {
        stringArrayList = new ArrayList<>();

        try {

            Cursor cPdv = db.rawQuery(String.format("SELECT idpdv, NombrePdV, NumeroPOS FROM %s ORDER BY NombrePdV", TBL_PUNTOSDEVENTA), null);

            if (cPdv.getCount() > 0) {

                for (cPdv.moveToFirst(); !cPdv.isAfterLast(); cPdv.moveToNext()) {

                    stringArrayList.add(new classClientesPDV(
                            cPdv.getInt(0),
                            cPdv.getString(1),
                            cPdv.getString(2)));

                }
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("No hay puntos de venta")
                        .setMessage("Aun no has descargado los puntos de venta, pero puedes realiza las operaciones.")
                        .setCancelable(true)
                        .setPositiveButton("Descargar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new classMetodosGenerales(frmVender.this).VerificarNuevosPuntosguardados(true, setting.getInt("codUsuario", 0), 0);
                            }
                        })
                        .setNegativeButton("Omitir", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
            //**********************************************************************

        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
        }
    }

    /**
     * Muestra el mensaje de dialgo con las opciones para ese momento.
     */
    private void OpcionesDeComandos() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(frmVender.this);
        builder.setTitle("Seleccione la operación a ejecutar");
        builder.setCancelable(true);
        final String[] opciones = new String[]{"Vender recarga", "Ver saldo disponible", "Ver saldo en PDR", "Cambiar pin", "Editar Cliente"};
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        dialogVenderRecargas();
                        break;
                    case 1:
                        dialogoVerSaldoDisponible();
                        break;
                    case 2:
                        dialogoDeOpcionesdeComandos(CONSULTAR_CLIENTE, opciones[2]);
                        break;
                    case 3:
                        dialogoDeOpcionesdeComandos(CAMBIAR_PIN, opciones[3]);
                        break;
                    case 4:
                        startActivity(new Intent(frmVender.this, frmGuardarPuntoVenta.class)
                                .putExtra("idCliente", "" + IdPDRseleccionado));
                        frmVender.this.finish();
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        //dialog.getWindow().getAttributes().windowAnimations = R.style.OpenDialog;
        dialog.show();
    }


    TextView tvCantPendPagOnline;

    private void dialogVenderRecargas() {
        try {

            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dg_vender_recargas_frmvender);

            final EditText etNumTel = (EditText) dialog.findViewById(R.id.etTelefono_dgVR);
            final EditText etPin = (EditText) dialog.findViewById(R.id.etPIN_dgVR);
            final EditText etCantidad = (EditText) dialog.findViewById(R.id.etCantidad_dgVR);
            final RadioButton rbContado = (RadioButton) dialog.findViewById(R.id.rbContado_dgVR);
            RadioButton rbCredito = (RadioButton) dialog.findViewById(R.id.rbCredito_dgVR);
            final Button btnEfectuar = (Button) dialog.findViewById(R.id.btnEfectuar_dgVR);
            TextView tvcantPendi = (TextView) dialog.findViewById(R.id.tvCantidCredtPendientPago_dgVR);
            tvCantPendPagOnline = (TextView) dialog.findViewById(R.id.tvCantidCredtPendientPagoOnline_dgVR);
            Button btnIraAbonar = (Button) dialog.findViewById(R.id.btnIraAbonar_dgVR);

            final int CantddPendientPago = cmg.CantidadDeCreditosQueTieneCliente(IdPDRseleccionado);
            if (CantddPendientPago > 0) {
                tvcantPendi.setText(String.format("Este cliente debe %d créditos.", CantddPendientPago));
                btnIraAbonar.setVisibility(View.VISIBLE);
            } else {
                tvcantPendi.setVisibility(View.GONE);
                btnIraAbonar.setVisibility(View.GONE);
            }

            miPINventa = setting.getString("stPIN_PERSONAL", "1234");
            NumeroSeleccionado = etNumero.getText().toString();
            etPin.setText(miPINventa);
            etNumTel.setText(NumeroSeleccionado);

            /**************************************************************
             * ESTO ES PARA CONSULTAR EL SALDO DISPONIBLE DE VENTAS DEL CLIENTES ANTES DE VENDERLE*/
            String pin = TextUtils.isEmpty(etPin.getText()) ? "" : etPin.getText().toString();
            final String numero = TextUtils.isEmpty(etNumTel.getText()) ? "" : etNumTel.getText().toString();

            final StringBuilder cadenaComandos = new StringBuilder();
            cadenaComandos.append("*601*");
            cadenaComandos.append(String.format("3*%s*%s", pin, numero));

            EjecutarUSSDD(cadenaComandos);
            /******************************************************************/

            //se verifica en linea si aun debe o canceló de sus ventas al credito
            cmg.VerificarClienteDebeSaldoEnLinea("" + IdPDRseleccionado);


            btnIraAbonar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String jsonCliente = String.format("{'idpdv':'%s', 'NombrePdV':'%s', 'NumeroPOS':'%s'}",
                            IdPDRseleccionado,
                            NombreSeleccionado,
                            NumeroSeleccionado);

                    startActivity(new Intent(frmVender.this, frmEstadoCuenta.class)
                            .putExtra("jsonCliente", jsonCliente));
                }
            });

            rbCredito.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {    //si es en modo credito.

                        //se verifica localmente el estado de credito
                        // si debe mas de las ventas permitidas se inhabilita la opcion de vender
                        if (CantddPendientPago >= Integer.parseInt(setting.getString("stMaximCreditoPermitido", "3"))) { //si debe 3 o mas creditos no se le puede vender mas al credito
                            btnEfectuar.setVisibility(View.GONE);
                        }
                    } else {
                        btnEfectuar.setVisibility(View.VISIBLE);
                    }
                }
            });
            etPin.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    SharedPreferences.Editor editor = setting.edit();
                    editor.putString("stPIN_PERSONAL", charSequence.toString());
                    editor.commit();
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            btnEfectuar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String pin = TextUtils.isEmpty(etPin.getText()) ? "" : etPin.getText().toString();
                    final String numero = TextUtils.isEmpty(etNumTel.getText()) ? "" : etNumTel.getText().toString();
                    final String cantidad = TextUtils.isEmpty(etCantidad.getText()) ? "" : etCantidad.getText().toString();

                    final StringBuilder cadenaComandos = new StringBuilder();
                    cadenaComandos.append("*601*");
                    cadenaComandos.append(String.format("1*%s*%s*%s*1", pin, numero, cantidad));

                    //muestra un alert con la confirmacion de la venta
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Confirmar envio de C$ " + cantidad + ".00");
                    builder.setCancelable(false);
                    builder.setMessage(cadenaComandos.toString() + "#");
                    builder.setPositiveButton("Proceder", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            EjecutarUSSDD(cadenaComandos);

                            venta = new classVenta(
                                    0,
                                    setting.getInt("codUsuario", 0),
                                    IdPDRseleccionado,
                                    numero,
                                    cantidad,
                                    "" + (Long.parseLong(cantidad) / 1.04),
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                                    rbContado.isChecked() ? "CONTADO" : "CREDITO"
                            );

                            dialog.dismiss();

                            ConfirmarVenta(venta);

                        }
                    }).setNegativeButton("Corregir", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();


                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
        }
    }

    /**
     * esto es cuando la consulta de la deuda en linea es ejecutada
     *
     * @param result es de tipo JsonObject y trae items cantventapendcancelar y totalgnralsaldo entre otros
     */
    @Override
    public void onTaskComplete(final JSONObject result) {

        try {
            if (result != null) {
                tvCantPendPagOnline.setText("Cant. ventas: " + result.getString("cantventapendcancelar") + "\n" +
                        "Total deuda: C$ " + result.getString("totalgnralsaldo"));

                tvCantPendPagOnline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        try {

                            JSONArray jsonArray = result.getJSONArray("deudas");

                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("\tFecha venta\t\tSaldo\n");

                            for (int i = 0; i < jsonArray.length(); i++) {        // PARA CADA PUNTO DE VENTA REGISTRADO

                                try {
                                    JSONObject c = jsonArray.getJSONObject(i);
                                    stringBuilder.append(c.getString("fecha") + "\tC$ " + c.getString("saldo") + "\n");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            new AlertDialog.Builder(mContext)
                                    .setTitle("Ventas Pendientes")
                                    .setMessage(stringBuilder)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            } else {
                tvCantPendPagOnline.setText("No se ha podido conectar.");
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void dialogoVerSaldoDisponible() {
        try {

            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dg_vender_recargas_frmvender);

            final EditText etPin = (EditText) dialog.findViewById(R.id.etPIN_dgVR);
            Button btnEfectuar = (Button) dialog.findViewById(R.id.btnEfectuar_dgVR);

            ((TextInputLayout) dialog.findViewById(R.id.tilcANTIDAD_dgVR)).setVisibility(View.GONE);
            ((TextInputLayout) dialog.findViewById(R.id.tilNumeroCelular_dgVR)).setVisibility(View.GONE);
            ((RadioGroup) dialog.findViewById(R.id.rgTipoVenta_dgVR)).setVisibility(View.GONE);

            etPin.setText(setting.getString("stPIN_PERSONAL", "1234"));

            btnEfectuar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String pin = TextUtils.isEmpty(etPin.getText()) ? "" : etPin.getText().toString();

                    final StringBuilder cadenaComandos = new StringBuilder();
                    cadenaComandos.append("*601*");
                    cadenaComandos.append(String.format("2*%s", pin));

                    EjecutarUSSDD(cadenaComandos);

                    dialog.dismiss();
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
        }
    }

    public void dialogoDeOpcionesdeComandos(final int posicion, String opcion) {

        final Dialog dialog = new Dialog(this);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        LinearLayout layout = new LinearLayout(this);
        layout.setBackgroundResource(R.drawable.border);
        layout.setPadding(0, 0, 0, 10);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                /*width*/ ViewGroup.LayoutParams.MATCH_PARENT,
                /*height*/ ViewGroup.LayoutParams.WRAP_CONTENT,
                /*weight*/ 1.0f
        );
        layout.setLayoutParams(param);
        layout.requestLayout();//It is necesary to refresh the screen


        final TextView tvtitle = new TextView(this);
        tvtitle.setText("Para " + opcion + " Favor rellene todos los campos.");
        tvtitle.setTextColor(Color.BLACK);
        tvtitle.setBackgroundColor(Color.parseColor("#F57C00"));
        tvtitle.setPadding(5, 5, 5, 5);

        layout.addView(tvtitle);

        final EditText etPin = new EditText(this);
        etPin.setHint("PIN");
        etPin.setText(setting.getString("stPIN_PERSONAL", "1234"));
        etPin.setInputType(InputType.TYPE_CLASS_NUMBER);

        final EditText etCampo2 = new EditText(this);
        etCampo2.setInputType(InputType.TYPE_CLASS_NUMBER);

        final EditText etCampo3 = new EditText(this);
        etCampo3.setInputType(InputType.TYPE_CLASS_NUMBER);

        if (posicion == CONSULTAR_CLIENTE) {
            layout.addView(etPin);
            etCampo2.setHint("Telefono del Cliente");
            etCampo2.setText(TextUtils.isEmpty(etNumero.getText()) ? "" : etNumero.getText().toString());
            layout.addView(etCampo2);
        }
        if (posicion == CAMBIAR_PIN) {
            layout.addView(etPin);
            etCampo2.setHint("Nuevo PIN");
            layout.addView(etCampo2);
            etCampo3.setHint("Confirmar PIN");
            layout.addView(etCampo3);
        }


        final Button btnConfirmar = new Button(this);
        btnConfirmar.setText("Efectuar");
        btnConfirmar.setBackgroundResource(R.color.colorAccent);
        btnConfirmar.setPadding(15, 15, 15, 15);
        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String pin = TextUtils.isEmpty(etPin.getText()) ? "" : etPin.getText().toString();
                String camp2 = TextUtils.isEmpty(etCampo2.getText()) ? "" : etCampo2.getText().toString();
                String camp3 = TextUtils.isEmpty(etCampo3.getText()) ? "" : etCampo3.getText().toString();

                final StringBuilder cadenaComandos = new StringBuilder();
                cadenaComandos.append("*601*");

                if (posicion == CONSULTAR_CLIENTE) {
                    cadenaComandos.append(String.format("3*%s*%s", pin, camp2));
                }
                if (posicion == CAMBIAR_PIN) {
                    cadenaComandos.append(String.format("5*%s*%s*%s", pin, camp2, camp3));
                }


                EjecutarUSSDD(cadenaComandos);

                dialog.dismiss();
            }
        });

        layout.addView(btnConfirmar);
        dialog.setContentView(layout);

        dialog.show();
    }

    private void ConfirmarVenta(final classVenta venta) {

        try {

            AlertDialog.Builder builder = new AlertDialog.Builder(frmVender.this);
            builder.setCancelable(false);
            builder.setTitle("Guardar");
            builder.setMessage("¿Se ha realizado la transferencia con éxito?");
            builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    // si confirma que la venta fue exitosa se vuelve a consultar el nuevo saldo del cliente
                    /**************************************************************
                     * ESTO ES PARA CONSULTAR EL SALDO DISPONIBLE DE VENTAS DEL CLIENTES DESPUES DE VENDERLE*/
                    final StringBuilder cadenaComandos = new StringBuilder();
                    cadenaComandos.append(String.format("*601*3*%s*%s", miPINventa, NumeroSeleccionado));
                    EjecutarUSSDD(cadenaComandos);
                    /******************************************************************/

                    GuardarRecargaVendida(venta);
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            Dialog dialog = builder.create();
            dialog.show();


        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
        }
    }

    private void EjecutarUSSDD(StringBuilder cadenaComandos) {
        try {

            //cmg.Toast(this, cadenaComandos.toString() + "#", R.drawable.ic_info);
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + cadenaComandos.toString() + Uri.encode("#")));
            if (ActivityCompat.checkSelfPermission(frmVender.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            }
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
        }
    }

    public static void launchRepuestaDeLaPeticion(String message) {

        try {
        /*
        Intent intent = new Intent(mContext, frmVender.class);
        intent.putExtra("message", message);
        mContext.startActivity(intent);*/

            String venta = "EXITOSO, Recarga de C$ 520 para el 86752483, TXN numero CT180312.1643.350005. Su nuevo saldo es C$ 13440";
            String consulta = "EXITOSO, el saldo del 86752483 es C$ 351";
           /* String[] partes = message.split(" ");
            StringBuilder mensaje = new StringBuilder();
            for (int i = 0; i < partes.length; i++) {
                mensaje.append(i + " - " + partes[i] + "\n");
            }*/
            EventosRespondidosPorServidor.append("►\t" + message + "\n\n");

            final Dialog dialog = new Dialog(mContext);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_layout_repuesta_servidor);
            dialog.setCanceledOnTouchOutside(false);

            final TextView tvTexto = (TextView) dialog.findViewById(R.id.tvMensaje_CLRS);
            tvTexto.setText(EventosRespondidosPorServidor);

            ((Button) dialog.findViewById(R.id.btnLimpiar_CLRS)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //EventosRespondidosPorServidor.setLength(0);
                    EventosRespondidosPorServidor.delete(0, EventosRespondidosPorServidor.length());
                }
            });

            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
        }
    }

    private void GuardarRecargaVendida(classVenta venta) {

        try {
            ContentValues values = new ContentValues();
            values.put("idVenta", (byte[]) null);
            values.put("codUsuario", venta.getCodUsuario());
            values.put("Pdv", venta.getPdv());
            values.put("numeroPOS", venta.getNumeroPOS());
            values.put("Cantidad", venta.getCantidad());
            values.put("TotalCobrarse", venta.getTotalCobrarse());
            values.put("Fecha", venta.getFecha());
            values.put("tipoVentaCntdCrdt", venta.getTipoVentaCntdCrdt());
            values.put("estadoEnviado", 0);
            values.put("IdEnviado", 0);

            int idInsert = (int) db.insert(TBL_VENTAS_REALIZADAS, null, values);

            if (idInsert > 0) {

                cmg.Toast(this, (String.format("Se ha guardado la venta por C$%s.00 al número %s con éxito", venta.getCantidad(), venta.getNumeroPOS())), R.drawable.ic_success);

                if (cmg.TieneConexion(this)) {  // SI TIENE CONEXION A INTERNET

                    cmg.EnviarVentaRealizadaaDBOnline(
                            true,
                            idInsert,
                            String.valueOf(venta.getCodUsuario()),
                            String.valueOf(venta.getPdv()),
                            String.valueOf(venta.getNumeroPOS()),
                            String.valueOf(venta.getCantidad()),
                            String.valueOf(venta.getFecha()),
                            String.valueOf(venta.getTipoVentaCntdCrdt()),
                            String.valueOf(venta.getTotalCobrarse()));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_vender, menu);

        try {
            MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
            final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (TextUtils.isEmpty(newText)) {
                        adapter.filter("");
                        listView.clearTextFilter();
                    } else {
                        adapter.filter(newText);
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
        }
        return true;
    }


    @Override
    protected void onPause() {
        cmg.SolicitudDesdeLaAplicacion = true;
        Log.d("Metodos", "Llamado a onPause()");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        cmg.SolicitudDesdeLaAplicacion = false;
        Log.d("Metodos", "Llamado a onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        cmg.SolicitudDesdeLaAplicacion = true;
        Log.d("Metodos", "Llamado a onResume()");
        super.onResume();
    }

}