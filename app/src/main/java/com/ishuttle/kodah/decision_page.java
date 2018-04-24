package com.ishuttle.kodah;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class decision_page extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decision_page);
    }

    public void DriverClick(View view){

        startActivity(new Intent(this,log_in.class));
    }
    public void StudentClick(View view){
        finish();
        startActivity(new Intent(this,MapsActivity.class));
    }
}
