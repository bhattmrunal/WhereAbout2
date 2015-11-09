package com.mrunal_sonal.whereabout;

/**
 * Created by mrunal on 6/18/15.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

//import android.support.v7.app.AlertDialog;

public class MyActivity extends Activity {

    private static final String TAG = MyActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private Uri uriContact;
    private String contactID;     // contacts unique ID
    private  String contactPhonenumber="0000000000", myPhoneNumber;
    public TextView tv;
    ProgressDialog progress;
    private ListView lv;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.main);
        myPhoneNumber=getIntent().getExtras().getString("myPhone");
        tv= (TextView) findViewById(R.id.numbers);
        Log.d("MyPhone number in conn ",myPhoneNumber);
        startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);

    }

    public void onClickSelectContact(View btnSelectContact) {

        // using native contacts selection
        // Intent.ACTION_PICK = Pick an item from the data, returning what was selected.
        //startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        }
        if (requestCode == REQUEST_CODE_PICK_CONTACTS) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Uri contactData = data.getData();

                    try {

                        String id = contactData.getLastPathSegment();
                        Cursor phoneCur = getContentResolver()
                                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                                + " = ?", new String[] { id },
                                        null);

                        final ArrayList<String> phonesList = new ArrayList<String>();
                        while (phoneCur.moveToNext()) {
                            // This would allow you get several phone addresses
                            // if thestartActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS); phone addresses were stored in an array
                            String phone = phoneCur
                                    .getString(phoneCur
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                            if(phone.contains("-"))
                            {
                                phone=phone.replaceAll("-","");
                            }
                            phonesList.add(phone);
                        }
                        phoneCur.close();

                        if (phonesList.size() == 0) {
                            Toast.makeText(this, getString(R.string.error_no_phone_no_in_contact),
                                    Toast.LENGTH_LONG).show();
                            Intent act1 = new Intent(getApplicationContext(), MapsActivity.class);
                             startActivity(act1);

                        } else if (phonesList.size() == 1) {
                            contactPhonenumber=phonesList.get(0);
                            new addNewConnectionTask().execute(new ApiConnector());
                            Intent act1 = new Intent(getApplicationContext(), MapsActivity.class);
                            startActivity(act1);
                        } else {

                            final String[] phonesArr = new String[phonesList
                                    .size()];
                            for (int i = 0; i < phonesList.size(); i++) {

                                String currentNumberInList=phonesList.get(i);
                                phonesArr[i] = currentNumberInList;
                            }

                            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MyActivity.this);
                            LayoutInflater inflater = getLayoutInflater();
                            View convertView = (View) inflater.inflate(R.layout.contact_filter_list, null);
                            alertDialog.setView(convertView);
                            alertDialog.setTitle("Select Number");
                            ListView ContactListView = (ListView) convertView.findViewById(R.id.contactListView);

                            if(phonesArr.length>1)
                            {
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, phonesList);
                                ContactListView.setAdapter(adapter);
                                final AlertDialog alert=alertDialog.create();
                                ContactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                                            long id) {
                                        contactPhonenumber=phonesList.get(position);
                                        Log.i("Contact Phone: ",contactPhonenumber);
                                        new addNewConnectionTask().execute(new ApiConnector());
                                        alert.dismiss();
                                        Intent act1 = new Intent(getApplicationContext(), MapsActivity.class);
                                        startActivity(act1);
                                    }
                                });
                                alert.show();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("FILES", "Failed to get phone data", e);
                    }
                }
            }

        }
    }

    private void retrieveContactPhoto() {

        Bitmap photo = null;

        try {
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactID)));

            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
                ImageView imageView = (ImageView) findViewById(R.id.img_contact);
                imageView.setImageBitmap(photo);
            }

            assert inputStream != null;
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void retrieveContactNumber() {

        String contactNumber = null;

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        Log.d(TAG, "Contact ID: " + contactID);

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,


                new String[]{contactID},
                null);

        while(cursorPhone.moveToNext())
        {
         //if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            tv.setText(tv.getText()+contactNumber+" \n");
        }

        cursorPhone.close();
        Toast.makeText(this, "Number: " + contactNumber,Toast.LENGTH_SHORT).show();
        //Log.d(TAG, "Contact Phone Number: " + contactNumber);
    }

    private void retrieveContactName() {

        String contactName = null;

        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {

            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.

            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();

        //Toast.makeText(this, "Contact Name: " + contactName,Toast.LENGTH_SHORT).show();
       // Log.d(TAG, "Contact Name: " + contactName);

    }

    private class addNewConnectionTask extends AsyncTask<ApiConnector,Long,JSONArray>
    {
        @Override
        protected JSONArray doInBackground(ApiConnector... params) {

            // it is executed on Background thread

            return params[0].addNewConnection(myPhoneNumber, contactPhonenumber);
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {

            if(jsonArray!=null){
                JSONObject json;

                try {
                    for(int i=0;i<jsonArray.length();i++) {
                        json = jsonArray.getJSONObject(i);

                        String name = json.getString("Name");
                        String ID=json.getString("ID");

                        if (name.equals("-1#+1")) {

                            Toast.makeText(getApplicationContext(),"The user with that number is not yet registered. Please ask them to download WhereAbout app and register.",Toast.LENGTH_LONG).show();


                        } else if(name.equals("-99#+99")){

                            Toast.makeText(getApplicationContext(),"Your request has been sent. It will be shown in your friends' list once the user has accepted your request.",Toast.LENGTH_LONG).show();

                        }
                    }
                    //progress.dismiss();
                }
                catch(Exception e)
                {
                    //Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}