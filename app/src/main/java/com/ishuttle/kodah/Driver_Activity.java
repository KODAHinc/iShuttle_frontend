package com.ishuttle.kodah;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class Driver_Activity extends AppCompatActivity implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    TextView NameTV,LatTV,LngTV;
    //String data="Mr Dummy Data";
    String[] SPINNERLIST = {"C-Commercial to Business School", "B-Brunei to Business School", "A-Gaza to Business School"};
    GoogleApiClient googleApiClient;
    Location mLastlocation;
    LocationRequest mLocationRequest;
    Double lat,lng;
    String lat_geo,lng_geo;
    String route,method;
    int input_route;
    Spinner betterSpinner;
    InputStream is=null;
    String line=null;
    String result=null;
    String data=null;
    String[] UsernameArray,PasswordArray,IDArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        NameTV=(TextView)findViewById(R.id.Name_ID);
        LatTV=(TextView)findViewById(R.id.latID);
        LngTV=(TextView)findViewById(R.id.lngID);


        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item, SPINNERLIST);
        betterSpinner = (Spinner) findViewById(R.id.spinner);
        betterSpinner.setAdapter(arrayAdapter);
        String DriverId=getIntent().getStringExtra("value");
        new setName().execute(DriverId);

        buildGoogleApiClient();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


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
        String Id=getIntent().getStringExtra("value");
        lat=location.getLatitude();
        lng=location.getLongitude();
        lat_geo=lat.toString();
        lng_geo=lng.toString();
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
                route="A";
                break;
        }
        method="geostore";
        BackgroundTask backgroundTask=new BackgroundTask(this);
        backgroundTask.execute(method,Id,route,lat_geo,lng_geo);

        LatTV.setText(lat_geo);
        LngTV.setText(lng_geo);
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


                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    IDArray[i] = jo.getString("Drivers_id");
                    UsernameArray[i] = jo.getString("Username");
                    if ((IDArray[i].equals(DriverId))) {
                        data = UsernameArray[i];

                    }


                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return data;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String Name="Mr. "+s;
            NameTV.setText(Name);
        }

    }
}
