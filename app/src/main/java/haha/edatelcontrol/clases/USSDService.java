package haha.edatelcontrol.clases;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Collections;
import java.util.List;

import haha.edatelcontrol.frmVender;

/**
 * Created by User on 12/03/2018.
 */

public class USSDService extends AccessibilityService {

    public static String TAG = USSDService.class.getSimpleName();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent");

        AccessibilityNodeInfo source = event.getSource();
        //Log.d(TAG, source.toString());

    /* if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !event.getClassName().equals("android.app.AlertDialog")) { // android.app.AlertDialog is the standard but not for all phones  */
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !String.valueOf(event.getClassName()).contains("AlertDialog")) {
            return;
        }
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && (source == null || !source.getClassName().equals("android.widget.TextView"))) {
            return;
        }
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && TextUtils.isEmpty(source.getText())) {
            return;
        }

        List<CharSequence> eventText;

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            eventText = event.getText();
        } else {
            eventText = Collections.singletonList(source.getText());
        }

        String text = processUSSDText(eventText);


        if (TextUtils.isEmpty(text))
            return;

        //si la soliocituid se realiza desde la aplicacion edatel
        if (new classMetodosGenerales(this).SolicitudDesdeLaAplicacion) {
            //posibles repuestas del servidor, el ultimo es de movistar al querer hacer la consulta.
            if (text.contains("EXITOSO, el saldo del ") ||
                    text.contains("Su saldo actual es C$") ||
                    text.contains("EXITOSO, Recarga de C$") ||
                    text.contains("USSDC: Routing failed")) {
                // Close dialog
                performGlobalAction(GLOBAL_ACTION_BACK); // This works on 4.1+ only

                //Toast.makeText(this, text, Toast.LENGTH_LONG).show();
                frmVender.launchRepuestaDeLaPeticion(text);
                Log.d(TAG, text);
                // Handle USSD response here
            }
        }
    }



    private String processUSSDText(List<CharSequence> eventText) {
        for (CharSequence s : eventText) {
            String text = String.valueOf(s);
            // Return text if text is the expected ussd response
            if (true) {
                return text;
            }
        }
        return null;
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.packageNames = new String[]{"com.android.phone"};
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
    }
}