package com.ishuttle.kodah;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Register extends AppCompatActivity {
    EditText ET_username,ET_regpassword,ET_code;
    String username,regpassword,secret_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reg_activity);
        ET_username=(EditText)findViewById(R.id.id_username);
        ET_regpassword=(EditText)findViewById(R.id.id_regpassword);
        ET_code=(EditText)findViewById(R.id.id_code);




    }
    public void RegisterClick(View view){

        username=ET_username.getText().toString();
        regpassword=ET_regpassword.getText().toString();
        secret_code=ET_code.getText().toString();

        if (username.matches("")) {
            Toast.makeText(this, "You have not entered your username", Toast.LENGTH_SHORT).show();
            return;
        }
       else if (secret_code.matches("")) {
            Toast.makeText(this, "You have not entered your secret code", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (regpassword.matches("")) {
            Toast.makeText(this, "You have not entered your password", Toast.LENGTH_SHORT).show();
            return;
        }
        if(secret_code.equals("2007")){
            String method="register";



            BackgroundTask backgroundTask=new BackgroundTask(this);
            backgroundTask.execute(method,username,regpassword);

            finish();
            startActivity(new Intent(this,log_in.class));


        }else{

            Toast.makeText(this,"Wrong Secret code entered",Toast.LENGTH_LONG).show();
        }

    }

}
