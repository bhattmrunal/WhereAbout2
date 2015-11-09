package com.mrunal_sonal.whereabout;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserDetailsActivity extends Activity {

    TextView nameTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends_list);

     //   this.nameTextView= (TextView) findViewById(R.id.textView2);
        new GetMyUserDataTask().execute(new ApiConnector());

    }

    public void setTextToTextView(JSONArray jsonArray)
    {
        String s  = "";
        for(int i=0; i<jsonArray.length();i++){

            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                s = s +
                        "ID : "+json.getString("ID")+" Name : "+json.getString("Name")+ " Phone : "+json.getString("Phonenumber") +"\n\n";
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        this.nameTextView.setText(s);

    }


    private class GetMyUserDataTask extends AsyncTask<ApiConnector,Long,JSONArray>
    {
        @Override
        protected JSONArray doInBackground(ApiConnector... params) {

            // it is executed on Background thread

            return params[0].GetMyUserData("2168012314");
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {

           try {
               if(jsonArray!=null)
               {
                   Toast.makeText(getApplicationContext(),"Location Updated",Toast.LENGTH_LONG).show();
               }
           }
           catch(Exception e)
           {
               Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
           }


        }
    }



}