package haha.edatelcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import haha.edatelcontrol.clases.classClientesPDV;
import haha.edatelcontrol.clases.classColorGenerator;
import haha.edatelcontrol.clases.classExceptionHandler;
import haha.edatelcontrol.clases.classMetodosGenerales;
import haha.edatelcontrol.clases.classSQLHELPER;
import haha.edatelcontrol.clases.classTextDrawable;

import static haha.edatelcontrol.interfaceDefineVariables.TBL_ASISTENCIAS_MARCADAS;
import static haha.edatelcontrol.interfaceDefineVariables.TBL_PUNTOSDEVENTA;

public class frmMarcarVisitas extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, android.location.LocationListener, LocationListener, OnMapReadyCallback {

    // Google client to interact with Google API
    //esto es para el gps
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    PendingResult<LocationSettingsResult> result;

    //esto es para el mapa
    GoogleMap googleMap;
    SupportMapFragment mapFragment = null;
    double latitude, longitude;
    boolean isGPS = false;
    private Location mLastLocation;
    float ZoomActual = 17f;

    //esto es para la base de datos
    SQLiteDatabase db;
    classSQLHELPER Helper;
    Context mContext;
    Activity mActivity;
    FloatingActionButton fab;
    TextView tvPosicion;
    EditText etFechaHora, etComentarios;
    private Handler customHandler = new Handler();
    Button btngetPosicion, btnVenderRecarga, btnRefrescar, btnActualizarClientes;
    Spinner spnPdvEncontrados;
    classMetodosGenerales cmg;
    boolean isPuntoEspecial = false;
    SharedPreferences setting;
    int IdAsistenciaMarcada = 0;

