package com.budi.go_learn.Activity.User;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.budi.go_learn.Controller.RequestHandler;
import com.budi.go_learn.Controller.SQLiteHandler;
import com.budi.go_learn.R;
import com.budi.go_learn.Server.Konfigurasi;
import com.budi.go_learn.Variable.Pengajar;
import com.budi.go_learn.Variable.Transaksi;
import com.budi.go_learn.Variable.User;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class OrderPengajar extends AppCompatActivity implements View.OnClickListener {
    String id, name, email, phone, address, pelajaran, description, gender, active, work;
    TextView txtEmail, txtPhone, txtAddress, txtPelajaran, txtDescription, txtStatus, txtGender;
    ImageView imgOrderPengajar, imgEmail, imgPhone;
    ProgressDialog pDialog;
    Button btnContact;
    private static final int REQUEST_PHONE_CALL = 1;
    private SQLiteHandler db;
    String nama, telp, imel, salam, text;
    PopupMenu popup;
    FloatingActionButton map, order;
    ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_pengajar);
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
        id = i.getStringExtra(Pengajar.KEY_ID);
        name = i.getStringExtra(Pengajar.KEY_NAME);
        email = i.getStringExtra(Pengajar.KEY_EMAIL);
        phone = i.getStringExtra(Pengajar.KEY_PHONE);
        gender = i.getStringExtra(Pengajar.KEY_GENDER);
        address = i.getStringExtra(Pengajar.KEY_ADDRESS);
        pelajaran = i.getStringExtra(Pengajar.KEY_PELAJARAN);
        description = i.getStringExtra(Pengajar.KEY_KET);
        active = i.getStringExtra(Pengajar.KEY_ACTIVE);
        work = i.getStringExtra(Pengajar.KEY_WORK);

        setTitle(name);
        txtEmail.setText(email);
        txtPhone.setText(phone);
        txtGender.setText(gender);
        txtAddress.setText(address);
        txtPelajaran.setText(pelajaran);
        txtDescription.setText(description);


        HashMap<String, String> user = db.getUserDetails();
        nama = user.get(User.KEY_NAME);
        telp = user.get(User.KEY_PHONE);
        imel = user.get(User.KEY_EMAIL);
        if (gender.equals("Laki-laki")){
            salam = "Bapak";
        } else {
            salam = "Ibu";
        }

        text = "Permisi "+salam+" "+name+", \nSaya "+nama+" ingin memesan jasa les Anda. " +
                "\n\nSaya bisa dihubungi melalui: \nTelepon      : "+telp+"\nEmail    : "+imel;

        map = findViewById(R.id.mapuser);
        order = findViewById(R.id.order);
        map.setOnClickListener(this);
        order.setOnClickListener(this);

        if (active.equals("0") || work.equals("1")){
            order.setVisibility(View.GONE);
        }

        getImage();
    }

    @Override
    public void onClick(View view) {
        if (view == map) {
            Intent i = new Intent(OrderPengajar.this, MapsUser.class);
            i.putExtra(Pengajar.KEY_ADDRESS, address);
            startActivity(i);
        }
        if (view == order) {
            KonfirmasiOrder();
        }
        if (view == btnContact) {
            popup = new PopupMenu(OrderPengajar.this, btnContact);
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
        if (ContextCompat.checkSelfPermission(OrderPengajar.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(OrderPengajar.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
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

    private void Order(){
        class Order extends AsyncTask<Void,Void,String> {


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(OrderPengajar.this, "Processing", "Please Wait...", false, false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Toast.makeText(OrderPengajar.this, s, Toast.LENGTH_LONG).show();
                if (!s.equals("Anda sudah melakukan transaksi lainnya")){
                    editStatusPengajar();
                    onBackPressed();
                } else {
                    startActivity(new Intent(OrderPengajar.this, UserActivity.class));
                    finish();
                }
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String,String> data = new HashMap<>();
                data.put(Transaksi.KEY_EMAIL_PENGAJAR, email);
                data.put(Transaksi.KEY_EMAIL_USER, imel);

                RequestHandler rh = new RequestHandler();
                String s = rh.sendPostRequest(Konfigurasi.URL_ORDER_TRANSAKSI, data);
                return s;
            }
        }
        Order o = new Order();
        o.execute();
    }

    private void KonfirmasiOrder(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Apakah Kamu Yakin Ingin Order Oengajar?");

        alertDialogBuilder.setPositiveButton("Ya",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Order();
                        order.setVisibility(View.GONE);
                        loading.dismiss();
                    }
                });

        alertDialogBuilder.setNegativeButton("Tidak",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void editStatusPengajar(){
        class editStatusPengajar extends AsyncTask<Void,Void,String>{

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put(Pengajar.KEY_EMAIL, email);
                hashMap.put(Pengajar.KEY_WORK, "1");

                RequestHandler rh = new RequestHandler();
                String s = rh.sendPostRequest(Konfigurasi.URL_WORK,hashMap);

                return s;
            }
        }

        editStatusPengajar ue = new editStatusPengajar();
        ue.execute();
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
