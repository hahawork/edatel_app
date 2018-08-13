package haha.edatelcontrol;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import haha.edatelcontrol.clases.classMetodosGenerales;
import haha.edatelcontrol.clases.classSQLHELPER;

import static haha.edatelcontrol.interfaceDefineVariables.TBL_ASISTENCIAS_MARCADAS;
import static haha.edatelcontrol.interfaceDefineVariables.TBL_PUNTOSDEVENTA;
import static haha.edatelcontrol.interfaceDefineVariables.TBL_VENTAS_REALIZADAS;

public class frmReportes extends AppCompatActivity {

    private final int HOY = 0, AYER = 1, ESTASEMANA = 2, ULTIM7DIAS = 3, ULTIMMES = 4, MESANTERIOR = 5, MESACTUAL = 6, PERSONALIZADO = 7;
    Calendar calFechaInicio, calFechaFin;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
    DecimalFormat formateador;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frmreportes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new classSQLHELPER(this).getWritableDatabase();

        simbolos.setDecimalSeparator('.');
        simbolos.setGroupingSeparator(',');
        formateador = new DecimalFormat("#,##0.00", simbolos);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(frmReportes.this,RecuperacionClientes.class));
                //new classMetodosGenerales(frmReportes.this).clearData();
            }
        });


        //GridViewAdapterCreditos();
    }

    private void LimpiarCreditosClientes() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        try {
            getMenuInflater().inflate(R.menu.menu_frm_reportes, menu);

            MenuItem item = menu.findItem(R.id.spinner);
            Spinner spinner = (Spinner) item.getActionView();

            String[] lista_fechas = new String[]{"Hoy", "Ayer", "Esta Semana", "Últimos 7 días", "Último Mes", "Mes Anterior", "Mes Actual", "Mi rango de fechas"};
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                    R.layout.custom_spinner_item, lista_fechas);
            dataAdapter.setDropDownViewResource(R.layout.custom_spinner_item);
            spinner.setAdapter(dataAdapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    getFechasSegunRangoSeleccionado(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void getFechasSegunRangoSeleccionado(int posicion) {
        try {

            String fecha = "";
            switch (posicion) {
                case HOY: // hoy
                    fecha = dateFormat.format(new Date());
                    break;
                case AYER: //ayer
                    fecha = getFechaDiferenciaDeDias(-1);   // -1 dia es igual a ayer
                    break;
                case ESTASEMANA:
                    fecha = getFechaEstaSemana();
                    break;

                case ULTIM7DIAS:
                    fecha = getFechaDiferenciaDeDias(-7);
                    break;
                case ULTIMMES:
                    fecha = getFechaDiferenciaDeMeses(-1);
                    break;
                case MESANTERIOR:
                    fecha = getFechaUltimoMes();
                    break;
                case MESACTUAL:
                    break;

                case PERSONALIZADO:
                    break;
            }

            new AlertDialog.Builder(this)
            .setTitle("Fecha")
            .setMessage(fecha)
            .show();

            TextView tvVentasRealizadas = (TextView) findViewById(R.id.tvTotalVentasRealizadas_R);
            TextView tvTotalVendido = (TextView) findViewById(R.id.tvTotalVendido_R);
            TextView tvVentasVisitadas = (TextView) findViewById(R.id.tvTotalClientesVisitados_R);

            String[] ventas = getCantidadVentasRealizadas(fecha);
            tvVentasRealizadas.setText(ventas[0]);
            tvTotalVendido.setText("C$" + ventas[1]);
            tvVentasVisitadas.setText(getCantidadPuntosVisitados(fecha));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] getCantidadVentasRealizadas(String fecha) {
        try {

            Cursor cCantVR = db.rawQuery("select Cantidad from " + TBL_VENTAS_REALIZADAS + " where Fecha like '" + fecha + "%'", null);
            Float cantidadvendido = 0f;

            if (cCantVR.getCount() > 0) {
                for (cCantVR.moveToFirst(); !cCantVR.isAfterLast(); cCantVR.moveToNext()) {
                    cantidadvendido += cCantVR.getFloat(0);
                }

                return new String[]{"" + cCantVR.getCount(), formateador.format(cantidadvendido)};

            } else {

                return new String[]{"0", "0.00"};
            }

        } catch (Exception e) {

            e.printStackTrace();
            return new String[]{"0Err", "0.00Err"};
        }
    }

    private String getCantidadPuntosVisitados(String fecha) {
        try {

            Cursor cCantPV = db.rawQuery("select IdAsistencia from " + TBL_ASISTENCIAS_MARCADAS + " where Fecha like '" + fecha + "%'", null);

            if (cCantPV.getCount() > 0) {

                return "" + cCantPV.getCount();

            } else {

                return "0";
            }

        } catch (Exception e) {

            e.printStackTrace();
            return "0Err";
        }
    }


    /*public void GridViewAdapterCreditos() {
        try {
            final LinearLayout layout = (LinearLayout)findViewById(R.id.llListaClientesCreditoPend_R);

            //obtiene todas las ventas en modo credito
            Cursor cDeudas = db.rawQuery("SELECT * FROM " + TBL_VENTAS_REALIZADAS + " WHERE tipoVentaCntdCrdt = 'CREDITO'", null);

            if (cDeudas.getCount() > 0) {

                for (cDeudas.moveToFirst(); !cDeudas.isAfterLast(); cDeudas.moveToNext()) {

                    Long totalVendido = new classMetodosGenerales(this).VerificaClienteTieneSaldoPendientesDeAbonar(cDeudas.getInt(cDeudas.getColumnIndex("Pdv")));
                    if (totalVendido > 0) {


                        View view = getLayoutInflater().inflate(R.layout.list_clientes_con_creditos_pendient, null);
                        TextView tvcliente = (TextView) view.findViewById(R.id.tvNombCliente_lccp);
                        TextView tvsaldo = (TextView) view.findViewById(R.id.tvCantidadSaldo_numero_lccp);

                        try {
                            tvcliente.setText(cDeudas.getString(cDeudas.getColumnIndex("numeroPOS")));
                            tvsaldo.setText(String.format("%s - C$%s",
                                    cDeudas.getString(cDeudas.getColumnIndex("numeroPOS")),
                                    formateador.format(totalVendido)));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        layout.addView(view);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/


    private String getFechaDiferenciaDeDias(int dias) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, dias);
        return dateFormat.format(calendar.getTime());
    }

    private String getFechaDiferenciaDeMeses(int meses) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, meses);
        return dateFormat.format(calendar.getTime());
    }

    private String getFechaEstaSemana() {

        StringBuilder f = new StringBuilder();

        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        f.append(dateFormat.format(calendar.getTime()));
        f.append(" AND ");
        f.append(dateFormat.format(new Date()));

        return f.toString();
    }

    private String getFechaUltimoMes() {

        StringBuilder fecha = new StringBuilder();

        Calendar aCalendar = Calendar.getInstance();
        // add -1 month to current month
        aCalendar.add(Calendar.MONTH, -1);
        // set DATE to 1, so first date of previous month
        aCalendar.set(Calendar.DATE, 1);
        fecha.append(dateFormat.format(aCalendar.getTime()));
        fecha.append(" AND ");
        // set actual maximum date of previous month
        aCalendar.set(Calendar.DATE, aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        //read it
        fecha.append(dateFormat.format(aCalendar.getTime()));
        return fecha.toString();
    }


}
