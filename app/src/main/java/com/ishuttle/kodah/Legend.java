package com.ishuttle.kodah;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Misan on 5/15/2018.
 */

public class Legend extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pop_up);
    }

public void onClose(View view){
        finish();
        startActivity(new Intent(this,locateKNUST.class));
}

    @Override
    public void onBackPressed() {

    }
}
