package haha.edatelcontrol.clases;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;

public class classExceptionHandler  {
    private final Context myContext;
    private final String LINE_SEPARATOR = "\n";

    public classExceptionHandler(Context con) {
        myContext = con;
    }

    public void uncaughtException(final Exception e) {

        class uncaughtException extends AsyncTask<String,Void,Void>{

            @Override
            protected Void doInBackground(String... strings) {


                /*StringWriter stackTrace = new StringWriter();
                e.printStackTrace(new PrintWriter(stackTrace));
                StringBuilder errorReport = new StringBuilder();


                File root = android.os.Environment.getExternalStorageDirectory();
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(
                        new Date());

                File dir = new File(root.getAbsolutePath() + "/edatel/log");
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File file = new File(dir, "log.txt");
                if (!file.exists()){
                    errorReport.append("\n_____________________________________________\n");
                    errorReport.append("\n************ INFORMACION DEL DISPOSITIVO ***********\n");
                    errorReport.append("Marca: ");
                    errorReport.append(Build.BRAND);
                    errorReport.append(" \t Disp: ");
                    errorReport.append(Build.DEVICE);
                    errorReport.append(" \t Modelo: ");
                    errorReport.append(Build.MODEL);
                    errorReport.append(" \t Version: ");
                    errorReport.append(Build.VERSION.SDK_INT + " = " + Build.VERSION.RELEASE);
                    errorReport.append(LINE_SEPARATOR);
                }

                errorReport.append("\n_________________________________________________\n" +
                        "************ ERROR DETALLES ************\n");
                errorReport.append(stackTrace);
                //errorReport.append(((Activity)myContext).getClass().getSimpleName() + " .:. "+e.toString());


                try {
                    BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
                    buf.append(currentDateTimeString + ": " + errorReport.toString()+
                            "\n_________________________________________________________\n");
                    buf.newLine();
                    buf.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
*/
                return null;
            }
        }

        new uncaughtException().execute();
    }

}