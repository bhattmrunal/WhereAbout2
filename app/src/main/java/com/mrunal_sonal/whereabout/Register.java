package com.mrunal_sonal.whereabout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;


/**
 * Created by mrunal on 6/25/15.
 */
public class Register extends Activity {

    private String myPhoneNumber, myName, password, re_password;
    private Toolbar toolbar;
    private Button registerButton;
    TextView phoneTextView;
    private EditText nameEditText, pass,re_pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        toolbar= (Toolbar) findViewById(R.id.app_bar);
       toolbar.setTitle("Registration");
        toolbar.showOverflowMenu();
        registerButton= (Button) findViewById(R.id.register_registerationButton);

        phoneTextView= (TextView) findViewById(R.id.register_phoneTextView);
        Intent myIntent= getIntent();
        //getActionBar().setTitle("Registration");
        myPhoneNumber=myIntent.getExtras().getString("myPhone");

        phoneTextView.setText("Your Phone number: "+myPhoneNumber);
        nameEditText= (EditText) findViewById(R.id.register_nameEditText);
        pass=(EditText)findViewById(R.id.register_password);
        re_pass=(EditText)findViewById(R.id.register_re_password);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myName=nameEditText.getText().toString();
                password=pass.getText().toString();
                re_password=re_pass.getText().toString();
                if(myName.trim()==null)
                {
                    Toast.makeText(getApplicationContext(),"Please enter your name", Toast.LENGTH_SHORT).show();
                    nameEditText.setFocusableInTouchMode(true);
                    nameEditText.requestFocus();
                }
                else if(password.trim()==null)
                {
                    Toast.makeText(getApplicationContext(),"Password field cannot be left blank", Toast.LENGTH_SHORT).show();
                    pass.setFocusableInTouchMode(true);
                    pass.requestFocus();
                }
                else if(re_password.trim()==null || !(password.equals(re_password)))
                {
                    Toast.makeText(getApplicationContext(),"" +
                            "please reenter the password correctly", Toast.LENGTH_SHORT).show();
                    re_pass.setFocusableInTouchMode(true);
                    re_pass.requestFocus();
                }
                else if(!password.equals(re_password))
                {
                    Toast.makeText(getApplicationContext(),"" +
                            "The password fields do not match.", Toast.LENGTH_SHORT).show();
                    re_pass.setFocusableInTouchMode(true);
                    re_pass.requestFocus();
                }
                else
                {
                    new addNewUserTask().execute(new ApiConnector());

                    SharedPreferences pref=getSharedPreferences("register", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editr=pref.edit();
                    editr.putString("myname",myName);
                    Intent myIntent = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivityForResult(myIntent, 0);
                }

            }
        });

    }


    // This is Async task, which will Add/Register new User separately of the main thread
    private class addNewUserTask extends AsyncTask<ApiConnector,Long,JSONArray>
    {
        @Override
        protected JSONArray doInBackground(ApiConnector... params) {

            // it is executed on Background thread

            return params[0].addNewUser(myPhoneNumber,myName, password);
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {

            try {
                if(jsonArray!=null)
                {
                    Toast.makeText(getApplicationContext(), "You are now registered with Phone: "+myPhoneNumber, Toast.LENGTH_LONG).show();
                }
            }
            catch(Exception e)
            {
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }
}
