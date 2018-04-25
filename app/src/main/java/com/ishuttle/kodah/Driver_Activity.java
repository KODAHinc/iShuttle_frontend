package com.ishuttle.kodah;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Driver_Activity extends AppCompatActivity implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    TextView NameTV,LatTV,LngTV;
    String[] SPINNERLIST = {"C-Commercial to Business School", "B-Brunei to Business School", "A-Gaza to Business School"};
    GoogleApiClient googleApiClient;
    Location mLastlocation;
    LocationRequest mLocationRequest;
    Double lat,lng;
    String New_lat,New_lng;
    String route,method;
    int input_route;
    Spinner betterSpinner;
    InputStream is=null;
    String line=null;
    String result=null;
    String data=null;
    String[] UsernameArray,IDArray,LatArray,LngArray;
    String[] Geolocation;
    Location Brunei,Commercial,Gaza;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        NameTV=findViewById(R.id.Name_ID);
        LatTV=findViewById(R.id.latID);
        LngTV=findViewById(R.id.lngID);


        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item, SPINNERLIST);
        betterSpinner = (Spinner) findViewById(R.id.spinner);
        betterSpinner.setAdapter(arrayAdapter);

        Brunei=new Location("");
        Commercial=new Location("");
        Brunei.setLatitude(6.6702854);
        Brunei.setLongitude(-1.5743008);

        Commercial.setLatitude(6.6827207);
        Commercial.setLongitude(-1.5769408);

        Gaza.setLatitude(6.687655);
        Gaza.setLongitude(-1.556916);

        String DriverId=getIntent().getStringExtra("value");
        new setName().execute(DriverId);

        buildGoogleApiClient();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastlocation=location;

        lat=location.getLatitude();
        lng=location.getLongitude();
        New_lat=lat.toString();
        New_lng=lng.toString();


        if(location.distanceTo(Brunei)<=50){
            betterSpinner.setSelection(1);
        }
        if(location.distanceTo(Commercial)<=50){
            betterSpinner.setSelection(0);
        }
        if(location.distanceTo(Gaza)<=50){
            betterSpinner.setSelection(2);
        }

        input_route = betterSpinner.getSelectedItemPosition();
        switch(input_route){
            case 0:
                route="C";
                break;
            case 1:
                route="B";
                break;
            case 2:
                route="A";
                break;
            default:
                route="B";
                break;
        }
        LatTV.setText(New_lat);
        LngTV.setText(New_lng);
        String DriverId=getIntent().getStringExtra("value");
        method="geostore";
        BackgroundTask backgroundTask=new BackgroundTask(getApplicationContext());
        backgroundTask.execute(method,DriverId,route,New_lat,New_lng);

        //LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
    }
    protected synchronized  void buildGoogleApiClient(){
        googleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }
    @SuppressLint("StaticFieldLeak")
    class setName extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {

            try {

                String DriverId = params[0];

                URL url = new URL("https://kodahinc.000webhostapp.com/testlogin.php");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                is = new BufferedInputStream(con.getInputStream());
                //READ IS content into a string
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();


                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                result = sb.toString();
                is.close();
                br.close();

                //PARse JSON DATA

                JSONArray ja = new JSONArray(result);

                UsernameArray = new String[ja.length()];
                IDArray = new String[ja.length()];
                LatArray= new String[ja.length()];
                LngArray=new String[ja.length()];
                Geolocation=new String[2];
                //Map<String,String> dataMap=new HashMap<>(1);


                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    IDArray[i] = jo.getString("Drivers_id");
                    UsernameArray[i] = jo.getString("Username");
                    if ((IDArray[i].equals(DriverId))) {
                        data = UsernameArray[i];


                        return data;
                    }


                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e("IOexcep","Not Connected");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("IOexcep","Not Connected");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSONexcep","JSON Error");
            }


            return null;
        }


        @Override
        protected void onPostExecute(String data) {
            String Name = null;
            String Id=getIntent().getStringExtra("value");
                Name = "Mr. " + data;

            NameTV.setText(Name);


        }

    }
}
