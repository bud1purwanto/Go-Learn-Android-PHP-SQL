package com.budi.go_learn.Activity.User;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.budi.go_learn.Activity.Public.LoginActivity;
import com.budi.go_learn.Adapter.AdminAdapter2;
import com.budi.go_learn.Adapter.MenuUserAdapter;
import com.budi.go_learn.Controller.SQLiteHandler;
import com.budi.go_learn.Controller.SessionManager;
import com.budi.go_learn.Models.mFitur;
import com.budi.go_learn.R;
import com.budi.go_learn.RecyclerView.ClickListener;
import com.budi.go_learn.RecyclerView.RecyclerTouchListener;
import com.budi.go_learn.Server.Konfigurasi;
import com.budi.go_learn.Variable.Pengajar;
import com.budi.go_learn.Variable.User;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView txtName;
    private TextView txtEmail;
    private TextView txtStatus;
    private ImageView imgProfil;
    private ConstraintLayout Header;

    private SQLiteHandler db;
    private SessionManager session;
    private String name, email;

    private RecyclerView recyclerview;
    private MenuUserAdapter adapter1;
    private AdminAdapter2 adapter2;
    private List<mFitur> menuList = new ArrayList<>();

    private static final int TIME_INTERVAL = 3000;
    private long mBackPressed;
    public static final int NOTIFICATION_ID = 1;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Menu1();
                    return true;
                case R.id.navigation_notifications:
                    Pendidikan();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        setTitle("Halaman User");

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        recyclerview = (RecyclerView) findViewById(R.id.rvHome);

        txtName = header.findViewById(R.id.namaProfil);
        txtEmail = header.findViewById(R.id.emailProfil);
        txtStatus = header.findViewById(R.id.statusProfil);
        imgProfil = header.findViewById(R.id.fotoProfil);
        Header = header.findViewById(R.id.header);

        Header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ProfilUser.class);
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
        txtStatus.setText("User");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        Menu();
        getImage();

        getSupportActionBar().setSubtitle("Pilih Mata Pelajaran");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void tampilNotification(View v) {

//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.computerdrift.blogspot.com/"));
        Intent intent = new Intent(getApplicationContext(), ProfilUser.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.loading2)
                .setContentTitle("Notifikasi Baru")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(12)
                .setContentText("Kunjungi Blog Saya");


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build()
        );
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
        txtStatus.setText("User");
    }

    private void Menu(){
        adapter1 = new MenuUserAdapter(menuList);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new GridLayoutManager(this,2);
        recyclerview.setLayoutManager(mLayoutManager);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        menuList();
        recyclerview.setAdapter(adapter1);
        recyclerview.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerview, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (position==0){
                    Intent i = new Intent(UserActivity.this, ListPengajar.class);
                    i.putExtra(Pengajar.KEY_PELAJARAN, "Bahasa Indonesia");
                    startActivity(i);
                } else if (position==1){
                    Intent i = new Intent(UserActivity.this, ListPengajar.class);
                    i.putExtra(Pengajar.KEY_PELAJARAN, "Matematika");
                    startActivity(i);
                } else if (position==2){
                    Intent i = new Intent(UserActivity.this, ListPengajar.class);
                    i.putExtra(Pengajar.KEY_PELAJARAN, "IPA");
                    startActivity(i);
                } else if (position==3){
                    Intent i = new Intent(UserActivity.this, ListPengajar.class);
                    i.putExtra(Pengajar.KEY_PELAJARAN, "Bahasa Inggris");
                    startActivity(i);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private void Menu1(){
        adapter1 = new MenuUserAdapter(menuList);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new GridLayoutManager(this,2);
        recyclerview.setLayoutManager(mLayoutManager);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        menuList();
        recyclerview.setAdapter(adapter1);
    }

    private void menuList(){
        menuList.clear();
        mFitur menu = new mFitur(R.drawable.indo, "Bahasa Indonesia");
        menuList.add(menu);
        menu = new mFitur(R.drawable.matematika, "Matematika");
        menuList.add(menu);
        menu = new mFitur(R.drawable.atom, "Pengetahuan Alam");
        menuList.add(menu);
        menu = new mFitur(R.drawable.inggris, "Bahasa Inggris");
        menuList.add(menu);
        adapter1.notifyDataSetChanged();

    }

    private void Pendidikan(){
        adapter2 = new AdminAdapter2();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerview.setLayoutManager(mLayoutManager);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setAdapter(adapter2);

        recyclerview.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerview, new ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
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
                String add = Konfigurasi.URL_FOTOPROFIL_USER+"?email="+email;
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

        Intent intent = new Intent(UserActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(getBaseContext(), "Tekan lagi untuk keluar", Toast.LENGTH_SHORT).show();
        }
        mBackPressed = System.currentTimeMillis();
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.listorder) {
            Intent i = new Intent(UserActivity.this, ListOrder.class);
            i.putExtra(User.KEY_EMAIL, email);
            startActivity(i);
        } else if (id == R.id.history) {

        } else if (id == R.id.editprofil) {

        } else if (id == R.id.logout) {
            logoutUser();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
