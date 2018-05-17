package com.ishuttle.kodah;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.Duration;
import com.google.maps.model.TrafficModel;
import com.google.maps.model.TransitMode;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;
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
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static android.location.LocationManager.NETWORK_PROVIDER;
import static java.lang.Thread.sleep;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location mLastlocation;
    LocationRequest mLocationRequest;
    InputStream is = null;
    String line = null;
    String result = null;
    String[] SPINNERLIST = {"C-Commercial to Business School", "B-Brunei to Business School", "A-Gaza to Business School"};
    String[] NewLatArray, NewLngArray, LatArray, LngArray, RouteArray;
    List<LatLng> OldgeoCordinates;
    List<Map<String, LatLng>> NewgeoCordinates;
    Map<String, LatLng> geoCordinates;
    Map<Integer, Map<LatLng, String>> Extract;
    List<LatLng> NewLatLng;

    LatLng currentLocation, index;
    LatLng[] oldlatLng = null;

    List<String> listRoutes;
    List<String[]> Testing;
    Spinner routeSpinner;
    LocationManager locationManager;
    Marker[] mk = new Marker[20];
    /*int[] setA = new int[20];
    int[] setB = new int[20];
    int[] setC = new int[20];
    */
    int[] set = new int[20];

    ArrayList check=new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, SPINNERLIST);
        routeSpinner = findViewById(R.id.map_spinner);
        routeSpinner.setAdapter(arrayAdapter);

        StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder().permitNetwork().build()));

        /*Arrays.fill(setA, 0);
        Arrays.fill(setB, 0);
        Arrays.fill(setC, 0);*/
        Arrays.fill(set, 0);


