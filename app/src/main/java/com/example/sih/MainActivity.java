package com.example.sih;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.Context;
import android.provider.Telephony;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.content.DialogInterface;
import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {

    TelephonyManager Tel;
    MyPhoneStateListener MyListener;

    private static final int PERMISSION_REQUEST_CODE = 200;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    //the client
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyListener = new MyPhoneStateListener();
        Tel = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        Button button = (Button)findViewById(R.id.button);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                if (checkLocationPermission()) {
                    getLocation();
                }
                if(checkPermission()){
                    realMainActivity();
                }
                else{
                    requestPermission();
                }
            }

        });


    }

    private class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            TextView signaltext = findViewById(R.id.signaltext);
            signaltext.setText("Signal: " + String.valueOf(signalStrength.getGsmSignalStrength()));

        }

    }
    private boolean checkLocationPermission() {
        //check the location permissions and return true or false.
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //permissions granted
            Toast.makeText(getApplicationContext(), "permissions granted", Toast.LENGTH_LONG).show();
            return true;
        } else {
            //permissions NOT granted
            //if permissions are NOT granted, ask for permissions
            Toast.makeText(getApplicationContext(), "Please enable permissions", Toast.LENGTH_LONG).show();
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Permissions request")
                        .setMessage("we need your permission for location in order to show you this example")
                        .setPositiveButton("Ok, I agree", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    getLocation();
                } else {
                    // permission denied
                    TextView locationText = findViewById(R.id.locationtext);
                    locationText.setText("location: permission denied");

                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Cannot get the location!")
                            .setPositiveButton("OK", null)
                            .setNegativeButton("Cancel", null)
                            .create()
                            .show();
                }
        }
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);

    }

    public void getLocation() {
        Toast.makeText(getApplicationContext(), "getLocation", Toast.LENGTH_LONG).show();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //request the last location and add a listener to get the response. then update the UI.
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location.
                    TextView locationText = findViewById(R.id.locationtext);
                    if (location != null) {
                        locationText.setText("location: " + location.toString());
                    } else {
                        locationText.setText("location: IS NULL");
                    }
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "getLocation ERROR", Toast.LENGTH_LONG).show();
            TextView locationText = findViewById(R.id.locationtext);
            locationText.setText("location: ERROR");
        }
    }

    private void realMainActivity(){
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        String simCountryIso = tm.getSimCountryIso();
        String simOperator = tm.getSimOperator();
        String simOperatorName = tm.getSimOperatorName();

        TextView countryiso = findViewById(R.id.countryiso);
        TextView simoperator = findViewById(R.id.operator);
        TextView operatorname = findViewById(R.id.operatorname);
        TextView simstate = findViewById(R.id.state);

        int simState = tm.getSimState();
        String sSimStateString = "Not Defined";
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                sSimStateString = "ABSENT";
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                sSimStateString = "NETWORK_LOCKED";
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                sSimStateString = "PIN_REQUIRED";
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                sSimStateString = "PUK_REQUIRED";
                break;
            case TelephonyManager.SIM_STATE_READY:
                sSimStateString = "STATE_READY";
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                sSimStateString = "STATE_UNKNOWN";
                break;
        }
        countryiso.setText(simCountryIso);
        simoperator.setText(simOperator);
        operatorname.setText(simOperatorName);
        simstate.setText(sSimStateString);
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

}