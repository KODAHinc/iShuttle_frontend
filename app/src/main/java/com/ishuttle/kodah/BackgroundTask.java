package com.ishuttle.kodah;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


public class BackgroundTask extends AsyncTask<String,Void,String>{
    Context ctx;
    BackgroundTask(Context ctx){

        this.ctx=ctx;
    }
    @Override
    protected String doInBackground(String... params) {
        String reg_url="http://kodahinc.000webhostapp.com/register.php";
        String geo_url="http://kodahinc.000webhostapp.com/driverlocation.php";

        String method=params[0];
        if(method.equals("register")){
            String username=params[1];
            String password=params[2];
            try {
                URL url=new URL(reg_url);
                HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                OutputStream os=httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
                String data = URLEncoder.encode("username","UTF-8")+"="+URLEncoder.encode(username,"UTF-8")+"&"+
                        URLEncoder.encode("password","UTF-8")+"="+URLEncoder.encode(password,"UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                os.close();
                InputStream IS=httpURLConnection.getInputStream();
                IS.close();
                return "Registration success";

            } catch (MalformedURLException e) {
                Log.e("IOexcep","Not Connected");
            } catch (IOException e) {
                Log.e("IOexcep","Not Connected");
            }


        }
        if(method.equals("geostore")){

            String Id=params[1];
            String route=params[2];
            String lat_geo=params[3];
            String lng_geo=params[4];

            try {
                URL url=new URL(geo_url);
                HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                OutputStream os=httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
                String data = URLEncoder.encode("driver_id","UTF-8")+"="+URLEncoder.encode(Id,"UTF-8")+"&"+
                        URLEncoder.encode("drivers_route","UTF-8")+"="+URLEncoder.encode(route,"UTF-8")+"&"+
                        URLEncoder.encode("geolat","UTF-8")+"="+URLEncoder.encode(lat_geo,"UTF-8")+"&"+
                        URLEncoder.encode("geolng","UTF-8")+"="+URLEncoder.encode(lng_geo,"UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                os.close();
                InputStream IS=httpURLConnection.getInputStream();
                IS.close();


            } catch (MalformedURLException e) {
                Log.e("IOexcep","Not Connected");
            } catch (IOException e) {
                Log.e("IOexcep","Not Connected");
            }
        }

        return null;
    }


    @Override
    protected void onPostExecute(String result) {

        Toast.makeText(ctx,result,Toast.LENGTH_SHORT).show();
    }
}