    List<classClientesPDV> arrListClientes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marcar_asistencias);

        setToolBar();
        setupWindowAnimations();
        iniciarInstancias();
        eventosControles();

        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
            startLocationUpdates();
        }
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void iniciarInstancias() {
        try {

            fab = (FloatingActionButton) findViewById(R.id.fab);
            etFechaHora = (EditText) findViewById(R.id.etFechaHora_ME);
            btngetPosicion = (Button) findViewById(R.id.btngetPosocion_MA);
            spnPdvEncontrados = (Spinner) findViewById(R.id.spnPdvEncontrado_MA);
            tvPosicion = (TextView) findViewById(R.id.tvPosicion_ME);
            Helper = new classSQLHELPER(this);
            db = Helper.getWritableDatabase();
            cmg = new classMetodosGenerales(this);
            etComentarios = (EditText) findViewById(R.id.etComentario_ME);
            mContext = this;
            mActivity = this;
            setting = PreferenceManager.getDefaultSharedPreferences(mContext);
            btnVenderRecarga = (Button) findViewById(R.id.btnVenderRecarga);
            btnRefrescar = (Button) findViewById(R.id.btnRefrescarPantalla);
            btnActualizarClientes = (Button) findViewById(R.id.btnActualizaClientes);

            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            customHandler.postDelayed(updateTimerThread, 1000);

            arrListClientes.clear();
            arrListClientes.add(new classClientesPDV(0, "Presionar ===>", "", ""));
            spnPdvEncontrados.setAdapter(new Custom_Spn_Lista(frmMarcarVisitas.this, arrListClientes));

        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
        }
    }

    private void eventosControles() {

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MarcarEntrada();

            }
        });

        btngetPosicion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListaDePuntosVentaCercanos();
            }
        });

        btnActualizarClientes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cmg.Toast(frmMarcarVisitas.this, "por favor espere", R.drawable.ic_info);
                cmg.VerificarNuevosPuntosguardados(true, setting.getInt("codUsuario", 0), 0);
            }
        });
        btnRefrescar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recreate();
            }
        });
        btnVenderRecarga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(frmMarcarVisitas.this, frmVender.class));

            }
        });
    }

    /**
     * si la version del sistema operativo soporta animaciones
     */
    private void setupWindowAnimations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition t3 = TransitionInflater.from(this)
                    .inflateTransition(R.transition.detail_enter_trasition);
            getWindow().setEnterTransition(t3);
        }
    }

    /**
     * evento que se ejecuta para actualizar la fecha y la hora actual en el campo de fecha y hora cada 1000 ms
     */
    //region actualizador de fechayhora
    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            try {
                String HA = new SimpleDateFormat("hh:mm:ss a").format(new Date()) + "\n" + new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
                etFechaHora.setText(HA);

            } catch (Exception e) {
                e.printStackTrace();
            }
            customHandler.postDelayed(this, 1000);
        }
    };
    //endregion

    private void ListaDePuntosVentaCercanos() {
        try {

            if (isGPS) {

                arrListClientes.clear();

                Cursor cPdv = db.rawQuery(String.format("SELECT idpdv, NombrePdV, NumeroPOS, CoordenadasGPS FROM %s", TBL_PUNTOSDEVENTA), null);

                if (cPdv.getCount() > 0) {

                    for (cPdv.moveToFirst(); !cPdv.isAfterLast(); cPdv.moveToNext()) {
                        Log.w("cursor", String.format("%s, %s, %s, %s.", cPdv.getString(0), cPdv.getString(1), cPdv.getString(2), cPdv.getString(3)));
                        // result += cursor.getString(iRow) + ": " + cursor.getString(iName) + " - " + cursor.getDouble(iLat) + " latitude " + cursor.getDouble(iLon) + " longitude\n";
                   /* }
                    if (cPdv.moveToFirst()) {
                        do {*/
                        boolean puntodentrodelradio = cmg.PdVCercano(cPdv.getString(3), mLastLocation);

                        if (puntodentrodelradio) {
                            arrListClientes.add(new classClientesPDV(
                                            Integer.parseInt(cPdv.getString(cPdv.getColumnIndex("idpdv"))),
                                            cPdv.getString(cPdv.getColumnIndex("NombrePdV")),
                                            cPdv.getString(cPdv.getColumnIndex("NumeroPOS")),
                                            cPdv.getString(cPdv.getColumnIndex("CoordenadasGPS"))
                                    )
                            );
                        }
                    } //while (cPdv.moveToNext());

                    if (arrListClientes.isEmpty()) {
                        PdVNoEncontrado();
                    } else {
                        isPuntoEspecial = false;
                        spnPdvEncontrados.setAdapter(new Custom_Spn_Lista(frmMarcarVisitas.this, arrListClientes));
                    }
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("No puntos de venta")
                            .setMessage("Aun no has descargado los puntos de venta, debes actualizar al menos una vez.")
                            .setCancelable(true)
                            .setPositiveButton("Descargar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    cmg.Toast(frmMarcarVisitas.this, "por favor espere", R.drawable.ic_info);
                                    cmg.VerificarNuevosPuntosguardados(true, setting.getInt("codUsuario", 0), 0);
                                }
                            })
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
                //**********************************************************************
            } else {
                Toast.makeText(frmMarcarVisitas.this, "Aún no se ha obtenido su ubicación", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
        }
    }


    /**
     * Muestra el mensaje de dialgo e punto de vent no encontrado. con las opciones para ese momento.
     */
    private void PdVNoEncontrado() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No se ha encontrado un punto de venta cercano");
        builder.setCancelable(true);
        builder.setItems(new String[]{"Cancelar", "Actividad especial (no es punto de venta)"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:

                        break;
                    case 1:
                        isPuntoEspecial = true;
                        arrListClientes.clear();
                        arrListClientes.add(new classClientesPDV(1, "Punto Especial", "", ""));
                        spnPdvEncontrados.setAdapter(new Custom_Spn_Lista(frmMarcarVisitas.this, arrListClientes));
                        break;
                }
            }
        });
        builder.create();
        builder.show();
    }

    /**
     * Este metodo es para guardar una asistencia a un pdv, si tiene internet lo envia, de lo contrario lo
     * guarda localmente
     */
    public void MarcarEntrada() {
        try {

            if (isGPS) {        // si ya se obtuvieron las coordenadas

                int IdPdV = 0;

                String Fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String Comentario = !isPuntoEspecial ? etComentarios.getText().toString() : etComentarios.getText().toString() + " ~ " + tvPosicion.getText().toString();

                IdPdV = isPuntoEspecial ? 1 : arrListClientes.get(spnPdvEncontrados.getSelectedItemPosition()).getId();      //#1 es el registro con el punto de venta especial en la base de datos

                if (IdPdV > 0) {    // SI YA SELECCIONO UN PUNTO DE VENTA
                    // se guardara local siempre para siempre tener registros aun que  falle el internet
                    try {

                        // se preparan los valores a insertar en la bd
                        ContentValues values = new ContentValues();
                        values.put("IdAsistencia", (byte[]) null);
                        values.put("codUsuario", setting.getInt("codUsuario", 0));
                        values.put("Pdv", IdPdV);
                        values.put("Fecha", Fecha);
                        values.put("Comentario", cmg.quitarTildesEspeciales(Comentario));
                        values.put("estado", 0);
                        values.put("IdEnviado", 0);

                        // se inserta en el bd
                        int IdInsert = (int) db.insert(TBL_ASISTENCIAS_MARCADAS, null, values);

                        // si la  insercion fue exitosa
                        if (IdInsert > 0) {
                            IdAsistenciaMarcada = IdInsert;
                            etComentarios.setText("");

                            if (cmg.TieneConexion(mContext)) {  // SI TIENE CONEXION A INTERNET

                                String consulta = "SELECT * FROM " + TBL_ASISTENCIAS_MARCADAS + " WHERE IdAsistencia = '" + IdAsistenciaMarcada + "'";
                                Cursor cAsistenc = db.rawQuery(consulta, null);

                                if (cAsistenc.moveToFirst()) {

                                    cmg.CrearNuevaAsistenciaOnline(
                                            true,
                                            IdAsistenciaMarcada,
                                            cAsistenc.getString(cAsistenc.getColumnIndex("codUsuario")),
                                            cAsistenc.getString(cAsistenc.getColumnIndex("Pdv")),
                                            cAsistenc.getString(cAsistenc.getColumnIndex("Comentario")),
                                            cAsistenc.getString(cAsistenc.getColumnIndex("Fecha")));
                                }
                            } else {

                                new AlertDialog.Builder(mContext)
                                        .setTitle("Éxito marcar Entrada")
                                        .setMessage("Has marcado tu asistencia con éxito localmente.\nVerifique estado en envios pendientes.")
                                        .setIcon(R.drawable.ic_success)
                                        .setCancelable(true)
                                        .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                recreate();
                                            }
                                        })
                                        .show();
                                etComentarios.setText("");
                            }

                            //verifica si el cliente tiene abonos pendientes de pagar
                            Double saldo = cmg.VerificaClienteTieneSaldoPendientesDeAbonar(IdPdV);
                            if (saldo > 0) {
                                cmg.NotificacionClienteTieneSaldoPendienteAbono(IdPdV, saldo);
                            }

                            customHandler.removeCallbacks(updateTimerThread);

                        } else { // si no se inserto con exito
                            cmg.Toast(mActivity, "Error al guardar ", R.drawable.ic_error);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else { // SI NO HA SELECCIONADO UN PDV

                    cmg.Toast(mActivity, "Obtenga el punto de venta..", R.drawable.ic_error);
                }

            } else {        // SI NO SE HAN OBTENIDO LAS COORDENADAS
                cmg.Toast(mActivity, "Aun no se ha obtenido su ubicación", R.drawable.ic_error);
            }

        } catch (Exception e) {
            cmg.Toast(mActivity, "Ha ocurrido un error " + e.getMessage(), R.drawable.ic_error);
            e.printStackTrace();
        }
    }

    public class Custom_Spn_Lista extends ArrayAdapter<classClientesPDV> {

        private final Activity context;
        private final List<classClientesPDV> Pdv;

        public Custom_Spn_Lista(Activity context, List<classClientesPDV> pdv) {
            super(context, R.layout.list_pdv_find, pdv);
            this.context = context;
            this.Pdv = pdv;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.list_pdv_find, null, true);

            try {
                TextView txtid = (TextView) rowView.findViewById(R.id.tv_id_spnfind);
                //txtid.setText(Idpdv.get(position));
                ImageView number = (ImageView) rowView.findViewById(R.id.image_view_spnfind);
                //get first letter of each String item
                String cantidad = String.valueOf(Pdv.get(position).getId() == 0 ? 0 : position + 1);
                classColorGenerator generator = classColorGenerator.MATERIAL; // or use DEFAULT
                // generate random color
                int color = generator.getColor(Pdv.get(position));
                classTextDrawable drawable = classTextDrawable.builder().buildRound(cantidad, color); // radius in px
                number.setImageDrawable(drawable);

                TextView txtpdv = (TextView) rowView.findViewById(R.id.tv_pdv_spnfind);
                txtpdv.setText(Pdv.get(position).getNombre());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return rowView;
        }

    }

    //region REGION DEL GPS
    @Override
    public void onLocationChanged(Location location) {
// Assign the new location
        try {
            mLastLocation = location;
            Log.w("onlocationchanged", "cambiando la ubicacion");
            // Displaying the new location on UI
            if (location.getAccuracy() < 500) {
                displayLocation();
                createMapView();
            }
            startLocationUpdates();

        } catch (NullPointerException e) {
            startLocationUpdates();
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

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
            } else {
                mGoogleApiClient.connect();
            }
            startLocationUpdates();
        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
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
            new classExceptionHandler(mContext).uncaughtException(e);
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

                tvPosicion.setText(String.format("Lat: %s, Long: %s", latitude, longitude));
                isGPS = true;

                return mLastLocation;
            } else {

                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showSettingsAlert();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(mContext).uncaughtException(e);
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

    private void createMapView() {

        try {

            if (googleMap == null) {
                mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
            }

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            //BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.bg_gps_icon);


            LatLng mi_posic = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            Float bearing = null;
            if (googleMap != null && googleMap.getMyLocation() != null) {
                bearing = googleMap.getMyLocation().getBearing();
            }
            if (bearing != null) {
                CameraPosition currentPlace = new CameraPosition.Builder()
                        .target(mi_posic)
                        .bearing(bearing).tilt(45.5f).zoom(ZoomActual).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
            }

            googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    if (cameraPosition.zoom != ZoomActual) {
                        ZoomActual = (int) cameraPosition.zoom;
                    }
                }
            });
            googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    mLastLocation = googleMap.getMyLocation();
                    displayLocation();
                }
            });

        } catch (Exception exception) {
            exception.printStackTrace();
        }
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
                        111).show();
            } else {

                cmg.Toast(mActivity, "Dispositivo no soporta play services.", R.drawable.ic_error);
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
            //new classExceptionHandler(mContext).uncaughtException(e);
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
            new classExceptionHandler(mContext).uncaughtException(e);
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
                            status.startResolutionForResult(frmMarcarVisitas.this, 7);
                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;

        /*// Add a marker in Sydney, Australia, and move the camera.
        LatLng sydney = new LatLng(-34, 151);
        this.googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

    }

    //endregion
}
