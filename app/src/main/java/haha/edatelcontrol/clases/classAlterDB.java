package haha.edatelcontrol.clases;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by User on 31/5/2017.
 */

public class classAlterDB {

    static SQLiteDatabase db;
    Context mCtx;

    public classAlterDB(Context ctx) {
        mCtx = ctx;
        db = new classSQLHELPER(ctx).getWritableDatabase();
    }

    public boolean ExisteColumna(String Tabla, String Columna) {

        boolean Existe = false;
        try {

            Cursor cVerificaColumna = db.rawQuery("select * from " + Tabla + " where 0", null);

            String[] ColumnasExis = cVerificaColumna.getColumnNames();
            if (ColumnasExis.length > 0) {
                for (int i = 0; i < ColumnasExis.length; i++) {
                    if (ColumnasExis[i].equalsIgnoreCase(Columna)) {
                        Existe = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mCtx).uncaughtException(e);
            return false;
        }
        return Existe;
    }

    public void AgregarColumna(String Tabla, String newColumna, String TipoDato) {
        try {

            db.execSQL("ALTER TABLE " + Tabla + " ADD COLUMN " + newColumna + " " + TipoDato);


        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mCtx).uncaughtException(e);
        }
    }

    public void AgregarTabla(String queryTabla) {
        try {

            db.execSQL(queryTabla);

        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mCtx).uncaughtException(e);
        }
    }

}
