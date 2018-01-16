package com.budi.go_learn.Activity.User;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.budi.go_learn.Adapter.lPengajarAdapter;
import com.budi.go_learn.Controller.AppController;
import com.budi.go_learn.Models.RoundImage;
import com.budi.go_learn.Models.mPengajar;
import com.budi.go_learn.R;
import com.budi.go_learn.RecyclerView.ClickListener;
import com.budi.go_learn.RecyclerView.RecyclerTouchListener;
import com.budi.go_learn.Server.Konfigurasi;
import com.budi.go_learn.Variable.Pengajar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListPengajar extends AppCompatActivity
        implements SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener{
    SwipeRefreshLayout swipe;
    List<mPengajar> listPengajar;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager recyclerViewlayoutManager;
    lPengajarAdapter pengajarAdapter;
    ProgressBar progressBar;
    String Pelajaran;
    TextView pesan;
    Bitmap bitmap;
    String email = "q@.";

    private static final String TAG = ListPengajar.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_pengajar);
        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);
        pesan = (TextView) findViewById(R.id.pesan);
        listPengajar = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        recyclerView.setHasFixedSize(true);
        recyclerViewlayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerViewlayoutManager);
        progressBar.setVisibility(View.VISIBLE);
        pengajarAdapter = new lPengajarAdapter(this, listPengajar);

        Intent i = getIntent();
        Pelajaran = i.getStringExtra(Pengajar.KEY_PELAJARAN);
        setTitle(Pelajaran);

        ActionBar toolbar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setSubtitle("Pilih Pengajar");

        swipe.setOnRefreshListener(this);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {

            @Override
            public void onClick(View view, int position) {
                Intent i = new Intent(getApplicationContext(), OrderPengajar.class);
                mPengajar lP = listPengajar.get(position);
                i.putExtra(Pengajar.KEY_ID,lP.getId());
                i.putExtra(Pengajar.KEY_NAME,lP.getName());
                i.putExtra(Pengajar.KEY_EMAIL,lP.getEmail());
                i.putExtra(Pengajar.KEY_PHONE,lP.getPhone());
                i.putExtra(Pengajar.KEY_GENDER,lP.getGender());
                i.putExtra(Pengajar.KEY_ADDRESS,lP.getAddress());
                i.putExtra(Pengajar.KEY_PELAJARAN,lP.getPelajaran());
                i.putExtra(Pengajar.KEY_KET,lP.getKeterangan());
                i.putExtra(Pengajar.KEY_ACTIVE,lP.getActive());
                i.putExtra(Pengajar.KEY_WORK,lP.getWork());
                startActivity(i);
            }

            @Override
            public void onLongClick(View view, int position) {
                mPengajar lP = listPengajar.get(position);
                Toast.makeText(getApplicationContext(), lP.getName(), Toast.LENGTH_SHORT).show();
            }
        }));

        DataCalling();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.action_search, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint(getString(R.string.type_name));
        searchView.setIconified(true);
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        pengajarAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        progressBar.setVisibility(View.VISIBLE);
        listPengajar.clear();
        pengajarAdapter.notifyDataSetChanged();
        DataCalling();
        swipe.setRefreshing(false);
    }

    private void getImage(final String imel, final mPengajar data, final int position) {
        class GetImage extends AsyncTask<String,Void,Bitmap> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Bitmap b) {
                super.onPostExecute(b);
                if (b != null){
                    data.setFoto(new RoundImage(
                            getApplicationContext()).transform(bitmap));
                    int result = position-1;
                    listPengajar.set(result, data);
                    recyclerView.setAdapter(pengajarAdapter);
                }
            }

            @Override
            protected Bitmap doInBackground(String... params) {
                email = params[0];
                String add = Konfigurasi.URL_FOTOPROFIL_PENGAJAR+"?email="+imel;
                URL url = null;
                try {
                    url = new URL(add);
                    bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return bitmap;
            }
        }

        GetImage gi = new GetImage();
        gi.execute(imel);
    }

    @Override
    public boolean onQueryTextSubmit(String keyword) {
        DataSearch(keyword);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String keyword) {
        DataSearch(keyword);
        return false;
    }

    public void DataCalling(){
        StringRequest strReq = new StringRequest(Request.Method.POST, Konfigurasi.URL_LIST_PENGAJAR, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e("Response: ", response.toString());
                if (response.toString().equals("Kosong")){
                    Toast.makeText(getApplicationContext(), "Tidak Ada Data", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    pesan.setVisibility(1);
                    pesan.setText("Tidak Ada Data");
                } else{
                    try {
                        JSONObject jObj = new JSONObject(response);
                        String getObject = jObj.getString("results");
                        JSONArray jsonArray = new JSONArray(getObject);
                        int position = jsonArray.length();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            mPengajar data = new mPengajar();
                            data.setId(obj.getString(Pengajar.KEY_ID));
                            data.setName(obj.getString(Pengajar.KEY_NAME));
                            data.setEmail(obj.getString(Pengajar.KEY_EMAIL));
                            data.setPhone(obj.getString(Pengajar.KEY_PHONE));
                            data.setGender(obj.getString(Pengajar.KEY_GENDER));
                            data.setAddress(obj.getString(Pengajar.KEY_ADDRESS));
                            data.setPelajaran(obj.getString(Pengajar.KEY_PELAJARAN));
                            data.setKeterangan(obj.getString(Pengajar.KEY_KET));
                            data.setActive(obj.getString(Pengajar.KEY_ACTIVE));
                            data.setWork(obj.getString(Pengajar.KEY_WORK));
                            getImage(obj.getString(Pengajar.KEY_EMAIL), data, position);

                            listPengajar.add(data);
                            recyclerView.setAdapter(pengajarAdapter);

                        }
                        progressBar.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    pengajarAdapter.notifyDataSetChanged();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Periksa koneksi internet Anda", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("pelajaran", Pelajaran);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, "json_obj_req");
    }

    private void DataSearch(final String keyword) {
        StringRequest strReq = new StringRequest(Request.Method.POST, Konfigurasi.URL_CARI_PENGAJAR, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e("Response: ", response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    int value = jObj.getInt("value");

                    if (value == 1) {
                        listPengajar.clear();
                        String getObject = jObj.getString("results");
                        JSONArray jsonArray = new JSONArray(getObject);
                        int position = jsonArray.length();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            mPengajar data = new mPengajar();
                            data.setId(obj.getString(Pengajar.KEY_ID));
                            data.setName(obj.getString(Pengajar.KEY_NAME));
                            data.setEmail(obj.getString(Pengajar.KEY_EMAIL));
                            data.setPhone(obj.getString(Pengajar.KEY_PHONE));
                            data.setGender(obj.getString(Pengajar.KEY_GENDER));
                            data.setAddress(obj.getString(Pengajar.KEY_ADDRESS));
                            data.setPelajaran(obj.getString(Pengajar.KEY_PELAJARAN));
                            data.setKeterangan(obj.getString(Pengajar.KEY_KET));
                            data.setActive(obj.getString(Pengajar.KEY_ACTIVE));
                            data.setWork(obj.getString(Pengajar.KEY_WORK));
                            getImage(obj.getString(Pengajar.KEY_EMAIL), data, position);

                            listPengajar.add(data);
                            recyclerView.setAdapter(pengajarAdapter);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), jObj.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                pengajarAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Periksa koneksi internet Anda", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("keyword", keyword);
                params.put("pelajaran", Pelajaran);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, "json_obj_req");
    }
}