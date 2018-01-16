package com.budi.go_learn.Activity.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.budi.go_learn.Activity.Public.LoginActivity;
import com.budi.go_learn.Adapter.AdminAdapter1;
import com.budi.go_learn.Adapter.AdminAdapter2;
import com.budi.go_learn.Controller.SQLiteHandler;
import com.budi.go_learn.Controller.SessionManager;
import com.budi.go_learn.Models.mFitur;
import com.budi.go_learn.Variable.User;
import com.budi.go_learn.R;
import com.budi.go_learn.RecyclerView.ClickListener;
import com.budi.go_learn.RecyclerView.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdminActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView txtName;
    private TextView txtEmail;
    private TextView txtStatus;
    private ConstraintLayout imgProfil;

    private SQLiteHandler db;
    private SessionManager session;

    private RecyclerView recyclerview;
    private AdminAdapter1 adapter1;
    private AdminAdapter2 adapter2;
    private List<mFitur> menuList = new ArrayList<>();

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
        setContentView(R.layout.activity_admin);
        setTitle("Halaman Admin");

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        txtName = header.findViewById(R.id.namaProfil);
        txtEmail = header.findViewById(R.id.emailProfil);
        txtStatus = header.findViewById(R.id.statusProfil);
        imgProfil = header.findViewById(R.id.header);

        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }
        recyclerview = (RecyclerView) findViewById(R.id.rvHome);

        Menu();

        HashMap<String, String> user = db.getUserDetails();

        String name = user.get(User.KEY_NAME);
        String email = user.get(User.KEY_EMAIL);
        String status = user.get(User.KEY_STATUS);

        txtName.setText(name);
        txtEmail.setText(email);
        if (status.equalsIgnoreCase("admin")){
            txtStatus.setText("Admin");
        }else{
            txtStatus.setText("User");
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

    }

    private void Menu(){
        adapter1 = new AdminAdapter1(menuList);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new GridLayoutManager(this,2);
        recyclerview.setLayoutManager(mLayoutManager);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        menuList();
        recyclerview.setAdapter(adapter1);
//        recyclerview.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerview, new ClickListener() {
//            @Override
//            public void onClick(View view, int position) {
//                Intent fitur1 = new Intent(getApplicationContext(), Kamret.class);
//                Intent fitur2 = new Intent(getApplicationContext(), Kamret.class);
//                if(position==0){
//                    startActivity(fitur1);
//                }
//                else if (position==1){
//                    startActivity(fitur2);
//                }
//            }
//
//            @Override
//            public void onLongClick(View view, int position) {
//
//            }
//        }));
    }

    private void Menu1(){
        adapter1 = new AdminAdapter1(menuList);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new GridLayoutManager(this,2);
        recyclerview.setLayoutManager(mLayoutManager);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        menuList();
        recyclerview.setAdapter(adapter1);
    }

    private void menuList(){
        menuList.clear();
        mFitur menu = new mFitur(R.drawable.dash1, "mFitur 1");
        menuList.add(menu);
        menu = new mFitur(R.drawable.dash2, "mFitur 2");
        menuList.add(menu);
        menu = new mFitur(R.drawable.dash3, "mFitur 3");
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

    private void logoutUser() {
        session.setLogin(false);
        db.deleteUsers();

        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent i = new Intent(getApplicationContext(), AddPengajar.class);
            startActivity(i);
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
