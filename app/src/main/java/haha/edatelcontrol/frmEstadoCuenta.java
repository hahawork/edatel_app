package haha.edatelcontrol;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class frmEstadoCuenta extends AppCompatActivity implements Fragmentestadocuentaclientes.OnArticuloSelectedListener {

    Fragmentmiestadocuenta fragmentmiestadocuenta;
    Fragmentestadocuentaclientes fragmentestadocuentaclientes;
    Fragmentestadocuentadetalles fragmentestadocuentadetalles;

    BottomNavigationView navigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            try {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        transaction.replace(R.id.fragment_container, fragmentmiestadocuenta);
                        //transaction.addToBackStack(null);
                        transaction.commit();

                        return true;
                    case R.id.navigation_dashboard:
                        transaction.replace(R.id.fragment_container, fragmentestadocuentaclientes);
                        //transaction.addToBackStack(null);
                        transaction.commit();

                        return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frm_estado_cuenta);


        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fragmentmiestadocuenta = new Fragmentmiestadocuenta();
        fragmentestadocuentaclientes = new Fragmentestadocuentaclientes();
        fragmentestadocuentadetalles = new Fragmentestadocuentadetalles();

        FragmentManager FM = getSupportFragmentManager();
        FragmentTransaction FT = FM.beginTransaction();
        FT.replace(R.id.fragment_container, fragmentmiestadocuenta);
        FT.commit();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String jsonCliente = bundle.getString("jsonCliente");
            if (jsonCliente != null) {
                onArticuloSelected(jsonCliente);
            }
        }
      /*  String a = System.getenv("SECONDARY_STORAGE");
        Toast.makeText(this, a, Toast.LENGTH_LONG).show();*/

    }

    public void onArticuloSelected(String str) {

        Fragment fragment = new Fragmentestadocuentadetalles();

        Bundle args = new Bundle();
        args.putString("jsonCliente", str);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }
}
