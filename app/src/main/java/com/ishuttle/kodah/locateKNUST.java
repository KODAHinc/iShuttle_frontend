package com.ishuttle.kodah;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class locateKNUST extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location mLastlocation;
    LocationRequest mLocationRequest;
    LatLng currentLocation;
    Marker mCurrent;
    ArrayList check=new ArrayList();
    Point p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locate_knust);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        check.add("false");
        Button btn_show = (Button) findViewById(R.id.map_button);
        btn_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                //Open popup window
                if (p != null)
                    showPopup(locateKNUST.this, p);
            }
        });
    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.raw_map));

            if (!success) {
                Log.e("TAG", "Stryle parsing failed");
            }
        }catch(Resources.NotFoundException e){
            Log.e("TAG","Can't find style.Error: ",e);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);


    }
    protected synchronized  void buildGoogleApiClient(){
        googleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastlocation=location;

        currentLocation=new LatLng(location.getLatitude(),location.getLongitude());

        if(check.get(0).equals("false")) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            check=new ArrayList();
            check.add("true");
        }
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

        //showLocation(mMap);
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);

        LatLng Petroleum=new LatLng(6.672822,-1.564204);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Petroleum).title("Petroleum Building").icon(BitmapDescriptorFactory.fromResource(R.mipmap.petroleum)));

        LatLng Auditorium=new LatLng(6.672523,-1.564919);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Auditorium).title("Engineering Auditorium").icon(BitmapDescriptorFactory.fromResource(R.mipmap.auditorium)));

        LatLng Vodafone=new LatLng(6.672789,-1.565525);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Vodafone).title("Vodafone Buildig").icon(BitmapDescriptorFactory.fromResource(R.mipmap.vodafone_cafe)));

        LatLng Airplane=new LatLng(6.673274,-1.565526);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Airplane).title("Airplane Building").icon(BitmapDescriptorFactory.fromResource(R.mipmap.airplane_building)));

        LatLng N_Block=new LatLng(6.674261,-1.565634);
        mCurrent=mMap.addMarker(new MarkerOptions().position(N_Block).title("N Block").icon(BitmapDescriptorFactory.fromResource(R.mipmap.n_building)));
        LatLng Lecture=new LatLng(6.673694,-1.565318);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Lecture).title("Lecture Theatre").icon(BitmapDescriptorFactory.fromResource(R.mipmap.lecture)));

        LatLng Science=new LatLng(6.673190,-1.566999);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Science).title("College of Science").icon(BitmapDescriptorFactory.fromResource(R.mipmap.science)));

        LatLng Business=new LatLng(6.668800,-1.568521);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Business).title("School of Business").icon(BitmapDescriptorFactory.fromResource(R.mipmap.business)));

        LatLng Health_science=new LatLng(6.672301,-1.568290);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Health_science).title("College of Health Sciences").icon(BitmapDescriptorFactory.fromResource(R.mipmap.health_science)));

        LatLng Humanities=new LatLng(6.674551,-1.565660);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Humanities).title("College of Health Sciences").icon(BitmapDescriptorFactory.fromResource(R.mipmap.socioso)));

        LatLng Agriculture=new LatLng(6.675349,-1.566429);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Agriculture).title("College of Agriculture").icon(BitmapDescriptorFactory.fromResource(R.mipmap.agriculture)));

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // Get the x and y position after the button is draw on screen
// (It's important to note that we can't get the position in the onCreate(),
// because at that stage most probably the view isn't drawn yet, so it will return (0, 0))
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        int[] location = new int[2];
        Button button = (Button) findViewById(R.id.map_button);

        // Get the x, y location and store it in the location[] array
        // location[0] = x, location[1] = y.
        button.getLocationOnScreen(location);

        //Initialize the Point with x, and y positions
        p = new Point();
        p.x = location[0];
        p.y = location[1];
    }

    // The method that displays the popup.
    private void showPopup(final Activity context, Point p) {
        int popupWidth = 200;
        int popupHeight = 150;

        // Inflate the popup_layout.xml
        LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.pop_up, viewGroup);

        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(context);
        popup.setContentView(layout);
        popup.setWidth(popupWidth);
        popup.setHeight(popupHeight);
        popup.setFocusable(true);

        // Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
        int OFFSET_X = 30;
        int OFFSET_Y = 30;

        // Clear the default translucent background
        popup.setBackgroundDrawable(new BitmapDrawable());

        // Displaying the popup at the specified location, + offsets.
        popup.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);

        // Getting a reference to Close button, and close the popup when clicked.
        Button close = (Button) layout.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
    }

}
