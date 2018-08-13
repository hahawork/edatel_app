package haha.edatelcontrol.clases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static haha.edatelcontrol.interfaceDefineVariables.DB_NAME;
import static haha.edatelcontrol.interfaceDefineVariables.DB_VERSION;
import static haha.edatelcontrol.interfaceDefineVariables.TBL_ABONOS_HECHOS;
import static haha.edatelcontrol.interfaceDefineVariables.TBL_ASISTENCIAS_MARCADAS;
import static haha.edatelcontrol.interfaceDefineVariables.TBL_PUNTOSDEVENTA;
import static haha.edatelcontrol.interfaceDefineVariables.TBL_VENTAS_REALIZADAS;


public class classSQLHELPER extends SQLiteOpenHelper {

    private Context Cntx;

    public final String tablapuntosdeventa = "CREATE TABLE IF NOT EXISTS " + TBL_PUNTOSDEVENTA +
            " (idpdv INTEGER PRIMARY KEY, NombrePdV text, NombrePropietario text, NumeroPOS text ,NumeroTelefono text, CoordenadasGPS text, Ciudad text, idDepartamento integer)";

    public final String tablaasistenciasmarcadas = "CREATE TABLE IF NOT EXISTS " + TBL_ASISTENCIAS_MARCADAS +
            " (IdAsistencia INTEGER PRIMARY KEY AUTOINCREMENT, codUsuario integer, Pdv integer, Fecha datetime, Comentario text, estado integer, IdEnviado integer)";

    public final String tablaventasrealizadas = "CREATE TABLE IF NOT EXISTS " + TBL_VENTAS_REALIZADAS +
            " (idVenta INTEGER PRIMARY KEY AUTOINCREMENT, codUsuario integer, Pdv integer, numeroPOS text, Cantidad decimal (10,2), TotalCobrarse decimal(10,2), Fecha datetime, tipoVentaCntdCrdt varchar(10)," +
            " estadoEnviado integer, IdEnviado integer)";

    public final String tablaabonos = "CREATE TABLE IF NOT EXISTS " + TBL_ABONOS_HECHOS +
            " (idAbono INTEGER PRIMARY KEY AUTOINCREMENT, idVentaLocal integer, idVentaOnline integer, idCliente integer, cantidad_abono decimal(10,2), cantidad_saldo decimal(10,2), fecha_abono datetime, " +
            " estadoEnviado integer, IdEnviado integer)";


    public classSQLHELPER(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.Cntx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(tablapuntosdeventa);
        sqLiteDatabase.execSQL(tablaasistenciasmarcadas);
        sqLiteDatabase.execSQL(tablaventasrealizadas);
        sqLiteDatabase.execSQL(tablaabonos);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
