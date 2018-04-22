package com.ishuttle.kodah;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location mLastlocation;
    LocationRequest mLocationRequest;
    InputStream is=null;
    String line=null;
    String result=null;
    String[] SPINNERLIST = {"C-Commercial to Business School", "B-Brunei to Business School", "A-Gaza to Business School"};
    String[] NewLatArray,NewLngArray,OldLatArray,OldLngArray,RouteArray;
    List<LatLng> OldgeoCordinates;
    List<Map<String,LatLng>> NewgeoCordinates;
    Map<List<LatLng>,List<Map<String,LatLng>>> geoCordinates;
    Map<String,LatLng> RouteCordinates;
    List<LatLng> NewLatLng,OldLatLng;

    LatLng latlng,currentLocation,newlatLng,index;
    Location destination;
    double newlat,newlng;
    //private static final String API_KEY="AIzaSyC3hHJA1icyVAYBpTBvDjEeU6JuAldzF-o";
    private static final String API_KEY="AIzaSyCixRvOByU6urV03272IIPC6X92TquLtB8";
    String driveRoutes ;
    List<String> listRoutes,Iroutes;
    Spinner betterSpinner;
    //List<Location> Brunei_path=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item, SPINNERLIST);
        betterSpinner = (Spinner) findViewById(R.id.map_spinner);
        betterSpinner.setAdapter(arrayAdapter);

        StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder().permitNetwork().build()));

        //Entering values for brunei path


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
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));

        new isInternetAccessibleThread().execute();

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
        //LatLng sydney = new LatLng(6.6705943,-1.5741065);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney").icon(BitmapDescriptorFactory.fromResource(R.mipmap.bus_demo2)));
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @SuppressLint("StaticFieldLeak")
    class FetchGeo extends AsyncTask<Void,Void,Map<List<LatLng>,List<Map<String,LatLng>>>>{
        @Override
        protected Map<List<LatLng>,List<Map<String,LatLng>>> doInBackground(Void... voids) {
            URL url;

            try {
                url = new URL("https://kodahinc.000webhostapp.com/getlocation.php");

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

                JSONArray ja = new JSONArray(result);

                NewLatArray = new String[ja.length()];
                NewLngArray = new String[ja.length()];
                OldLatArray=new String[ja.length()];
                OldLngArray=new String[ja.length()];
                RouteArray=new String[ja.length()];
                System.out.println(".............Ja.Length.........");
                System.out.println(ja.length());
                NewgeoCordinates=new ArrayList<>();
                OldgeoCordinates=new ArrayList<>();
                geoCordinates=new HashMap<>();
                RouteCordinates=new HashMap<>();

                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);

                    OldLatArray[i] = jo.getString("geolat");
                    OldLngArray[i] = jo.getString("geolng");
                    NewLatArray[i] = jo.getString("NewGeolat");
                    NewLngArray[i] = jo.getString("NewGeolng");
                    RouteArray[i] = jo.getString("Drivers_route");
                    System.out.println(".............Here is my data.........");
                    System.out.println(RouteArray[i]);

                    OldgeoCordinates.add(new LatLng(Double.parseDouble(OldLatArray[i]), Double.parseDouble(OldLngArray[i])));
                    RouteCordinates.put(RouteArray[i],new LatLng(Double.parseDouble(NewLatArray[i]), Double.parseDouble(NewLngArray[i])));
                    NewgeoCordinates.add(RouteCordinates);

                    geoCordinates.put(OldgeoCordinates,NewgeoCordinates);


                }

            } catch (MalformedURLException e) {

                Log.e("URLexcept","Not Connected");
            } catch (IOException e) {

                Log.e("IOexcep","Not Connected");

            } catch (JSONException e) {
                Log.e("JSONexcep","JSON Error");
            }
            return geoCordinates;
        }

        @Override
        protected void onPostExecute(Map<List<LatLng>,List<Map<String,LatLng>>> geoMap) {
            //   OldLatLng.clear();NewgeoCordinates.clear();listRoutes.clear();NewLatLng.clear();
            listRoutes=new ArrayList<>();
            NewLatLng=new ArrayList<>();
            Iroutes=new ArrayList<>();
            Marker mCurrent;
            int y;
            for (Map.Entry<List<LatLng>,List<Map<String,LatLng>>> entry: geoMap.entrySet()) {

                OldLatLng=entry.getKey();
                NewgeoCordinates=entry.getValue();
                for(y=0;y<NewgeoCordinates.size();y++) {
                    for (Map.Entry<String, LatLng> entry1 : NewgeoCordinates.get(y).entrySet()) {
                        listRoutes.add(entry1.getKey());
                        NewLatLng.add(entry1.getValue());

                    }
                }
            }
            mMap.clear();
            for(int i=0;i<OldLatLng.size();i++){

                     latlng=OldLatLng.get(i);
                     index=new LatLng((double)i,0);
                     new getDurationForRoute().execute(latlng,currentLocation,NewLatLng.get(i),index);

                /*switch(listRoutes.get(i)){
                    case "C":
                        mCurrent=mMap.addMarker(new MarkerOptions().position(latlng).title("Arrival Time of Shuttle: " + i).icon(BitmapDescriptorFactory.fromResource(R.mipmap.commercial_bus)));
                        animateMarker(NewLatLng.get(i),mCurrent);
                        break;
                    case "B":
                        mCurrent=mMap.addMarker(new MarkerOptions().position(latlng).title("Arrival Time of Shuttle: " + i).icon(BitmapDescriptorFactory.fromResource(R.mipmap.brunei_bus)));
                        animateMarker(NewLatLng.get(i),mCurrent);
                        break;
                    case "A":
                        mCurrent=mMap.addMarker(new MarkerOptions().position(latlng).title("Arrival Time of Shuttle: " + i).icon(BitmapDescriptorFactory.fromResource(R.mipmap.bus_gaza)));
                        animateMarker(NewLatLng.get(i),mCurrent);
                        break;
                    default:
                        mCurrent=mMap.addMarker(new MarkerOptions().position(latlng).title("Arrival Time of Shuttle: " + i).icon(BitmapDescriptorFactory.fromResource(R.mipmap.commercial_bus)));
                        animateMarker(NewLatLng.get(i),mCurrent);
                        break;
                }*/

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
            valueAnimator.setDuration(3000); // duration 3 second
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
            /*GeoApiContext geoApiContext=new GeoApiContext.Builder()
                    .apiKey(API_KEY)
                    .build();

            //Perform the actual request
            DirectionsResult directionsResult;


            Date currentTime= Calendar.getInstance().getTime();
            DateTime now= new DateTime(currentTime);

            try {
                directionsResult = DirectionsApi.newRequest(geoApiContext)
                        .mode(TravelMode.DRIVING)
                        .origin(String.valueOf(latLngs[0]))
                        .destination(String.valueOf(latLngs[1]))
                        .arrivalTime(now)
                        .transitMode(TransitMode.BUS)
                        //.trafficModel(TrafficModel.BEST_GUESS)
                        .await();

                //-Parse the result
                DirectionsRoute route=directionsResult.routes[0];
                DirectionsLeg leg=route.legs[0];
                Duration duration = leg.duration;
                //aTime= ;
                System.out.println(".............Here is my data.........");
                System.out.println(duration.humanReadable);
                */

                Coordinates[0]= (getTime(loc,destination)).toString();
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
                return Coordinates;

            /*} catch (ApiException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            //return null;
        }

        @Override
        protected void onPostExecute(String[] Coordinates) {
            Marker mCurrent;
            Double temp=Double.parseDouble(Coordinates[3]);
            int i;
            i=temp.intValue();
            //Marker mCurrent=mMap.addMarker(new MarkerOptions().position(latlng).title("Arrival Time of Shuttle: " + Coordinates[0]).icon(BitmapDescriptorFactory.fromResource(R.mipmap.bus_demo_yellow)));
            LatLng end_location=new LatLng(Double.parseDouble(Coordinates[1]),Double.parseDouble(Coordinates[2]));
            LatLng start_location=new LatLng(Double.parseDouble(Coordinates[4]),Double.parseDouble(Coordinates[5]));
            //animateMarker(end_location,mCurrent);


            Location loc=new Location("");
            loc.setLatitude(end_location.latitude);
            loc.setLongitude(end_location.longitude);

            //TODO:Try to use road api and polyline to implement car road movement
            float distance=mLastlocation.distanceTo(loc);


            System.out.println("-----------------Displaying data " + listRoutes.get(i) + "---------------------");
            System.out.println("The distance is=" + distance );
            System.out.println("The time is=" + Coordinates[0] );




            switch(listRoutes.get(i)){
                case "C":
                    mCurrent=mMap.addMarker(new MarkerOptions().position(start_location).title("Arrival Time of Shuttle: " + Coordinates[0]).icon(BitmapDescriptorFactory.fromResource(R.mipmap.commercial_bus)));
                    animateMarker(end_location,mCurrent);
                    break;
                case "B":
                    mCurrent=mMap.addMarker(new MarkerOptions().position(start_location).title("Arrival Time of Shuttle: " + Coordinates[0]).icon(BitmapDescriptorFactory.fromResource(R.mipmap.brunei_bus)));
                    animateMarker(end_location,mCurrent);
                    break;
                case "A":
                    mCurrent=mMap.addMarker(new MarkerOptions().position(start_location).title("Arrival Time of Shuttle: " + Coordinates[0]).icon(BitmapDescriptorFactory.fromResource(R.mipmap.bus_gaza)));
                    animateMarker(end_location,mCurrent);
                    break;
                default:
                    mCurrent=mMap.addMarker(new MarkerOptions().position(start_location).title("Arrival Time of Shuttle: " + Coordinates[0]).icon(BitmapDescriptorFactory.fromResource(R.mipmap.commercial_bus)));
                    animateMarker(end_location,mCurrent);
                    break;
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

                return (urlc.getResponseCode()==200);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("IOerror","I/O exception");
            }
        }else{
            Log.e("NetState","Internet not Available!");
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {

       if(aBoolean.equals(true))
        new FetchGeo().execute();
    }
}

public Double getTime(Location start,Location destination){
        //float dist;
    Location bus=new Location("");
    bus=start;
    //Brunei_path=new ArrayList<>(6);

    Double brunei_path[][]=new Double[6][2];

    Location Itemp=new Location("");
    /*Itemp.setLatitude(6.6703300);
    Itemp.setLongitude(-1.5743219);
    Brunei_path.add(Itemp);
    System.out.println(Brunei_path.get(0));
    Itemp.setLatitude(6.6725768);
    Itemp.setLongitude(-1.5734388);
    Brunei_path.add(Itemp);
    System.out.println(Brunei_path.get(1));
    Itemp.setLatitude(6.6751033);
    Itemp.setLongitude(-1.5722526);
    Brunei_path.add(Itemp);
    System.out.println(Brunei_path.get(2));
    Itemp.setLatitude(6.675119);
    Itemp.setLongitude(-1.570731);
    Brunei_path.add(Itemp);
    System.out.println(Brunei_path.get(3));
    Itemp.setLatitude(6.6748622);
    Itemp.setLongitude(-1.5673230);
    Brunei_path.add(Itemp);
    System.out.println("test="+Brunei_path.get(0));
    System.out.println(Brunei_path.get(4));
    Itemp.setLatitude(6.6682733);
    Itemp.setLongitude(-1.5670126);
    Brunei_path.add(Itemp);
    System.out.println(Brunei_path.get(5));
    System.out.println("test="+Brunei_path.get(0));*/

    brunei_path[0][0]= 6.6703300;
    brunei_path[0][1]= -1.5743219;
    System.out.println("1="+ brunei_path[0][0] + " & " + brunei_path[0][1]);
    brunei_path[1][0] = 6.6725768;
    brunei_path[1][1]= -1.5734388;
    System.out.println("2="+ brunei_path[1][0] + " & " + brunei_path[1][1]);
    brunei_path[2][0]=6.6751033;
    brunei_path[2][1]=-1.5722526;
    System.out.println("3="+ brunei_path[2][0] + " & " + brunei_path[2][1]);
    brunei_path[3][0]=6.675119;
    brunei_path[3][1]=-1.570731;
    System.out.println("4="+ brunei_path[3][0] + " & " + brunei_path[3][1]);
    brunei_path[4][0]=6.6748622;
    brunei_path[4][1]=-1.5673230;
    System.out.println("5="+ brunei_path[4][0] + " & " + brunei_path[4][1]);
    System.out.println("test="+ brunei_path[1][0] + " & " + brunei_path[1][1]);
    brunei_path[5][0]=6.6682733;
    brunei_path[5][1]=-1.5670126;
    System.out.println("6="+ brunei_path[5][0] + " & " + brunei_path[5][1]);
    System.out.println("test="+ brunei_path[0][0] + " & " + brunei_path[0][1]);

    int z=1;
    Double aTime=0.0;
    Double Ftime=0.0;
    int x,y;
    int temp=0;
//    System.out.println("0="+Brunei_path.get(0));
//    System.out.println("1="+Brunei_path.get(1));

    int check=0;
    Location loc;
    Location loop_location;
    //int size=5;
    int confirm=0;
    while(z==1){
        loc=new Location("");

        //dist=bus.distanceTo(destination);
         for(x=0;x< 6;x++){
             confirm=0;
             loop_location=new Location("");
             loop_location.setLatitude(brunei_path[x][0]);
             loop_location.setLongitude(brunei_path[x][1]);

            if(destination.distanceTo(loop_location) < bus.distanceTo(destination)){
                confirm=3;
                check=3;
                loc.setLatitude(brunei_path[0][0]);
                loc.setLongitude(brunei_path[0][1]);
                for (y=1;y< 6;y++) {
                    loop_location=new Location("");
                    loop_location.setLatitude(brunei_path[y][0]);
                    loop_location.setLongitude(brunei_path[y][1]);
                    System.out.println("y="+ loop_location);
                    System.out.println("bus="+ bus);
                    System.out.println("loc="+ loc);
                    System.out.println("bus.distanceto(loc)="+ bus.distanceTo(loc));
                    System.out.println("bus.distanceTo(Routes[y])="+ bus.distanceTo(loop_location));
                    if ((bus.distanceTo(loc) > bus.distanceTo(loop_location)) && ((bus.distanceTo(loop_location))!=0.0)) {
                        loc = loop_location;
                        //temp=y;
                        System.out.println("passed 1");
                    }

                    //break inner;
                }

                System.out.println("passed 2");
                float temp_dist=bus.distanceTo(loc);
                bus=new Location("");

                Ftime = (temp_dist / 536.448);
                aTime=(aTime + Ftime)+1;
                System.out.println("Time= " + aTime);
                System.out.println("dist= " + temp_dist);
                bus.setLatitude(loc.getLatitude());
                bus.setLongitude(loc.getLongitude());
                System.out.println("new bus = "+ bus);

                //x=0;
                //size=temp;

            }
            if(confirm==0 && x==5){
                if(check==0){
                    float temp_dist=bus.distanceTo(destination);
                    System.out.println();
                    Ftime = (temp_dist / 536.448);
                    aTime=(aTime + Ftime)+1;
                    System.out.println("Time2= " + aTime);
                    System.out.println("dist2= " + temp_dist);
                }
                System.out.println("end case");
                z=0;
                break ;
            }

        }
    }
    return aTime;
}

}