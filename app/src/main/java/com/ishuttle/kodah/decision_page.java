package com.ishuttle.kodah;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class decision_page extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this,new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION}, 1);}

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setContentView(R.layout.activity_decision_page);
                }
                else {
                    finish();
                }
                return;
            }

    }
    }

    public void DriverClick(View view){

        startActivity(new Intent(this,log_in.class));
    }
    public void StudentClick(View view){
       // if(isInternetAccessible()) {
            finish();
            startActivity(new Intent(this, MapsActivity.class));
       // }else{
           // Toast.makeText(decision_page.this,"No Internet Connection",Toast.LENGTH_LONG).show();
       // }
    }
    public void LocateClick(View view){
        Toast.makeText(this, "Coming SOON!!", Toast.LENGTH_LONG).show();

    }


    public boolean isNetworkAvailable(){
        ConnectivityManager manager =(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable=false;
        if (networkInfo != null&& networkInfo.isConnectedOrConnecting()) {
            isAvailable = true;
        }else {
            Toast.makeText(decision_page.this,"Not Connected", Toast.LENGTH_SHORT).show();
        }
        return isAvailable;
    }
    public boolean isInternetAccessible(){
        if(isNetworkAvailable()){
            try {
                HttpURLConnection urlc=(HttpURLConnection)(new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent","Test");
                urlc.setRequestProperty("Connection","close");
                urlc.setConnectTimeout(1500);
                     urlc.connect();

                return (urlc.getResponseCode()==200);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("IOerror","I/O exception");
            }
        }else{
            Log.e("NetState","Internet not Available!");
        }
        Toast.makeText(this,"Network not Available",Toast.LENGTH_SHORT).show();
        return false;
    }
}
