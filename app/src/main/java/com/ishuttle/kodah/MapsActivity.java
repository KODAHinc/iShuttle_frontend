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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
    String[] NewLatArray,NewLngArray,LatArray,LngArray,RouteArray;
    List<LatLng> OldgeoCordinates;
    List<Map<String,LatLng>> NewgeoCordinates;
    Map<String,LatLng> geoCordinates;
    Map<String,LatLng> RouteCordinates;
    List<LatLng> NewLatLng,OldLatLng;
    String tempRoute;

    LatLng latlng,currentLocation,index;
    LatLng[] oldlatLng=null;
    Location destination;
    double newlat,newlng;
    private static final String API_KEY="AIzaSyCixRvOByU6urV03272IIPC6X92TquLtB8";
    private static final long TURN_ANIMATION_DURATION=3000;
    private static final long MOVE_ANIMATION_DURATION=3000;
    String driveRoutes ;
    List<String> listRoutes,Iroutes;
    Spinner routeSpinner;
    Bitmap mMarkerIcon;
    private List<LatLng> mPathPolygonPoints;
    int mIndexCurrentPoint=0;
    Marker mCurrent;

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
        routeSpinner = (Spinner) findViewById(R.id.map_spinner);
        routeSpinner.setAdapter(arrayAdapter);

        StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder().permitNetwork().build()));




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

        if(oldlatLng!=null)
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
    class FetchGeo extends AsyncTask<Void,Void,Map<String,LatLng>>{
        @Override
        protected Map<String,LatLng> doInBackground(Void... voids) {
            URL url;

            try {
                url = new URL("https://kodahinc.000webhostapp.com/getlocation.php");

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


                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);

                    LatArray[i] = jo.getString("geolat");
                    LngArray[i] = jo.getString("geolng");
                    RouteArray[i] = jo.getString("Drivers_route");


                    //OldgeoCordinates.add(new LatLng(Double.parseDouble(LatArray[i]), Double.parseDouble(LngArray[i])));
                    geoCordinates.put(RouteArray[i],new LatLng(Double.parseDouble(LatArray[i]),Double.parseDouble(LngArray[i])));


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
        protected void onPostExecute(Map<String,LatLng> geoMap) {

            listRoutes=new ArrayList<>();
            NewLatLng=new ArrayList<>();
            LatLng newlatlng;
            int i=0;

            for (Map.Entry<String,LatLng> entry: geoMap.entrySet()) {
                newlatlng=entry.getValue();
                //newlatlng=new LatLng(6.6725768,-1.5734388);

                System.out.println(".............Here is my data.........");
                System.out.println("Route "+ i + "=" + entry.getKey());
                listRoutes.add(entry.getKey());
                index=new LatLng((double)i,0);
                new getDurationForRoute().execute(oldlatLng[i],currentLocation,newlatlng,index);
                System.out.println("oldlatlng "+ i + "=" + oldlatLng[i]);
                System.out.println("newlatlng "+ i + "=" + newlatlng);
                oldlatLng[i]=newlatlng;
                i++;
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
                Coordinates[0]= (getTime(loc,destination,Coordinates[3])).toString();
                return Coordinates;


        }

        @Override
        protected void onPostExecute(String[] Coordinates) {

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
            int input;
            String route;
            input=routeSpinner.getSelectedItemPosition();
            switch(input){
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

            mMap.clear();
            switch(listRoutes.get(i)){
                case "C":
                    if(route.equals("C")){
                        System.out.println("icon displayed");
                    mCurrent=mMap.addMarker(new MarkerOptions().position(start_location).title("Arrival Time of Shuttle: " + Coordinates[0]).icon(BitmapDescriptorFactory.fromResource(R.mipmap.commercial_bus)));
                    //animateCarMove(mCurrent,start_location,end_location,3000,"C");
                        if(!start_location.equals(end_location))
                            animateMarker(end_location,mCurrent);

                    }
                    break;
                case "B":
                    if(route.equals("B")) {
                        System.out.println("icon displayed");
                        mCurrent = mMap.addMarker(new MarkerOptions().position(start_location).title("Arrival Time of Shuttle: " + Coordinates[0]).icon(BitmapDescriptorFactory.fromResource(R.mipmap.brunei_bus)));
                        //end_location=new LatLng(6.6725768,-1.5734388);
                        if(!start_location.equals(end_location))
                            animateMarker(end_location,mCurrent);
                        //animateCarMove(mCurrent, start_location, end_location, 3000, "B");
                    }
                    break;
                case "A":
                    if(route.equals("A")){
                        System.out.println("icon displayed");
                        mCurrent=mMap.addMarker(new MarkerOptions().position(start_location).title("Arrival Time of Shuttle: " + Coordinates[0]).icon(BitmapDescriptorFactory.fromResource(R.mipmap.bus_gaza)));
                        if(!start_location.equals(end_location))
                            animateMarker(end_location,mCurrent);
                        //animateCarMove(mCurrent,start_location,end_location,3000,"A");
                    }
                    break;
                default:
                    if(route.equals("C")){
                        System.out.println("icon displayed");
                    mCurrent=mMap.addMarker(new MarkerOptions().position(start_location).title("Arrival Time of Shuttle: " + Coordinates[0]).icon(BitmapDescriptorFactory.fromResource(R.mipmap.commercial_bus)));
                        if(!start_location.equals(end_location))
                            animateMarker(end_location,mCurrent);
                        //animateCarMove(mCurrent,start_location,end_location,3000,"C");
                    }
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

                if(urlc.getResponseCode()==200){
                    /*Snackbar snackbar=Snackbar.make(getWindow().getDecorView().getRootView(),"User Online",Snackbar.LENGTH_SHORT);
                    View view=snackbar.getView();
                    view.setBackgroundColor(ContextCompat.getColor(getAc));*/
                    return true;
                }

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

public Double getTime(Location start,Location destination,String index){

    Location bus;
    bus=start;

    Double Dtemp=Double.parseDouble(index);
    int i;
    i=Dtemp.intValue();
    Double Path[][]=null;
    int end=0;
    switch(listRoutes.get(i)){
        case "C":
            Path=new Double[8][2];
             end=8;

            Path[0][0]= 6.682836;
            Path[0][1]= -1.576970;
            Path[1][0]= 6.682376;
            Path[1][1]= -1.576931;
            Path[2][0]= 6.681777;
            Path[2][1]= -1.575653;
            Path[3][0] = 6.679165;
            Path[3][1]= -1.572579;
            Path[4][0]=6.678509;
            Path[4][1]=-1.570892;
            Path[5][0]=6.675119;
            Path[5][1]=-1.570731;
            Path[6][0]=6.6748622;
            Path[6][1]=-1.5673230;
            Path[7][0]=6.6682733;
            Path[7][1]=-1.5670126;
            break;
        case "B":
            Path=new Double[6][2];
            end =6;

            Path[0][0]= 6.6703300;
            Path[0][1]= -1.5743219;
            Path[1][0] = 6.6725768;
            Path[1][1]= -1.5734388;
            Path[2][0]=6.6751033;
            Path[2][1]=-1.5722526;
            Path[3][0]=6.675119;
            Path[3][1]=-1.570731;
            Path[4][0]=6.6748622;
            Path[4][1]=-1.5673230;
            Path[5][0]=6.6682733;
            Path[5][1]=-1.5670126;

            break;
        case "A":
            Path=new Double[9][2];
            end=9;

            Path[0][0]= 6.687655;
            Path[0][1]= -1.556916;
            Path[1][0]= 6.686565;
            Path[1][1]= -1.557055;
            Path[2][0]= 6.684714;
            Path[2][1]= -1.558249;
            Path[3][0]= 6.685548;
            Path[3][1]= -1.560698;
            Path[4][0] = 6.681832;
            Path[4][1]= -1.562225;
            Path[5][0]=6.680689;
            Path[5][1]=-1.564802;
            Path[6][0]=6.677267;
            Path[6][1]=-1.567183;
            Path[7][0]=6.6748622;
            Path[7][1]=-1.5673230;
            Path[8][0]=6.6682733;
            Path[8][1]=-1.5670126;
            break;
        default:

            break;
    }



    int z=1;
    Double aTime=0.0;
    Double Ftime=0.0;
    int x,y;
    int temp=0;


    int check=0;
    Location loc;
    Location loop_location,prev_loc=new Location(""),next_loc=new Location("");
    int begin=1;

    //int size=5;
    int confirm=0;
    while(z==1){
        loc=new Location("");

        //dist=bus.distanceTo(destination);
         for(x=0;x< 6;x++){
             confirm=0;
             loop_location=new Location("");
             loop_location.setLatitude(Path[x][0]);
             loop_location.setLongitude(Path[x][1]);

            if(destination.distanceTo(loop_location) < bus.distanceTo(destination)){
                confirm=3;
                check=3;
                loc.setLatitude(Path[begin-1][0]);
                loc.setLongitude(Path[begin-1][1]);
                for (y=begin;y< end;y++) {
                    loop_location=new Location("");
                    loop_location.setLatitude(Path[y][0]);
                    loop_location.setLongitude(Path[y][1]);
                    System.out.println("y="+ loop_location);
                    System.out.println("bus="+ bus);
                    System.out.println("loc="+ loc);
                    System.out.println("bus.distanceto(loc)="+ bus.distanceTo(loc));
                    System.out.println("bus.distanceTo(Routes[y])="+ bus.distanceTo(loop_location));
                    if ((bus.distanceTo(loc) > bus.distanceTo(loop_location)) && ((bus.distanceTo(loop_location))!=0.0)) {
                        loc = loop_location;
                        temp=y;
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
                if(temp!=0) {
                    prev_loc.setLatitude(Path[temp - 1][0]);
                    prev_loc.setLongitude(Path[temp - 1][1]);
                }

                next_loc.setLatitude(Path[temp+1][0]);
                next_loc.setLongitude(Path[temp+1][1]);

                if(prev_loc.distanceTo(destination)<next_loc.distanceTo(destination)){
                    end=temp+1;
                }
                else if(prev_loc.distanceTo(destination)>next_loc.distanceTo(destination)){
                    begin=temp;
                }

                x=0;
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


    private void animateCarMove(final Marker marker, final LatLng beginLatLng, final LatLng endLatLng, final long duration,String Route) {
        mIndexCurrentPoint=0;
        final Handler handler = new Handler();
        final long startTime = SystemClock.uptimeMillis();
        if(Route.equals("C"))
           mMarkerIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.commercial_bus);

        if(Route.equals("B"))
            mMarkerIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.brunei_bus);

        if(Route.equals("A"))
            mMarkerIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.bus_gaza);

        final Interpolator interpolator = new LinearInterpolator();

        // set car bearing for current part of path
        float angleDeg = (float)(180 * getAngle(beginLatLng, endLatLng) / Math.PI);
        Matrix matrix = new Matrix();
        matrix.postRotate(angleDeg);
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(mMarkerIcon, 0, 0, mMarkerIcon.getWidth(), mMarkerIcon.getHeight(), matrix, true)));

        handler.post(new Runnable() {
            @Override
            public void run() {
                // calculate phase of animation
                long elapsed = SystemClock.uptimeMillis() - startTime;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                // calculate new position for marker
                double lat = (endLatLng.latitude - beginLatLng.latitude) * t + beginLatLng.latitude;
                double lngDelta = endLatLng.longitude - beginLatLng.longitude;

                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * t + beginLatLng.longitude;

                marker.setPosition(new LatLng(lat, lng));

                // if not end of line segment of path
                if (t < 1.0) {
                    // call next marker position
                    handler.postDelayed(this, 16);
                } else {
                    // call turn animation

                    //nextTurnAnimation();
                }
            }
        });
    }

    private double getAngle(LatLng beginLatLng, LatLng endLatLng) {
        double f1 = Math.PI * beginLatLng.latitude / 180;
        double f2 = Math.PI * endLatLng.latitude / 180;
        double dl = Math.PI * (endLatLng.longitude - beginLatLng.longitude) / 180;
        return Math.atan2(Math.sin(dl) * Math.cos(f2) , Math.cos(f1) * Math.sin(f2) - Math.sin(f1) * Math.cos(f2) * Math.cos(dl));
    }

    private void nextTurnAnimation() {
        mIndexCurrentPoint++;

        if (mIndexCurrentPoint < mPathPolygonPoints.size() - 1) {
            LatLng prevLatLng = mPathPolygonPoints.get(mIndexCurrentPoint - 1);
            LatLng currLatLng = mPathPolygonPoints.get(mIndexCurrentPoint);
            LatLng nextLatLng = mPathPolygonPoints.get(mIndexCurrentPoint + 1);

            float beginAngle = (float)(180 * getAngle(prevLatLng, currLatLng) / Math.PI);
            float endAngle = (float)(180 * getAngle(currLatLng, nextLatLng) / Math.PI);

            animateCarTurn(mCurrent, beginAngle, endAngle, TURN_ANIMATION_DURATION);
        }
    }

    private void animateCarTurn(final Marker marker, final float startAngle, final float endAngle, final long duration) {
        final Handler handler = new Handler();
        final long startTime = SystemClock.uptimeMillis();
        final Interpolator interpolator = new LinearInterpolator();

        final float dAndgle = endAngle - startAngle;

        Matrix matrix = new Matrix();
        matrix.postRotate(startAngle);
        Bitmap rotatedBitmap = Bitmap.createBitmap(mMarkerIcon, 0, 0, mMarkerIcon.getWidth(), mMarkerIcon.getHeight(), matrix, true);
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(rotatedBitmap));

        handler.post(new Runnable() {
            @Override
            public void run() {

                long elapsed = SystemClock.uptimeMillis() - startTime;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                Matrix m = new Matrix();
                m.postRotate(startAngle + dAndgle * t);
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(mMarkerIcon, 0, 0, mMarkerIcon.getWidth(), mMarkerIcon.getHeight(), m, true)));

                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                } else {
                    nextMoveAnimation();
                }
            }
        });
    }

    private void nextMoveAnimation() {
        if (mIndexCurrentPoint <  mPathPolygonPoints.size() - 1) {
            animateCarMove(mCurrent, mPathPolygonPoints.get(mIndexCurrentPoint), mPathPolygonPoints.get(mIndexCurrentPoint+1), MOVE_ANIMATION_DURATION,tempRoute);
        }
    }

}