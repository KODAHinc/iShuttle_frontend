package com.ishuttle.kodah;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

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


public class locateKNUST extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location mLastlocation;
    LocationRequest mLocationRequest;
    LatLng currentLocation;
    Marker mCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locate_knust);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

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
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));

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

        LatLng Pharmacy=new LatLng(6.674253,-1.566354);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Pharmacy).title("Pharmacy Building\nBUILDINGS AROUND\n\nCollege of Science\nChemistry Lab").icon(BitmapDescriptorFactory.fromResource(R.mipmap.pharmacy)));

        LatLng UITS=new LatLng(6.675671,-1.567062);
        mCurrent=mMap.addMarker(new MarkerOptions().position(UITS).title("University Information Technology Services").icon(BitmapDescriptorFactory.fromResource(R.mipmap.uits)));

        LatLng IDL=new LatLng(6.675585,-1.567534);
        mCurrent=mMap.addMarker(new MarkerOptions().position(IDL).title("Institute of Distant Learning").icon(BitmapDescriptorFactory.fromResource(R.mipmap.idl)));

        LatLng Engineering_Auditorium=new LatLng(6.672822,-1.565020);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Engineering_Auditorium).title("Engineering_Auditorium\nBUILDINGS AROUND\n\nVodafone Cafe\nLecturer's Offices\nEngineering Labs").icon(BitmapDescriptorFactory.fromResource(R.mipmap.engineering_auditorium)));

        LatLng Petroleum_Building=new LatLng(6.672920,-1.563536);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Petroleum_Building).title("Petroleum_Building\nBUILDINGS AROUND\n\n Engineering Gate").icon(BitmapDescriptorFactory.fromResource(R.mipmap.pb)));

        LatLng Coke_Stand=new LatLng(6.674429,-1.565562);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Coke_Stand).title("Coke_Stand\nBUILDINGS AROUND\n\nDepartment of languages\nAgric faculty ").icon(BitmapDescriptorFactory.fromResource(R.mipmap.coke)));

        LatLng SMS_canteen=new LatLng(6.673055,-1.568180);
        mCurrent=mMap.addMarker(new MarkerOptions().position(SMS_canteen).title("SMS Canteen").icon(BitmapDescriptorFactory.fromResource(R.mipmap.canteen)));

        LatLng Faculty_of_Arts=new LatLng(6.668273,-1.565847);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Faculty_of_Arts).title("Faculty of Arts\nPLACES AROUND\n\nCommercial Area\nLaw Faculty").icon(BitmapDescriptorFactory.fromResource(R.mipmap.arts)));

        LatLng School_of_Business=new LatLng(6.668817,-1.567885);
        mCurrent=mMap.addMarker(new MarkerOptions().position(School_of_Business).title("School of Business").icon(BitmapDescriptorFactory.fromResource(R.mipmap.business)));

        LatLng CCB=new LatLng(6.675973,-1.565114);
        mCurrent=mMap.addMarker(new MarkerOptions().position(CCB).title("CCB\nBUILDINGS AROUND\n\nSFED\nAyeduase Gate").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ccb)));

        LatLng Republic_Hall=new LatLng(6.678277,-1.565114);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Republic_Hall).title("Republic Hall").icon(BitmapDescriptorFactory.fromResource(R.mipmap.republic)));

        LatLng Katanga_Hall=new LatLng(6.672408,-1.563582);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Katanga_Hall).title("Katanga Hall").icon(BitmapDescriptorFactory.fromResource(R.mipmap.katanga)));

        LatLng Brunei=new LatLng(6.69765,-1.575760);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Brunei).title("GUSS Hostels:Brunei").icon(BitmapDescriptorFactory.fromResource(R.mipmap.brunei)));

        LatLng Africa_Hall=new LatLng(6.680589,-1.575540);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Africa_Hall).title("Africa Hall\nBUILDINGS AROUND\n\nAU Gardens").icon(BitmapDescriptorFactory.fromResource(R.mipmap.africa)));

        LatLng Unity_Hall=new LatLng(6.679864,-1.571742);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Unity_Hall).title("Unity Hall").icon(BitmapDescriptorFactory.fromResource(R.mipmap.unity)));

        LatLng Queens_Hall=new LatLng(6.677083,-1.574103);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Queens_Hall).title("Queens Hall").icon(BitmapDescriptorFactory.fromResource(R.mipmap.queens)));

        LatLng Independent_Hall=new LatLng(6.676881,-1.571378);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Independent_Hall).title("Indece hall").icon(BitmapDescriptorFactory.fromResource(R.mipmap.indece)));

        LatLng Main_Administration=new LatLng(6.674707,-1.569983);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Main_Administration).title("Main Administration").icon(BitmapDescriptorFactory.fromResource(R.mipmap.admin)));

        LatLng Paa_Joe=new LatLng(6.676764,-1.569983);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Paa_Joe).title("Paa Joe").icon(BitmapDescriptorFactory.fromResource(R.mipmap.paajoe)));

        LatLng Great_Hall=new LatLng(6.674664,-1.572461);
        mCurrent=mMap.addMarker(new MarkerOptions().position(Great_Hall).title("Great Hall,\nMain Library right beside").icon(BitmapDescriptorFactory.fromResource(R.mipmap.greathall)));

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
