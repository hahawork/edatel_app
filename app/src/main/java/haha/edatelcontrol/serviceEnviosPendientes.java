package haha.edatelcontrol;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;

import haha.edatelcontrol.clases.classExceptionHandler;
import haha.edatelcontrol.clases.classMetodosGenerales;
import haha.edatelcontrol.clases.classSQLHELPER;
import haha.edatelcontrol.clases.classVenta;

import static haha.edatelcontrol.interfaceDefineVariables.TBL_ABONOS_HECHOS;
import static haha.edatelcontrol.interfaceDefineVariables.TBL_ASISTENCIAS_MARCADAS;
import static haha.edatelcontrol.interfaceDefineVariables.TBL_VENTAS_REALIZADAS;

/**
 * Created by User on 06/02/2018.
 */

public class serviceEnviosPendientes extends Service {

    SQLiteDatabase db;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressWarnings("static-access")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {

            if (isOnline()) {
                ObtenerEnviarVentasPendienesdeEnvios();
                ObtenerEnviarVisitasPendienesdeEnvios();
                ObtenerEnviarAbonosPendienesdeEnvios();
            }

            stopSelf();

        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(this).uncaughtException(e);
        }

        return START_NOT_STICKY;
    }

    private void ObtenerEnviarVentasPendienesdeEnvios() {
        try {

            db = new classSQLHELPER(this).getWritableDatabase();
            Cursor cVentas = db.rawQuery("select * from " + TBL_VENTAS_REALIZADAS + " where estadoEnviado = 0 and IdEnviado = 0 ", null);

            if (cVentas.getCount() > 0) {

                //para mostrar notificacion de que se enviara automaticamente
                pushNotificacion(cVentas.getCount(), "REPORTE(S) DE VENTA(S)", 1001);

                for (cVentas.moveToFirst(); !cVentas.isAfterLast(); cVentas.moveToNext()) {

                    Long cantidad = cVentas.getLong(cVentas.getColumnIndex("Cantidad"));
                    Long TotalCobrar = cVentas.getLong(cVentas.getColumnIndex("TotalCobrarse"));

                    classVenta venta = new classVenta(
                            cVentas.getInt(cVentas.getColumnIndex("idVenta")),
                            cVentas.getInt(cVentas.getColumnIndex("codUsuario")),
                            cVentas.getInt(cVentas.getColumnIndex("Pdv")),
                            cVentas.getString(cVentas.getColumnIndex("numeroPOS")),
                            String.valueOf(cantidad),
                            String.valueOf(TotalCobrar > 0 ? TotalCobrar : (cantidad / 1.04)),
                            cVentas.getString(cVentas.getColumnIndex("Fecha")),
                            cVentas.getString(cVentas.getColumnIndex("tipoVentaCntdCrdt"))
                    );

                    new classMetodosGenerales(this).EnviarVentaRealizadaaDBOnline(
                            false,
                            venta.getIdVenta(),
                            String.valueOf(venta.getCodUsuario()),
                            String.valueOf(venta.getPdv()),
                            String.valueOf(venta.getNumeroPOS()),
                            String.valueOf(venta.getCantidad()),
                            String.valueOf(venta.getFecha()),
                            String.valueOf(venta.getTipoVentaCntdCrdt()),
                            String.valueOf(venta.getTotalCobrarse()));

                }

                stopSelf();
            } else {
                stopSelf();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(this).uncaughtException(e);
        }
    }

    private void ObtenerEnviarVisitasPendienesdeEnvios() {
        try {

            db = new classSQLHELPER(this).getWritableDatabase();
            Cursor cVisitas = db.rawQuery("select * from " + TBL_ASISTENCIAS_MARCADAS + " where estado = 0 and IdEnviado = 0 ", null);

            if (cVisitas.getCount() > 0) {

                ////para mostrar notificacion de que se enviara automaticamente
                pushNotificacion(cVisitas.getCount(), "REPORTE(S) DE VISITA(S)", 1000);

                for (cVisitas.moveToFirst(); !cVisitas.isAfterLast(); cVisitas.moveToNext()) {


                    new classMetodosGenerales(this).CrearNuevaAsistenciaOnline(
                            false,
                            cVisitas.getInt(cVisitas.getColumnIndex("IdAsistencia")),
                            cVisitas.getString(cVisitas.getColumnIndex("codUsuario")),
                            cVisitas.getString(cVisitas.getColumnIndex("Pdv")),
                            cVisitas.getString(cVisitas.getColumnIndex("Comentario")),
                            cVisitas.getString(cVisitas.getColumnIndex("Fecha")));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(this).uncaughtException(e);
        }
    }

    private void ObtenerEnviarAbonosPendienesdeEnvios() {
        try {

            db = new classSQLHELPER(this).getWritableDatabase();
            String consulta = "select a.idAbono, b.IdEnviado, a.idCliente, a.cantidad_abono, a.cantidad_saldo, a.fecha_abono  from " + TBL_ABONOS_HECHOS + " as a inner join " + TBL_VENTAS_REALIZADAS + " as b on a.idVentaLocal = b.idVenta where a.estadoEnviado = 0";
            Cursor cAbono = db.rawQuery(consulta, null);

            if (cAbono.getCount() > 0) {

                ////para mostrar notificacion de que se enviara automaticamente
                pushNotificacion(cAbono.getCount(), "REPORTE(S) DE ABONO(S)", 1002);

                for (cAbono.moveToFirst(); !cAbono.isAfterLast(); cAbono.moveToNext()) {

                    int idventaonline = cAbono.getInt(cAbono.getColumnIndex("IdEnviado"));

                    if (idventaonline > 0) {

                        new classMetodosGenerales(this).EnviarAbonosRealizadosOnline(
                                false,
                                cAbono.getLong(cAbono.getColumnIndex("idAbono")),
                                cAbono.getString(cAbono.getColumnIndex("IdEnviado")), //esto es de la tabla ventas
                                cAbono.getString(cAbono.getColumnIndex("idCliente")),
                                cAbono.getString(cAbono.getColumnIndex("cantidad_abono")),
                                cAbono.getString(cAbono.getColumnIndex("cantidad_saldo")),
                                cAbono.getString(cAbono.getColumnIndex("fecha_abono")));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(this).uncaughtException(e);
        }
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void pushNotificacion(int cantidad, String tipoenvio, int idnotificacion) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = getBigTextStyle(new Notification.Builder(this), cantidad, tipoenvio);
        notificationManager.notify(idnotificacion, notification);

    }

    private Notification getBigTextStyle(Notification.Builder builder, int cantidad, String tipoenvio) {

        Intent intent = new Intent(this, frmHome.class)
                .putExtra("NotificationMessage", 12).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, 15, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Sonido por defecto de notificaciones, podemos usar otro
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String setContentTitle = "Se detectó conexión a Internet",
                setContentText = "y tenias " + cantidad + " " + tipoenvio + " pendientes de envio. Se enviará automaticamente, no requiere ninguna acción por parte del usuario.";
        builder
                .setContentTitle(setContentTitle)
                .setContentText(setContentText)
                .setContentInfo("Control")
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(defaultSound)
                .setLights(Color.RED, 1, 0)
                .setAutoCancel(true);
        // .addAction(android.R.drawable.ic_menu_view, "Abrir App.", pIntent);

        return new Notification.BigTextStyle(builder)
                .setBigContentTitle(setContentTitle)
                .bigText(setContentText)
                .setSummaryText("EDATEL")
                .build();
    }
}