/*        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000,
                10, (LocationListener) this);*/

        check.add("false");


    }

    @Override
    public void onBackPressed() {
        finish();
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

        if(oldlatLng!=null)
        new isInternetAccessibleThread().execute();


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

        //new LocationTask((LocationTask.AsyncResponse) this).execute();

        LocationTask locationTask= new LocationTask(new LocationTask.AsyncResponse() {
            @Override
            public void processFinish(LatLng[] output) {
                oldlatLng=output;

            }

        });
        locationTask.execute();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    @SuppressLint("StaticFieldLeak")
    class FetchGeo extends AsyncTask<Void,Void, List<String[]>>{
        @SuppressLint("UseSparseArrays")
        @Override
        protected List<String[]> doInBackground(Void... voids) {
            URL url;

            try {
                url = new URL("http://wigsbydebs.xyz/getlocation.php");

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                is = new BufferedInputStream(con.getInputStream());
                //READ IS content into a string
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String route;


                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                result = sb.toString();
                is.close();
                br.close();

                JSONArray ja = new JSONArray(result);

                NewLatArray = new String[ja.length()];
                NewLngArray = new String[ja.length()];
                LatArray=new String[ja.length()];
                LngArray=new String[ja.length()];
                RouteArray=new String[ja.length()];
                System.out.println(".............Ja.Length.........");
                System.out.println(ja.length());
                NewgeoCordinates=new ArrayList<>();
                OldgeoCordinates=new ArrayList<>();
                geoCordinates=new HashMap<>();
                Extract=new HashMap<>();

                Testing=new ArrayList<>();

                if(ja.isNull(0)){
                    Log.e("TAG","NULL DATA");
                }else{
                    for (int i = 0; i < ja.length(); i++) {
                        String[] testArray = new String[3];
                        JSONObject jo = ja.getJSONObject(i);

                        LatArray[i] = jo.getString("geolat");
                        LngArray[i] = jo.getString("geolng");
                        RouteArray[i] = jo.getString("Drivers_route");

                        System.out.println("-----------data" + RouteArray[i] + "-----------");
                        System.out.println("location(" + i + ")=" + LatArray[i] + "," + LngArray[i]);
                        testArray[0] = RouteArray[i];
                        testArray[1] = LatArray[i];
                        testArray[2] = LngArray[i];
                        System.out.println("-----------printing testArray" + i + "-----------");
                        System.out.println("testArray(" + i + ")=" + testArray[0]);

                        Testing.add(testArray);


                    }
                }

            } catch (MalformedURLException e) {

                Log.e("URLexcept","Not Connected");
            } catch (IOException e) {

                Log.e("IOexcep","Not Connected");

            } catch (JSONException e) {
                Log.e("JSONexcep","JSON Error");
            }
            return Testing;
        }

        @Override
        protected void onPostExecute(List<String[]> geoMap) {

            listRoutes=new ArrayList<>();
            NewLatLng=new ArrayList<>();
            LatLng[] newlatlng=null;
            if(!(geoMap.size()==0)) {
                newlatlng = new LatLng[geoMap.size()];
            }

            String[] list;
            LatLng temp;
            Double lat;
            Double lng;
            int i;

            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //mMap.clear();

            if(!(geoMap.size()==0)) {

                for (i = 0; i < geoMap.size(); i++) {
                    list = geoMap.get(i);
                    newlatlng[i] = new LatLng(Double.parseDouble(list[1]), Double.parseDouble(list[2]));
                    listRoutes.add(list[0]);
                    System.out.println(".............Here is my data.........");
                    System.out.println("Route " + i + "=" + list[0]);
                    System.out.println("Real Test newlatlng " + i + "=" + newlatlng[i]);

                    index = new LatLng((double) i, 0);
                    new getDurationForRoute().execute(oldlatLng[i], currentLocation, newlatlng[i], index);
                    System.out.println("oldlatlng " + i + "=" + oldlatLng[i]);
                    System.out.println("newlatlng " + i + "=" + newlatlng[i]);
                    temp = newlatlng[i];
                    lat = temp.latitude;
                    lng = temp.longitude;
                    oldlatLng[i] = new LatLng(lat, lng);

                }
            }


        }

    }

    private void animateMarker(final LatLng destination, final Marker marker) {

        if (marker != null) {

            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = destination;

            final float startRotation = marker.getRotation();
            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(5000); // duration 3 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        marker.setPosition(newPosition);
                        marker.setAnchor(0.5f, 0.5f);
                        /*mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                .target(newPosition)
                                .zoom(18f)
                                .build()));*/

                        marker.setRotation(getBearing(startPosition, destination));
                    } catch (Exception ex) {
                        Log.e("animationExcep","Animation error");
                    }
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);


                }
            });
            valueAnimator.start();
        }
    }

    private interface LatLngInterpolator {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {


                double lng=fraction * b.longitude + (1-fraction)*a.longitude;
                double lat=fraction * b.latitude + (1-fraction)*a.latitude;
                return new LatLng(lat, lng);
            }
        }
    }

    //Method for finding bearing between two points
    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    @SuppressLint("StaticFieldLeak")
    class getDurationForRoute extends AsyncTask<LatLng, Void, String[]> {
        @Override
        protected String[] doInBackground(LatLng...latLngs) {
            //We need a context to access the API
            String[] Coordinates=new String[6];


            Location loc=new Location("");
            loc.setLatitude(latLngs[0].latitude);
            loc.setLongitude(latLngs[0].longitude);

            Location destination=new Location("");
            destination.setLatitude(latLngs[1].latitude);
            destination.setLongitude(latLngs[1].longitude);



                Double temp=latLngs[2].latitude;
                Coordinates[1]=temp.toString();
                temp=latLngs[2].longitude;
                Coordinates[2]=temp.toString();
                temp=latLngs[3].latitude;
                Coordinates[3]=temp.toString();
                temp=latLngs[0].latitude;
                Coordinates[4]=temp.toString();
                temp=latLngs[0].longitude;
                Coordinates[5]=temp.toString();
                Coordinates[0]= Integer.toString(getArrivalTime(loc,destination,Coordinates[3]));
                return Coordinates;


        }

        @Override
        protected void onPostExecute(String[] Coordinates) {

            Double temp = Double.parseDouble(Coordinates[3]);
            int i;
            i = temp.intValue();
            //Marker mCurrent=mMap.addMarker(new MarkerOptions().position(latlng).title("Arrival Time of Shuttle: " + Coordinates[0]).icon(BitmapDescriptorFactory.fromResource(R.mipmap.bus_demo_yellow)));
            LatLng end_location = new LatLng(Double.parseDouble(Coordinates[1]), Double.parseDouble(Coordinates[2]));
            LatLng start_location = new LatLng(Double.parseDouble(Coordinates[4]), Double.parseDouble(Coordinates[5]));
            //animateMarker(end_location,mCurrent);


            Location loc = new Location("");
            loc.setLatitude(end_location.latitude);
            loc.setLongitude(end_location.longitude);

            float distance = mLastlocation.distanceTo(loc);


            System.out.println("-----------------Displaying data " + listRoutes.get(i) + "---------------------");
            System.out.println("The distance is=" + distance);
            System.out.println("The time is=" + Coordinates[0]);
            int input;
            String route;
            input = routeSpinner.getSelectedItemPosition();
            switch (input) {
                case 0:
                    route = "C";
                    break;
                case 1:
                    route = "B";
                    break;
                case 2:
                    route = "A";
                    break;
                default:
                    route = "B";
                    break;
            }




            if ((listRoutes.get(i)).equals("C")) {
                if (route.equals("C")) {
                    System.out.println("yellow icon displayed");
                    if (set[i] == 0) {

                        set[i] = 1;
                        if((Double.parseDouble(Coordinates[0]))>=1) {
                            mk[i] = mMap.addMarker(new MarkerOptions().position(start_location).title("Arrival Time of Shuttle: " + Coordinates[0] + "mins").icon(BitmapDescriptorFactory.fromResource(R.mipmap.commercial_bus)));
                        }else{
                            mk[i] = mMap.addMarker(new MarkerOptions().position(start_location).title("Arrival Time of Shuttle: Shuttle is close by").icon(BitmapDescriptorFactory.fromResource(R.mipmap.commercial_bus)));
                        }
                        if (!start_location.equals(end_location))
                            animateMarker(end_location, mk[i]);
                    } else if (set[i] == 1) {
                        mk[i].setPosition(start_location);
                        mk[i].setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.commercial_bus));
                        if (!start_location.equals(end_location))
                            animateMarker(end_location, mk[i]);
                        if((Double.parseDouble(Coordinates[0]))>=1) {
                            mk[i].setTitle("Arrival Time of Shuttle: " + Coordinates[0] + "mins");
                        }else{
                            mk[i].setTitle("Arrival Time of Shuttle: Shuttle is close by");
                        }
                    }
                }else if(set[i]==1){
                    mk[i].remove();
                    set[i]=0;
                }
            }
            if ((listRoutes.get(i)).equals("B")) {
                if (route.equals("B")) {
                    System.out.println("blue icon displayed");
                    if (set[i] == 0) {

                        set[i] = 1;
                        if((Double.parseDouble(Coordinates[0]))>=1) {
                            mk[i] = mMap.addMarker(new MarkerOptions().position(start_location).title("Arrival Time of Shuttle: " + Coordinates[0] + "mins").icon(BitmapDescriptorFactory.fromResource(R.mipmap.brunei_bus)));
                        }else{
                            mk[i] = mMap.addMarker(new MarkerOptions().position(start_location).title("Arrival Time of Shuttle: Shuttle is close by").icon(BitmapDescriptorFactory.fromResource(R.mipmap.brunei_bus)));
                        }
                        if (!start_location.equals(end_location))
                            animateMarker(end_location, mk[i]);
                    } else if (set[i] == 1) {
                        mk[i].setPosition(start_location);
                        mk[i].setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.brunei_bus));
                        if (!start_location.equals(end_location))
                            animateMarker(end_location, mk[i]);
                        if ((Double.parseDouble(Coordinates[0])) >= 1) {
                            mk[i].setTitle("Arrival Time of Shuttle: " + Coordinates[0] + "mins");
                        } else {
                            mk[i].setTitle("Arrival Time of Shuttle: Shuttle is close by");
                        }
                    }
                }else if(set[i]==1){
                    mk[i].remove();
                    set[i]=0;
                }
            }
            if ((listRoutes.get(i)).equals("A")) {
                if (route.equals("A")) {
                    System.out.println("red icon displayed");
                    if (set[i] == 0) {

                        set[i] = 1;
                        if((Double.parseDouble(Coordinates[0]))>=1) {
                            mk[i] = mMap.addMarker(new MarkerOptions().position(start_location).title("Arrival Time of Shuttle: " + Coordinates[0] + "mins").icon(BitmapDescriptorFactory.fromResource(R.mipmap.bus_gaza)));
                        }else{
                            mk[i] = mMap.addMarker(new MarkerOptions().position(start_location).title("Arrival Time of Shuttle: Shuttle is close by").icon(BitmapDescriptorFactory.fromResource(R.mipmap.bus_gaza)));
                        }
                        if (!start_location.equals(end_location))
                            animateMarker(end_location, mk[i]);
                    } else if (set[i] == 1) {
                        mk[i].setPosition(start_location);
                        mk[i].setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.bus_gaza));
                        if (!start_location.equals(end_location))
                            animateMarker(end_location, mk[i]);
                        if((Double.parseDouble(Coordinates[0]))>=1) {
                            mk[i].setTitle("Arrival Time of Shuttle: " + Coordinates[0] + "mins");
                        }else{
                            mk[i].setTitle("Arrival Time of Shuttle: Shuttle is close by");
                        }
                    }
                }else if(set[i]==1){
                    mk[i].remove();
                    set[i]=0;
                }
            }

        }
    }


    public boolean isNetworkAvailable(){
        boolean status=false;
        ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo=cm.getActiveNetworkInfo();
        return activeNetworkInfo !=null;
    }
