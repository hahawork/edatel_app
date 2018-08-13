package haha.edatelcontrol;

import android.widget.TextView;

import org.json.JSONObject;

/**
 * Created by User on 31/01/2018.
 */

public interface interfaceDefineVariables {

    //para los web services
    String URL_CARPETA_DE_WS = "http://www.edatel.com.ni/bdedateluser/ws_app_venta/";
    //String URL_CARPETA_DE_WS = "http://www.grupovalor.com.ni/wsphp/";
    String URL_WS_GUARDAR_NUEVO_PUNTO = URL_CARPETA_DE_WS + "ws_guardarpdv.php";
    String URL_WS_RECUPERAR_PUNTO = URL_CARPETA_DE_WS + "ws_recuperarclientes.php";
    String URL_WS_EDITAR_PUNTO = URL_CARPETA_DE_WS + "ws_editarpdv.php";
    String URL_WS_VERIFICA_PUNTOS_NUEVOS = URL_CARPETA_DE_WS + "ws_verificaNuevosPuntosGuardados.php";
    String URL_WS_OBTENER_DATOS_USUARIO = URL_CARPETA_DE_WS + "ws_getusuario.php";
    String URL_WS_GUARDAR_ASISTENCIA = URL_CARPETA_DE_WS + "ws_guardarVisitas.php";
    String URL_WS_GUARDAR_VENTAS_REALIZADAS = URL_CARPETA_DE_WS + "ws_guardarVentasRealizadas.php";
    String URL_WS_VERIFICA_NUEVA_VERSION_APLICACION = URL_CARPETA_DE_WS + "ws_verificanuevaversionaplicacion.php";
    String URL_WS_GUARDAR_ABONOS_REALIZADOS = URL_CARPETA_DE_WS + "ws_guardarAbonosRealizados.php";
    String URL_WS_VERIFICA_DEUDA_CLIENTE_ONLINE = URL_CARPETA_DE_WS + "ws_verificadeudaclienteonline.php";

    // para la base de datos local
    String DB_NAME = "EDATELDB.db";
    int DB_VERSION = 1;
    String TBL_PUNTOSDEVENTA = "tbl_puntosdeventa";
    String TBL_EDATEL_USUARIOS = "tbl_edatel_usuarios";
    String TBL_ASISTENCIAS_MARCADAS = "tbl_asistenciasmarcadas";
    String TBL_VENTAS_REALIZADAS = "tbl_ventasrealizadas";
    String TBL_ABONOS_HECHOS = "tbl_abonoshechos";

    // Define data you like to return from AysncTask
    public void onTaskComplete(JSONObject result);
}