package haha.edatelcontrol;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

import haha.edatelcontrol.clases.classClientesPDV;
import haha.edatelcontrol.clases.classMetodosGenerales;
import haha.edatelcontrol.clases.classSQLHELPER;

import static haha.edatelcontrol.interfaceDefineVariables.TBL_ABONOS_HECHOS;
import static haha.edatelcontrol.interfaceDefineVariables.TBL_VENTAS_REALIZADAS;


public class Fragmentestadocuentadetalles extends Fragment {

    DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
    DecimalFormat formateador;
    int idcliente;
    String NombrePdv, NumeroPOS;
    SQLiteDatabase db;
    String jsonCliente;
    Double totGnralVendido = 0d, totalGnralAbonado = 0d, totGnralSaldo = 0d;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_estadocuentadetalle, container, false);
        try {

            simbolos.setDecimalSeparator('.');
            simbolos.setGroupingSeparator(',');
            formateador = new DecimalFormat("#,##0.00", simbolos);

            db = new classSQLHELPER(getContext()).getWritableDatabase();

            Bundle bundle = getArguments();
            jsonCliente = bundle.getString("jsonCliente");

            if (jsonCliente != null) {
                JSONObject jsonObject = new JSONObject(jsonCliente);

                idcliente = jsonObject.getInt("idpdv");
                NombrePdv = jsonObject.getString("NombrePdV");
                NumeroPOS = jsonObject.getString("NumeroPOS");

                TextView texto = (TextView) view.findViewById(R.id.texto);
                texto.setText(
                        String.format("%s\n%s", NombrePdv, NumeroPOS)
                );

                GridViewAdapterDetallesCreditos(view);

                // el total general se calcula en el gridview por eso se pone debajo de lallamada de  la funcion
                ((TextView) view.findViewById(R.id.tvTotalVendido_fECD)).setText("C$" + formateador.format(totGnralVendido));
                ((TextView) view.findViewById(R.id.tvTotalAbonado_fECD)).setText("C$" + formateador.format(totalGnralAbonado));
                ((TextView) view.findViewById(R.id.tvTotalSaldo_fECD)).setText("C$" + formateador.format(totGnralSaldo));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }


    public void GridViewAdapterDetallesCreditos(View v) {
        try {
            final LinearLayout layout = (LinearLayout) v.findViewById(R.id.llListaVentasCreditoPendAbono_fecd);

            //obtiene todas las ventas en modo credito
            Cursor cEstado = db.rawQuery("SELECT * FROM " + TBL_VENTAS_REALIZADAS + " WHERE Pdv = " + idcliente + " AND tipoVentaCntdCrdt = 'CREDITO'", null);

            if (cEstado.getCount() > 0) {

                for (cEstado.moveToFirst(); !cEstado.isAfterLast(); cEstado.moveToNext()) {

                    final int idVenta = cEstado.getInt(cEstado.getColumnIndex("idVenta"));
                    final int idVentaOnline = cEstado.getInt(cEstado.getColumnIndex("IdEnviado"));
                    final Long totalabonado = getTotalAbonado(cEstado.getInt(cEstado.getColumnIndex("idVenta")));
                    final Double TotalVendido = cEstado.getDouble(cEstado.getColumnIndex("Cantidad"));
                    final Double totalCobrar = cEstado.getLong(cEstado.getColumnIndex("TotalCobrarse")) > 0
                            ? cEstado.getLong(cEstado.getColumnIndex("TotalCobrarse"))
                            : cEstado.getLong(cEstado.getColumnIndex("TotalCobrarse")) / 1.04;

                    if (totalabonado < totalCobrar) {

                        View view = getLayoutInflater().inflate(R.layout.list_ventaspendientesabonar, null);
                        TextView tvFechaventa = (TextView) view.findViewById(R.id.tvFechaVenta_lvpa);
                        TextView tvtotalvendido = (TextView) view.findViewById(R.id.tvTotVendido_lvpa);
                        TextView tvtotalcobrar = (TextView) view.findViewById(R.id.tvTotCobrar_lvpa);
                        TextView tvtotalabonado = (TextView) view.findViewById(R.id.tvTotAbonado_lvpa);
                        TextView tvsaldo = (TextView) view.findViewById(R.id.tvSaldo_lvpa);
                        Button btnAbonar = (Button) view.findViewById(R.id.btnAbonarVenta_lvpa);


                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // para formatear el tipo de formato de fecha
                        try {
                            tvFechaventa.setText(cEstado.getString(cEstado.getColumnIndex("Fecha")));
                            tvtotalvendido.setText("C$" + formateador.format(TotalVendido));
                            tvtotalcobrar.setText("C$"+formateador.format(totalCobrar));
                            tvtotalabonado.setText("C$" + formateador.format(totalabonado));
                            tvsaldo.setText("C$" + formateador.format(totalCobrar - totalabonado));

                            totGnralVendido += totalCobrar;
                            totalGnralAbonado += totalabonado;
                            totGnralSaldo += (totalCobrar - totalabonado);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        btnAbonar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Toast.makeText(getContext(), String.valueOf(idVenta), Toast.LENGTH_LONG).show();
                                dialogoAbonar(idVenta, idVentaOnline, (totalCobrar - totalabonado));
                            }
                        });

                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Toast.makeText(getContext(), String.valueOf(idVenta), Toast.LENGTH_LONG).show();
                                new AlertDialog.Builder(getContext())
                                        .setTitle("Listado de Abonos.")
                                        .setMessage(getListaAbonos(idVenta))
                                        .show();
                            }
                        });
                        layout.addView(view);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getListaAbonos(int idVenta) {
        StringBuilder abono = new StringBuilder();
        try {

            abono.append("Fecha /\tHora\t\t\t\t\t\t\tCant. Abono\n");

            Cursor cAbonos = db.rawQuery("select * from " + TBL_ABONOS_HECHOS + " where idVentaLocal = " + idVenta, null);
            for (cAbonos.moveToFirst(); !cAbonos.isAfterLast(); cAbonos.moveToNext()) {
                abono.append(cAbonos.getString(cAbonos.getColumnIndex("fecha_abono")) + " \tC$" + cAbonos.getString(cAbonos.getColumnIndex("cantidad_abono")) + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return abono.toString();
        }

        return abono.toString();
    }

    public void dialogoAbonar(final int idventa, final int idventaonline, final Double saldoactual) {

        final Dialog dialog = new Dialog(getContext());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        LinearLayout layout = new LinearLayout(getContext());
        layout.setBackgroundResource(R.drawable.border);
        //layout.setPadding(0, 0, 0, 10);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                       /*width*/ ViewGroup.LayoutParams.MATCH_PARENT,
               /*height*/ ViewGroup.LayoutParams.WRAP_CONTENT,
               /*weight*/ 1.0f
        );
        layout.setLayoutParams(param);
        layout.setPadding(20, 20, 20, 20);
        layout.requestLayout();//It is necesary to refresh the screen


        final TextView tvtitle = new TextView(getContext());
        tvtitle.setText("Ingrese la cantidad a abonar.");
        tvtitle.setTextColor(Color.BLACK);
        tvtitle.setBackgroundColor(Color.parseColor("#F57C00"));
        tvtitle.setPadding(5, 5, 5, 5);
        layout.addView(tvtitle);

        final EditText etCantidadAbonar = new EditText(getContext());
        etCantidadAbonar.setHint("C$" + saldoactual);
        etCantidadAbonar.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(etCantidadAbonar);

       /* CheckBox chkPorcentaje = new CheckBox(getContext());
        chkPorcentaje.setText("Restar 4%");
        chkPorcentaje.setChecked(true);
        layout.addView(chkPorcentaje);*/

        final Button btnConfirmar = new Button(getContext());
        btnConfirmar.setText("Abonar");
        btnConfirmar.setBackgroundResource(R.color.colorAccent);
        btnConfirmar.setPadding(15, 15, 15, 15);
        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    String Cantidd = TextUtils.isEmpty(etCantidadAbonar.getText()) ? "" : etCantidadAbonar.getText().toString();

                    if (Cantidd.length() > 0 && Integer.parseInt(Cantidd) > 0 && Integer.parseInt(Cantidd) <= saldoactual) { //si el tamaño de la cadena es mayor que cero y el valor > 0 y cantidad es menor o igual al saldo actual
                        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        ContentValues values = new ContentValues();
                        values.put("idAbono", (byte[]) null);
                        values.put("idVentaLocal", idventa);
                        values.put("idVentaOnline", idventaonline);
                        values.put("idCliente", idcliente);
                        values.put("cantidad_abono", Cantidd);
                        values.put("cantidad_saldo", saldoactual - Integer.parseInt(Cantidd));
                        values.put("fecha_abono", fecha);
                        values.put("estadoEnviado", "0");
                        values.put("IdEnviado", "0");

                        Long idinsert = db.insert(TBL_ABONOS_HECHOS, null, values);
                        if (idinsert > 0) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Guardado con éxito");
                            builder.setMessage(String.format("Se ha registrado un abono por C$%s quedando un saldo de C$%s",
                                    Cantidd,
                                    (saldoactual - Integer.parseInt(Cantidd))
                            ));
                            builder.setCancelable(false);
                            builder.setPositiveButton("Refrescar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //vuelve a crear el fragment
                                    Fragment fragment = new Fragmentestadocuentadetalles();

                                    Bundle args = new Bundle();
                                    args.putString("jsonCliente", jsonCliente);
                                    fragment.setArguments(args);
                                    android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                                    transaction.replace(R.id.fragment_container, fragment);
                                    //transaction.addToBackStack(null);
                                    fragmentManager.popBackStack();
                                    transaction.commit();
                                }
                            });
                            builder.setNegativeButton("Regresar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //regresa a la lista de clientes
                                    Fragment fragment = new Fragmentestadocuentaclientes();
                                    android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                                    transaction.replace(R.id.fragment_container, fragment);
                                    //transaction.addToBackStack(null);
                                    fragmentManager.popBackStack(null, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                    transaction.commit();
                                }
                            });

                            Dialog dialog1 = builder.create();
                            dialog1.show();

                            if (new classMetodosGenerales(getContext()).TieneConexion(getContext())) { //si tiene conexion a interrnet
                                if (idventaonline > 0) {    // si el idventa es mayor  que cero, la venta ya ha sido enviada.
                                    new classMetodosGenerales(getContext()).EnviarAbonosRealizadosOnline(
                                            true,
                                            idinsert,
                                            String.valueOf(idventaonline),
                                            String.valueOf(idcliente),
                                            Cantidd,
                                            "" + (saldoactual - Integer.parseInt(Cantidd)),
                                            fecha);
                                }
                            }
                        }
                    } else {
                        new classMetodosGenerales(getContext()).Toast(getActivity(), "No se puede realizar el abono, revise la cantidad ingresada", R.drawable.ic_error);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        layout.addView(btnConfirmar);

        dialog.setContentView(layout);

        dialog.show();
    }

    private Long getTotalAbonado(int idVenta) {

        try {

            //cursor para obtenerel total abonado
            Cursor cTotalabono = db.rawQuery("select SUM(cantidad_abono) AS cantidad_abono from " + TBL_ABONOS_HECHOS + " WHERE idVentaLocal =" + idVenta, null);
            cTotalabono.moveToFirst();
            return cTotalabono.getLong(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0l;
    }
}