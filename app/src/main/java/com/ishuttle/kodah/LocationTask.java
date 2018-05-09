package com.ishuttle.kodah;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.Result;
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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Dinho on 4/24/2018.
 */

public class LocationTask extends AsyncTask<Void,Void,LatLng[]> {

    Context ctx;

    public interface AsyncResponse{
        void processFinish(LatLng[] output);
    }

    public AsyncResponse delegate = null;

    public LocationTask(AsyncResponse delegate){
        this.delegate = delegate;
    }

    InputStream is=null;
    String line=null;
    String result=null;
    LatLng[] latlngArray;

    @Override
    protected LatLng[] doInBackground(Void... voids) {

        URL url;

        try {
            url = new URL("http://wigsbydebs.xyz/getlocation.php");

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

            String[] LatArray=new String[ja.length()];
            String[] LngArray=new String[ja.length()];
            latlngArray=new LatLng[ja.length()];
            System.out.println(".............Ja.Length.........");
            System.out.println(ja.length());

            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);

                LatArray[i] = jo.getString("geolat");
                LngArray[i] = jo.getString("geolng");
                latlngArray[i]=new LatLng(Double.parseDouble(LatArray[i]),Double.parseDouble(LngArray[i]));



            }

        } catch (MalformedURLException e) {

            Log.e("URLexcept","Not Connected");
        } catch (IOException e) {

            Log.e("IOexcep","Not Connected");

        } catch (JSONException e) {
            Log.e("JSONexcep","JSON Error");
        }
        return latlngArray;

    }

    @Override
    protected void onPostExecute(LatLng[] result) {
        delegate.processFinish(result);
    }
}
