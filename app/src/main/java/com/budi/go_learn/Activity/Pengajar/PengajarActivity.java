package com.budi.go_learn.Activity.Pengajar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.budi.go_learn.Activity.Public.LoginPengajar;
import com.budi.go_learn.Controller.SQLiteHandler;
import com.budi.go_learn.Controller.SessionManager;
import com.budi.go_learn.Variable.User;
import com.budi.go_learn.R;
import com.budi.go_learn.Server.Konfigurasi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class PengajarActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView txtName;
    private TextView txtEmail;
    private TextView txtStatus;
    private ImageView imgProfil;
    private ConstraintLayout Header;

    private SQLiteHandler db;
    private SessionManager session;
    private String name, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengajar);
        setTitle("Halaman Pengajar");

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        txtName = header.findViewById(R.id.namaProfil);
        txtEmail = header.findViewById(R.id.emailProfil);
        txtStatus = header.findViewById(R.id.statusProfil);
        imgProfil = header.findViewById(R.id.fotoProfil);
        Header = header.findViewById(R.id.header);

        Header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ProfilPengajar.class);
                startActivity(i);

            }
        });

        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        HashMap<String, String> user = db.getUserDetails();

        name = user.get(User.KEY_NAME);
        email = user.get(User.KEY_EMAIL);

        txtName.setText(name);
        txtEmail.setText(email);
        txtStatus.setText("Pengajar");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        getImage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getImage();
        HashMap<String, String> user = db.getUserDetails();

        name = user.get(User.KEY_NAME);
        email = user.get(User.KEY_EMAIL);

        txtName.setText(name);
        txtEmail.setText(email);
        txtStatus.setText("Pengajar");
    }

    private void getImage() {
        class GetImage extends AsyncTask<String,Void,Bitmap> {
            @Override
            protected void onPostExecute(Bitmap b) {
                super.onPostExecute(b);
                if (b != null){
                    imgProfil.setImageBitmap(b);
                }
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

    private void logoutUser() {
        session.setLogin(false);
        db.deleteUsers();

        Intent intent = new Intent(PengajarActivity.this, LoginPengajar.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), SettingPengajar.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_logout) {
            logoutUser();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
