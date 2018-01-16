package com.budi.go_learn.Activity.User;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.budi.go_learn.Controller.AppController;
import com.budi.go_learn.Controller.SQLiteHandler;
import com.budi.go_learn.R;
import com.budi.go_learn.Server.Konfigurasi;
import com.budi.go_learn.Variable.Pengajar;
import com.budi.go_learn.Variable.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ListOrder extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = ListOrder.class.getSimpleName();
    String id, name, email, phone, address, pelajaran, description, gender, active, work;
    TextView txtEmail, txtPhone, txtAddress, txtPelajaran, txtDescription, txtStatus, txtGender;
    ImageView imgOrderPengajar, imgEmail, imgPhone;
    ProgressDialog pDialog;
    Button btnContact;
    private static final int REQUEST_PHONE_CALL = 1;
    private SQLiteHandler db;
    String nama, telp, imel, salam, text;
    PopupMenu popup;
    FloatingActionButton map;
    ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_order);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnContact = findViewById(R.id.btnContact);

        imgOrderPengajar = findViewById(R.id.imgOrderPengajar);
        imgEmail = findViewById(R.id.imgEmail);
        imgPhone = findViewById(R.id.imgPhone);

        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtGender = findViewById(R.id.txtGender);
        txtAddress = findViewById(R.id.txtAddress);
        txtPelajaran = findViewById(R.id.txtPelajaran);
        txtStatus = findViewById(R.id.txtStatus);
        txtDescription = findViewById(R.id.txtDescription);

        imgEmail.setOnClickListener(this);
        imgPhone.setOnClickListener(this);
        btnContact.setOnClickListener(this);
        txtEmail.setOnClickListener(this);
        txtPhone.setOnClickListener(this);

        db = new SQLiteHandler(getApplicationContext());
        pDialog = new ProgressDialog(this);

        Intent i = getIntent();
        email = i.getStringExtra(Pengajar.KEY_EMAIL);

        HashMap<String, String> user = db.getUserDetails();
        nama = user.get(User.KEY_NAME);
        telp = user.get(User.KEY_PHONE);
        imel = user.get(User.KEY_EMAIL);

        DataCalling();
        setTitle(name);

        map = findViewById(R.id.mapuser);
        map.setOnClickListener(this);

        getImage();
    }

    public void DataCalling(){
        StringRequest strReq = new StringRequest(Request.Method.POST, Konfigurasi.URL_LISTORDER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e("Response: ", response.toString());
                if (response.toString().equals("Kosong")){
                    Toast.makeText(getApplicationContext(), "Tidak Ada Data", Toast.LENGTH_LONG).show();
                } else{
                    try {
                        JSONObject jObj = new JSONObject(response);
                        String getObject = jObj.getString("results");
                        JSONArray jsonArray = new JSONArray(getObject);

                        JSONObject c = jsonArray.getJSONObject(0);
                        name = c.getString(Pengajar.KEY_NAME);
                        email = c.getString("email_pengajar");
                        phone = c.getString(Pengajar.KEY_PHONE);
                        gender = c.getString(Pengajar.KEY_GENDER);
                        address = c.getString(Pengajar.KEY_ADDRESS);
                        pelajaran = c.getString(Pengajar.KEY_PELAJARAN);
                        description = c.getString(Pengajar.KEY_KET);

                        setTitle(name);

                        txtEmail.setText(email);
                        txtPhone.setText(phone);
                        txtGender.setText(gender);
                        txtAddress.setText(address);
                        txtPelajaran.setText(pelajaran);
                        txtDescription.setText(description);
                        if (gender.equals("Laki-laki")){
                            salam = "Bapak";
                        } else {
                            salam = "Ibu";
                        }

                        text = "Permisi "+salam+" "+name+", \nSaya "+nama+" ingin memesan jasa les Anda. " +
                                "\n\nSaya bisa dihubungi melalui: \nTelepon      : "+telp+"\nEmail    : "+imel;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Periksa koneksi internet Anda", Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(Pengajar.KEY_EMAIL, email);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, "json_obj_req");
    }

    @Override
    public void onClick(View view) {
        if (view == map) {
            Intent i = new Intent(ListOrder.this, MapsUser.class);
            i.putExtra(Pengajar.KEY_ADDRESS, address);
            startActivity(i);
        }
        if (view == btnContact) {
            popup = new PopupMenu(ListOrder.this, btnContact);
            popup.getMenuInflater().inflate(R.menu.menu_call, popup.getMenu());

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    switch (id){
                        case R.id.call:
                            cCall();
                            break;
                        case R.id.sms:
                            cSms();
                            break;
                        case R.id.wa:
                            cWA();
                            break;
                        case R.id.email:
                            cEmail();
                            break;
                    }
                    return true;
                }
            });
            popup.show();
        }
    }

    public void cCall(){
        Intent callIntent = new Intent(Intent.ACTION_DIAL); //ACTION_CALL
        callIntent.setData(Uri.parse("tel:"+phone+""));
        if (ContextCompat.checkSelfPermission(ListOrder.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ListOrder.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
        } else {
            startActivity(callIntent);
        }
    }

    public void cSms(){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phone));
        intent.putExtra("sms_body", text);
        startActivity(intent);
    }

    public void cWA(){
        String str = phone;
        str = "+62"+str.substring(1);

        Intent whatsapp = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://api.whatsapp.com/send?phone="+str+"&text="+text+""));
        startActivity(whatsapp);
    }

    public void cEmail(){
        Intent emailIntent = new Intent (Intent.ACTION_VIEW , Uri.parse("mailto:" +email));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Pemesanan Pengajar");
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(emailIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(share);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getImage() {
        class GetImage extends AsyncTask<String,Void,Bitmap> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog.setMessage("Loading...");
                pDialog.setCancelable(false);
                pDialog.show();
            }

            @Override
            protected void onPostExecute(Bitmap b) {
                super.onPostExecute(b);
                if (b != null){
                    imgOrderPengajar.setImageBitmap(b);
                }
                pDialog.dismiss();
            }

            @Override
            protected Bitmap doInBackground(String... params) {
                email = params[0];
                String add = Konfigurasi.URL_FOTOPROFIL_PENGAJAR+"?email="+email;
                URL url = null;
                Bitmap image = null;
                try {
                    url = new URL(add);
                    image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return image;
            }
        }

        GetImage gi = new GetImage();
        gi.execute(email);
    }
}