@SuppressLint("StaticFieldLeak")
class isInternetAccessibleThread extends AsyncTask<Void,Void,Boolean>{
    @Override
    protected Boolean doInBackground(Void... voids) {
        if(isNetworkAvailable()){
            try {
                HttpURLConnection urlc=(HttpURLConnection)(new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent","Test");
                urlc.setRequestProperty("Connection","close");
                urlc.setConnectTimeout(1500);
                urlc.connect();

                if(urlc.getResponseCode()==200){
                    Snackbar snackbar=Snackbar.make(getWindow().getDecorView().getRootView(),"User Online",Snackbar.LENGTH_SHORT);
                    View view=snackbar.getView();
                    view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.green));
                    return true;
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("IOerror","I/O exception");
            }
        }else{
            Log.e("NetState","Internet not Available!");
        }
        Snackbar snackbar=Snackbar.make(getWindow().getDecorView().getRootView(),"No Connection",Snackbar.LENGTH_SHORT);
        //View view=snackbar.getView();
        //view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.red));
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {

       if(aBoolean.equals(true)) {

           new FetchGeo().execute();
       }
    }
}


public int getArrivalTime(Location bus,Location destination,String index){

    Double Dtemp=Double.parseDouble(index);
    int i;
    int size=6;
    i=Dtemp.intValue();
    Double Path[][]=null;
    Location loc=new Location("");
    Location loop_location=new Location("");
    int end=0;
    int aTime=0;
    int begin=0;
if(listRoutes.size()!=0) {
    switch (listRoutes.get(i)) {
        case "C":
            Path = new Double[8][2];
            size = 8;

            Path[0][0] = 6.682836;
            Path[0][1] = -1.576970;
            Path[1][0] = 6.682376;
            Path[1][1] = -1.576931;
            Path[2][0] = 6.681777;
            Path[2][1] = -1.575653;
            Path[3][0] = 6.679165;
            Path[3][1] = -1.572579;
            Path[4][0] = 6.678509;
            Path[4][1] = -1.570892;
            Path[5][0] = 6.675119;
            Path[5][1] = -1.570731;
            Path[6][0] = 6.6748622;
            Path[6][1] = -1.5673230;
            Path[7][0] = 6.6682733;
            Path[7][1] = -1.5670126;
            break;
        case "B":
            Path = new Double[6][2];
            size = 6;

            Path[0][0] = 6.6703300;
            Path[0][1] = -1.5743219;
            Path[1][0] = 6.6725768;
            Path[1][1] = -1.5734388;
            Path[2][0] = 6.6751033;
            Path[2][1] = -1.5722526;
            Path[3][0] = 6.675119;
            Path[3][1] = -1.570731;
            Path[4][0] = 6.6748622;
            Path[4][1] = -1.5673230;
            Path[5][0] = 6.6682733;
            Path[5][1] = -1.5670126;

            break;
        case "A":
            Path = new Double[9][2];
            size = 9;

            Path[0][0] = 6.687655;
            Path[0][1] = -1.556916;
            Path[1][0] = 6.686565;
            Path[1][1] = -1.557055;
            Path[2][0] = 6.684714;
            Path[2][1] = -1.558249;
            Path[3][0] = 6.685548;
            Path[3][1] = -1.560698;
            Path[4][0] = 6.681832;
            Path[4][1] = -1.562225;
            Path[5][0] = 6.680689;
            Path[5][1] = -1.564802;
            Path[6][0] = 6.677267;
            Path[6][1] = -1.567183;
            Path[7][0] = 6.6748622;
            Path[7][1] = -1.5673230;
            Path[8][0] = 6.6682733;
            Path[8][1] = -1.5670126;
            break;
        default:

            break;
    }

    //checking if bus is d closest
    for (int x = 1; x < size; x++) {
        assert Path != null;
        loop_location.setLatitude(Path[x][0]);
        loop_location.setLongitude(Path[x][1]);

        if (destination.distanceTo(loop_location) < destination.distanceTo(bus)) {
            break;
        }
        if (x == size - 1 && (destination.distanceTo(bus) < destination.distanceTo(loop_location))) {
            aTime= 0;
            return aTime;
        }
    }
    //if bus not closest;checks for which saved location is closer to bus
    loc.setLatitude(Path[0][0]);
    loc.setLongitude(Path[0][1]);
    for (int x = 1; x < size; x++) {
        loop_location = new Location("");
        loop_location.setLatitude(Path[x][0]);
        loop_location.setLongitude(Path[x][1]);

        if (bus.distanceTo(loop_location) < bus.distanceTo(loc)) {
            begin = x;
            loc = new Location("");
            loc.setLatitude(loop_location.getLatitude());
            loc.setLongitude(loop_location.getLongitude());

        }
    }
    aTime = aTime + 1;
    //checks for which saved location is closest to destination
    loc = new Location("");
    loc.setLatitude(Path[0][0]);
    loc.setLongitude(Path[0][1]);
    for (int x = 1; x < size; x++) {
        loop_location = new Location("");
        loop_location.setLatitude(Path[x][0]);
        loop_location.setLongitude(Path[x][1]);

        if (destination.distanceTo(loop_location) < destination.distanceTo(loc)) {
            end = x;
            loc = new Location("");
            loc.setLatitude(loop_location.getLatitude());
            loc.setLongitude(loop_location.getLongitude());

        }
    }
    //Assuming each distance to be 1 min apart;loop sum through begin to end
    if (begin < end) {
        for (int x = begin; x < end + 1; x++) {
            aTime++;
        }
    }
    if (begin > end) {
        for (int x = begin; x > end - 1; x--) {
            aTime++;
        }
    }
}

    return aTime;
}

}