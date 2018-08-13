package haha.edatelcontrol;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import haha.edatelcontrol.clases.classClientesPDV;
import haha.edatelcontrol.clases.classExceptionHandler;
import haha.edatelcontrol.clases.classJSONParser;
import haha.edatelcontrol.clases.classMetodosGenerales;
import haha.edatelcontrol.clases.classSQLHELPER;

import static haha.edatelcontrol.interfaceDefineVariables.TBL_PUNTOSDEVENTA;
import static haha.edatelcontrol.interfaceDefineVariables.URL_WS_EDITAR_PUNTO;
import static haha.edatelcontrol.interfaceDefineVariables.URL_WS_GUARDAR_NUEVO_PUNTO;

public class frmGuardarPuntoVenta extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 10, REQUEST_CODE_CAMERA_PICTURE = 1, METODO_GUARDAR = 1, METODO_EDITAR = 2;

    // Google client to interact with Google API
    //esto es para el gps
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    PendingResult<LocationSettingsResult> result;

    //esto es para el mapa
    GoogleMap mGoogleMap;
    SupportMapFragment mapFragment = null;
    private Location mLastLocation;
    float ZoomActual = 17f;

    static Uri ImagenTomadaCamara = null;
    Spinner spnDeptos;
    String Coordenadas, imagePath = "";
    boolean IsGPS;
    EditText etIDPdV, etNombrePdv, etNombrePropietario, etNumeroPOS, etNumeroTelefono, etCoordenadas;
    AutoCompleteTextView etCiudad;
    ImageView ivFotoSoporte;
    Button btnFotoSoporte;
    CheckBox chkIncluirFoto;
    SharedPreferences setting;
    TextView tvInfoGPS;
    classMetodosGenerales cmg;
    classClientesPDV cliente;
    FloatingActionButton fab, fabEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardar_punto_venta);

        setToolbar(); // Reemplazar toolbar
        setupWindowAnimations(); // Añadir animaciones
        iniciarInstancias(); // inici todas las insancias de las clases y controles
        eventosControles(); // carga  los eventos a los controles
        getExtras();

        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
            startLocationUpdates();
        }
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void getExtras() {
        Intent sms_intent = getIntent();
        Bundle b = sms_intent.getExtras();
        if (b != null) {
            try {

                String idCliente = b.getString("idCliente");
                if (idCliente != null) {
                    ObtenerDatosParaEditarCliente(idCliente);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupWindowAnimations() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition t3 = TransitionInflater.from(this)
                    .inflateTransition(R.transition.detail_enter_trasition);
            getWindow().setEnterTransition(t3);
            //getWindow().setExitTransition(t3);
        }
    }

    private void iniciarInstancias() {

        etIDPdV = (EditText) findViewById(R.id.etIDPdV_GPV);
        etNombrePdv = (EditText) findViewById(R.id.etNombrePdV_GPV);
        etNombrePropietario = (EditText) findViewById(R.id.etNombrePropietario_GPV);
        etNumeroPOS = (EditText) findViewById(R.id.etNumeroPOS_GPV);
        etNumeroTelefono = (EditText) findViewById(R.id.etNumeroTelefono_GPV);
        etCoordenadas = (EditText) findViewById(R.id.etCoordenadas_GPV);
        etCiudad = (AutoCompleteTextView) findViewById(R.id.etCiudad_GPV);
        spnDeptos = (Spinner) findViewById(R.id.spndeptos_GPV);
        btnFotoSoporte = (Button) findViewById(R.id.btnTomarFotoSoporte_GPV);
        ivFotoSoporte = (ImageView) findViewById(R.id.ivFotoSoporte_GPV);
        chkIncluirFoto = (CheckBox) findViewById(R.id.chkIncluirFoto_GPV);
        setting = PreferenceManager.getDefaultSharedPreferences(this);
        tvInfoGPS = (TextView) findViewById(R.id.tvinfoGps_GPV);
        fabEdit = (FloatingActionButton) findViewById(R.id.fabEdit);

        cmg = new classMetodosGenerales(this);

        //esto es para el mapa
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        String[] list_depto = new String[]{"Boaco", "Carazo", "Chinandega", "Chontales", "Esteli", "Granada", "Jinotega", "Leon", "Madriz", "Managua", "Masaya", "Matagalpa", "Nueva Segovia", "RAAN", "RAAS", "Rio San Juan", "Rivas"};
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list_depto);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDeptos.setAdapter(dataAdapter);
        spnDeptos.setSelection(9);
    }

    private void eventosControles() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GuardarRegistro(METODO_GUARDAR);
            }
        });

        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GuardarRegistro(METODO_EDITAR);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnFotoSoporte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(frmGuardarPuntoVenta.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(frmGuardarPuntoVenta.this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                } else {

                    final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "edatel/clientes" + File.separator);
                    if (!root.exists())
                        root.mkdirs();

                    final String fname = "img_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".jpg";

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    ImagenTomadaCamara = Uri.fromFile(new File(root.getPath() + File.separator + fname));

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, ImagenTomadaCamara);
                    startActivityForResult(intent, REQUEST_CODE_CAMERA_PICTURE);
                }
            }
        });

        chkIncluirFoto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                try {
                    if (b && imagePath.length() > 0) {

                    } else {
                        chkIncluirFoto.setChecked(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        spnDeptos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 9) { //si es Managua
                    String[] ciudades = {"Managua", "Ciudad Sandino", "Mateare", "Carretera Sur", "El Crucero", "San Rafael del sur", "Masachapa", "Pochomil", "Carretera Vieja a Leon", "Villa El Carmen"};
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(frmGuardarPuntoVenta.this, android.R.layout.select_dialog_item, ciudades);
                    etCiudad.setAdapter(adapter);
                } else {
                    etCiudad.setAdapter(null);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void ObtenerDatosParaEditarCliente(String idCliente) {
        try {

            SQLiteDatabase db = new classSQLHELPER(this).getWritableDatabase();
            Cursor cCliente = db.rawQuery(String.format("select * from %s where idpdv = '%s'", TBL_PUNTOSDEVENTA, idCliente), null);
            if (cCliente.getCount() > 0) {
                cCliente.moveToFirst();
                cliente = new classClientesPDV(
                        cCliente.getInt(cCliente.getColumnIndex("idpdv")),
                        cCliente.getString(cCliente.getColumnIndex("NombrePdV")),
                        cCliente.getString(cCliente.getColumnIndex("NombrePropietario")),
                        cCliente.getString(cCliente.getColumnIndex("NumeroPOS")),
                        cCliente.getString(cCliente.getColumnIndex("NumeroTelefono")),
                        cCliente.getString(cCliente.getColumnIndex("CoordenadasGPS")),
                        cCliente.getString(cCliente.getColumnIndex("Ciudad")),
                        cCliente.getInt(cCliente.getColumnIndex("idDepartamento"))
                );

                etIDPdV.setText("" + cliente.getId());
                etNombrePdv.setText(cliente.getNombre());
                etNombrePropietario.setText(cliente.getPropietario());
                etNumeroPOS.setText(cliente.getNumero());
                etNumeroTelefono.setText(cliente.getOtronumero());
                etCiudad.setText(cliente.getCiudad());
                spnDeptos.setSelection(cliente.getIddDepto() - 1);
                fabEdit.setVisibility(View.VISIBLE);
                fab.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void GuardarRegistro(int metodoEnvio) {

        ((TextInputLayout) findViewById(R.id.tilCiudad_GPV)).setError(null);
        ((TextInputLayout) findViewById(R.id.tilNumerotelefono_GPV)).setError(null);
        ((TextInputLayout) findViewById(R.id.tilNumeroPOS_GPV)).setError(null);
        ((TextInputLayout) findViewById(R.id.tilnombrePropietario_GPV)).setError(null);
        ((TextInputLayout) findViewById(R.id.tilNombrePdV_GPV)).setError(null);


        View focus = null;
        boolean ok = true;

        if (TextUtils.isEmpty(etCiudad.getText())) {
            ((TextInputLayout) findViewById(R.id.tilCiudad_GPV)).setError("Por favor llene con la ciudad actual");
            focus = etCiudad;
            ok = false;
        }
        if (TextUtils.isEmpty(etNumeroPOS.getText())) {
            ((TextInputLayout) findViewById(R.id.tilNumeroPOS_GPV)).setError("Por favor llene con el POS del cliente");
            focus = etNumeroPOS;
            ok = false;
        }
        if (TextUtils.isEmpty(etNombrePropietario.getText())) {
            ((TextInputLayout) findViewById(R.id.tilnombrePropietario_GPV)).setError("Por favor llene con el nombre del propietario.");
            focus = etNombrePropietario;
            ok = false;
        }
        if (TextUtils.isEmpty(etNombrePdv.getText())) {
            ((TextInputLayout) findViewById(R.id.tilNombrePdV_GPV)).setError("Por favor llene con el nombre del pdv.");
            focus = etNombrePdv;
            ok = false;
        }

        if (!IsGPS) {
            ok = false;
            checkPlayServices();
        }

        if (metodoEnvio == METODO_EDITAR) {
            if (Integer.parseInt(etIDPdV.getText().toString()) == 0) {
                ((TextInputLayout) findViewById(R.id.tilIdPdV_GPV)).setError("No se puede editar el cliente.");
                ok = false;
            }
        }

        if (ok) {

            if (new classMetodosGenerales(this).TieneConexion(this)) {

                if (metodoEnvio == METODO_GUARDAR) {

                    new EnviarDatosDePdvAlServidor().execute();

                } else if (metodoEnvio == METODO_EDITAR) {

                    new EditarDatosDePdvAlServidor().execute();

                }
            } else {
                cmg.Toast(this, "No tienes conexión a internet.", R.drawable.ic_error);
            }
        }

    }

    private class EnviarDatosDePdvAlServidor extends AsyncTask<String, String, JSONObject> {
        classJSONParser jsonParser = new classJSONParser();
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(frmGuardarPuntoVenta.this);
            pDialog.setMessage("Enviando los datos al servidor. Espere...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    EnviarDatosDePdvAlServidor.this.cancel(true);
                    new AlertDialog.Builder(frmGuardarPuntoVenta.this).setMessage("Se ha cancelado la petición.").setIcon(R.drawable.ic_info).show();
                }
            });
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... strings) {

            // variables con los parametros a enviar
            String Departamento = (spnDeptos.getSelectedItemPosition() + 1) + "";
            String Ciudad = etCiudad.getText().toString();
            String NombrePdV = etNombrePdv.getText().toString();
            String NombrePropiet = etNombrePropietario.getText().toString();
            String NumeroPOS = etNumeroPOS.getText().toString();
            String TelefonoPropiet = etNumeroTelefono.getText().toString();
            String LocationGPS = Coordenadas;
            String UserSave = "" + setting.getInt("codUsuario", 0);

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idDepartamento", Departamento));
            params.add(new BasicNameValuePair("NombrePdV", cmg.quitarTildesEspeciales(NombrePdV)));
            params.add(new BasicNameValuePair("NombrePropietario", cmg.quitarTildesEspeciales(NombrePropiet)));
            params.add(new BasicNameValuePair("NumeroPOS", NumeroPOS.replaceAll("\\s", "")));
            params.add(new BasicNameValuePair("NumeroTelefono", TelefonoPropiet.replaceAll("\\s", "")));
            params.add(new BasicNameValuePair("Ciudad", Ciudad));
            params.add(new BasicNameValuePair("CoordenadasGPS", LocationGPS));
            params.add(new BasicNameValuePair("FechaRegistro", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
            params.add(new BasicNameValuePair("UsuarioGuardo", UserSave));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(URL_WS_GUARDAR_NUEVO_PUNTO, "POST", params);
            return json;
        }

        protected void onPostExecute(JSONObject file_url) {
            // check for success tag
            try {

                SharedPreferences.Editor editor = setting.edit();

                if (file_url == null) {
                    new AlertDialog.Builder(frmGuardarPuntoVenta.this).setTitle("Error al Obtener.").setMessage("Sin repuesta del servidor, o revisa la conexión de datos.").setIcon(R.drawable.ic_error).show();
                } else {
                    int status = file_url.getInt("status");
                    String message = file_url.getString("message");
                    if (status == 1) {
                        limpiarControles();
                        //si retorna un ok entonces se muestra el mensaje de guardado
                        new AlertDialog.Builder(frmGuardarPuntoVenta.this)
                                .setIcon(R.drawable.ic_success)
                                .setTitle("Guardado con éxito.")
                                .setMessage(message)
                                .setCancelable(true)
                                .show();
                    } else {
                        new AlertDialog.Builder(frmGuardarPuntoVenta.this)
                                .setIcon(R.drawable.ic_error)
                                .setTitle("Error al guardar.")
                                .setMessage(message)
                                .setCancelable(true)
                                .show();
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

    private class EditarDatosDePdvAlServidor extends AsyncTask<String, String, JSONObject> {
        classJSONParser jsonParser = new classJSONParser();
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(frmGuardarPuntoVenta.this);
            pDialog.setMessage("Enviando los datos al servidor para editar. Espere...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    EditarDatosDePdvAlServidor.this.cancel(true);
                    new AlertDialog.Builder(frmGuardarPuntoVenta.this).setMessage("Se ha cancelado la petición.").setIcon(R.drawable.ic_info).show();
                }
            });
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... strings) {

            // variables con los parametros a enviar
            String idcliente = etIDPdV.getText().toString();
            String Departamento = (spnDeptos.getSelectedItemPosition() + 1) + "";
            String Ciudad = etCiudad.getText().toString();
            String NombrePdV = etNombrePdv.getText().toString();
            String NombrePropiet = etNombrePropietario.getText().toString();
            String NumeroPOS = etNumeroPOS.getText().toString();
            String TelefonoPropiet = etNumeroTelefono.getText().toString();
            String LocationGPS = cliente.getCoordenadas(); //no se editan las coordenadas, se obtienen las anteriores
            String UserSave = "" + setting.getInt("codUsuario", 0);

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("idpdv", idcliente));
            params.add(new BasicNameValuePair("idDepartamento", Departamento));
            params.add(new BasicNameValuePair("NombrePdV", NombrePdV));
            params.add(new BasicNameValuePair("NombrePropietario", NombrePropiet));
            params.add(new BasicNameValuePair("NumeroPOS", NumeroPOS));
            params.add(new BasicNameValuePair("NumeroTelefono", TelefonoPropiet));
            params.add(new BasicNameValuePair("Ciudad", Ciudad));
            params.add(new BasicNameValuePair("CoordenadasGPS", LocationGPS));
            params.add(new BasicNameValuePair("FechaRegistro", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
            params.add(new BasicNameValuePair("UsuarioGuardo", UserSave));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(URL_WS_EDITAR_PUNTO, "POST", params);
            return json;
        }

        protected void onPostExecute(JSONObject file_url) {
            // check for success tag
            try {

                SharedPreferences.Editor editor = setting.edit();

                if (file_url == null) {
                    new AlertDialog.Builder(frmGuardarPuntoVenta.this).setTitle("Error al Obtener.").setMessage("Sin repuesta del servidor, o revisa la conexión de datos.").setIcon(R.drawable.ic_error).show();
                } else {
                    int status = file_url.getInt("status");
                    String message = file_url.getString("message");
                    if (status == 1) {
                        limpiarControles();
                        //si retorna un ok entonces se muestra el mensaje de guardado
                        new AlertDialog.Builder(frmGuardarPuntoVenta.this)
                                .setIcon(R.drawable.ic_success)
                                .setTitle("Actualizado con éxito.")
                                .setMessage(message)
                                .setCancelable(true)
                                .show();
                    } else {
                        new AlertDialog.Builder(frmGuardarPuntoVenta.this)
                                .setIcon(R.drawable.ic_error)
                                .setTitle("Error al guardar.")
                                .setMessage(message)
                                .setCancelable(true)
                                .show();
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

    private void limpiarControles() {
        etIDPdV.setText("0");
        etNombrePdv.setText("");
        etNombrePropietario.setText("");
        etNumeroPOS.setText("");
        etNumeroTelefono.setText("");
        etCiudad.setText("");
        fabEdit.setVisibility(View.GONE);
        fab.setVisibility(View.VISIBLE);
        imagePath = "";
        chkIncluirFoto.setChecked(false);
        ivFotoSoporte.setImageBitmap(null);
    }

    //region REGION DEL GPS
    @Override
    public void onLocationChanged(Location location) {
// Assign the new location
        mLastLocation = location;
        Log.w("onlocationchanged", "cambiando la ubicacion");

        // Displaying the new location on UI
        if (location.getAccuracy() < 500) {
            displayLocation();
            createMapView();
        }
        startLocationUpdates();

        double lat = location.getLatitude();
        double lng = location.getLongitude();

        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        StringBuilder builder = new StringBuilder();
        try {
            List<Address> address = geoCoder.getFromLocation(lat, lng, 1);
            //int maxLines = address.get(0).getMaxAddressLineIndex();
            for (int i = 0; i < 1; i++) {
                String addressStr = address.get(0).getAddressLine(i);
                builder.append(addressStr);
                builder.append(" ");
            }

            String locality = address.get(0).getLocality();
            String country = address.get(0).getCountryName();
            String Derpart = address.get(0).getAdminArea();
            String direcc = address.get(0).getFeatureName();

            //esta es la direccion que manda google
            String finalAddress = builder.toString(); //This is the complete address.

            tvInfoGPS.setText(String.format("\tGUIA\nDirección: %s.\nLugar:%s.\nDepto: %s.\nPais: %s.", direcc, locality, Derpart, country));
        } catch (IOException e) {
            tvInfoGPS.setText("N/D");
            e.printStackTrace();
        } catch (NullPointerException e) {
            tvInfoGPS.setText("N/D");
            startLocationUpdates();
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Once connected with google api, get the location
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        try {
            // Resuming the periodic location updates
            if (mGoogleApiClient.isConnected()) {
                startLocationUpdates();
                displayLocation();
            } else {
                mGoogleApiClient.connect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            stopLocationUpdates();
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /**
     * Method to display the location on UI
     */
    public Location displayLocation() {

        try {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();

                etCoordenadas.setText(String.format("%s, %s", latitude, longitude));
                Coordenadas = latitude + "," + longitude;
                IsGPS = true;

                return mLastLocation;
            } else {

                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showSettingsAlert();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mLastLocation;
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100);
        mLocationRequest.setFastestInterval(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(1);
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                } else {
                    finish();
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Starting the location updates
     */
    protected void startLocationUpdates() {

        try {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }

            //para mostar la alerta de que necesita encender el gps
            final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showSettingsAlert();
            }

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (Exception e) {
            e.printStackTrace();
            //new classExceptionHandler(this).uncaughtException(e);
        }
    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * funcion para mostrar alerta si el GPS no esta activado y enviarlo a la
     * configuracion
     */
    public void showSettingsAlert() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100); /// tenia 30 * 1000 como parametro
        mLocationRequest.setFastestInterval(10);   // tenia 5 * 1000 como parametro
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(frmGuardarPuntoVenta.this, 7);
                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    private void createMapView() {

        try {

            if (mGoogleMap == null) {
                mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
            }

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
            //BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.bg_gps_icon);


            LatLng mi_posic = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            Float bearing = null;
            if (mGoogleMap != null && mGoogleMap.getMyLocation() != null) {
                bearing = mGoogleMap.getMyLocation().getBearing();
            }
            if (bearing != null) {
                CameraPosition currentPlace = new CameraPosition.Builder()
                        .target(mi_posic)
                        .bearing(bearing).tilt(45.5f).zoom(ZoomActual).build();
                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
            }

            mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    if (cameraPosition.zoom != ZoomActual) {
                        ZoomActual = (int) cameraPosition.zoom;
                    }
                }
            });
            mGoogleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    mLastLocation = mGoogleMap.getMyLocation();
                    displayLocation();
                }
            });

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }
    //endregion

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode) {

            case REQUEST_CODE_CAMERA_PICTURE:

                if (resultCode == RESULT_OK) {  //si se tomo la fotografia
                    Uri selectedImageUri;
                    try {
                        selectedImageUri = ImagenTomadaCamara;// se obtiene el path de la imagen guardada
                        //Bitmap factory
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        // se hace la imagen mas pequeña, esto es para evitar el throws OutOfMemory Exception  en imagenes grandes
                        options.inSampleSize = 5;
                        final Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath(), options);
                        ivFotoSoporte.setImageBitmap(bitmap);

                        imagePath = selectedImageUri.getPath();
                        // se activa el check para confirmar subir esta foto tomada
                        chkIncluirFoto.setChecked(true);

                        // se guarda la ruta de la imagen en la base de datos para llevar un registro de las fotos por punto visitado
                        /*ContentValues values = new ContentValues();
                        values.put("Id", (byte[]) null);
                        values.put("IdAsistencia", setting.getString("strIdAsistencia", "0"));
                        values.put("puntoventa", gcodPdV);
                        values.put("FechaAsistenciaEntrada", setting.getString("strFechaEntr", ""));
                        values.put("AsistenciaConInternet", 0);
                        values.put("fotopath", selectedImageUri.getPath());
                        values.put("estEnvio", 0);
                        db.insert("tblAsistenciaFoto", null, values);*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                break;
        }
    }
}
