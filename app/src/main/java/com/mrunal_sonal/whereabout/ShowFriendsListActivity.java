package com.mrunal_sonal.whereabout;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by mrunal on 6/29/15.
 */
public class ShowFriendsListActivity extends Activity {
    ArrayList<String> friendsNameList;
    ArrayAdapter<String> arrayAdapter;
    public ListView FriendlListView;
    public String myPhoneNumber;
    public void onCreate(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.friends_list);

        // Get the reference of FriendsListView
        FriendlListView =(ListView)findViewById(R.id.friendsListView);
        Intent  myIntent=getIntent();

        myPhoneNumber=myIntent.getExtras().getString("myPhone");
        friendsNameList = new ArrayList<String>();
       // Log.i("myPhone: ",myPhoneNumber);
        //Toast.makeText(getApplicationContext(),myPhoneNumber,Toast.LENGTH_SHORT);

        getFriendsNames();

    }

    void getFriendsNames()
    {
        new GetMyFriendsDataTask().execute(new ApiConnector());
        arrayAdapter =
                new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, friendsNameList);
        // Set The Adapter
        FriendlListView.setAdapter(arrayAdapter);

    }
    private class GetMyFriendsDataTask extends AsyncTask<ApiConnector,Long,JSONArray>
    {
        @Override
        protected JSONArray doInBackground(ApiConnector... params) {

            // it is executed on Background thread

            return params[0].GetMyFriends(myPhoneNumber);
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {

                  if(jsonArray!=null){
                    JSONObject json;

                    try {
                    for(int i=0;i<jsonArray.length();i++) {
                        json = jsonArray.getJSONObject(i);

                        String name = json.getString("Name");
                        String receivedPhonenumber=json.getString("Phonenumber");
                        receivedPhonenumber=receivedPhonenumber.substring(
                                receivedPhonenumber.length()-10,receivedPhonenumber.length());
                            if (!name.equals("-1#+1")) {
                                if(!receivedPhonenumber.equals(myPhoneNumber)) {
                                    friendsNameList.add(name + "\n" + json.getString("Phonenumber"));

                                }

                            } else {

                                friendsNameList.add("You have no friends in your list yet.");
                            }
                    }
                     // Create The Adapter with passing ArrayList as 3rd parameter
                        arrayAdapter =
                                new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, friendsNameList);
                        // Set The Adapter
                        FriendlListView.setAdapter(arrayAdapter);

                        // register onClickListener to handle click events on each item
                        FriendlListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            // argument position gives the index of item which is clicked
                            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {

                                String selectedFriend = friendsNameList.get(position);
                                String friendNumber=selectedFriend.substring(selectedFriend.length() - 10, selectedFriend.length());
                                //Toast.makeText(getApplicationContext(), friendNumber, Toast.LENGTH_LONG).show();
                                Intent goToMapsActivity= new Intent(getApplicationContext(),MapsActivity.class);
                                goToMapsActivity.putExtra("PhoneNoFromFriendsList",friendNumber);
                                startActivity(goToMapsActivity);
                            }
                        });
                        FriendlListView.refreshDrawableState();

                    }
                    catch(Exception e)
                    {
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

                    }
                }
        }
    }
}
