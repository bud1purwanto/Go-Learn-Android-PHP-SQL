package com.budi.go_learn.Activity.Public;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.budi.go_learn.Activity.Pengajar.PengajarActivity;
import com.budi.go_learn.Controller.AppController;
import com.budi.go_learn.Controller.SQLiteHandler;
import com.budi.go_learn.Controller.SessionManager;
import com.budi.go_learn.Variable.Pengajar;
import com.budi.go_learn.Variable.User;
import com.budi.go_learn.R;
import com.budi.go_learn.Server.Konfigurasi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginPengajar extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnLogin;
    private Button btnLoginUser;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_pengajar);
        setTitle("Login Pengajar");
        btnLoginUser = (Button) findViewById(R.id.btnLoginUser);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        if (session.isLoggedIn()) {
            Intent intent = new Intent(LoginPengajar.this, PengajarActivity.class);
            startActivity(intent);
            finish();

        }

        btnLoginUser.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                boolean cancel = false;
                View focusView = null;

                if (!isPasswordValid(password)) {
                    inputPassword.setError(getString(R.string.error_invalid_password));
                    focusView = inputPassword;
                    cancel = true;
                }

                if (TextUtils.isEmpty(email)) {
                    inputEmail.setError(getString(R.string.error_field_required));
                    focusView = inputEmail;
                    cancel = true;
                } else if (!isEmailValid(email)) {
                    inputEmail.setError(getString(R.string.error_invalid_email));
                    focusView = inputEmail;
                    cancel = true;
                }

                else if (cancel) {
                    focusView.requestFocus();
                }

                else if (!email.isEmpty() && !password.isEmpty()) {
                    checkLogin(email, password);
                }
            }

        });
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 4;
    }

    /**
     * function to verify login details in mysql db
     * */

    private void checkLogin(final String email, final String password) {
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Konfigurasi.URL_LOGIN_PENGAJAR, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        session.setLogin(true);
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString(Pengajar.KEY_NAME);
                        String email = user.getString(Pengajar.KEY_EMAIL);
                        String phone = user.getString(Pengajar.KEY_PHONE);
                        String gender = user.getString(Pengajar.KEY_GENDER);
                        String address = user.getString(Pengajar.KEY_ADDRESS);
                        String created_at = user.getString(Pengajar.KEY_CREATED);
                        String pelajaran = user.getString(Pengajar.KEY_PELAJARAN);
                        String status = user.getString(Pengajar.KEY_STATUS);
                        String keterangan = user.getString(Pengajar.KEY_KET);
                        String active = user.getString(Pengajar.KEY_ACTIVE);
                        String work = user.getString(Pengajar.KEY_WORK);

                        db.addPengajar(name, email, phone, gender, address, uid, created_at, pelajaran, status, keterangan, active, work);

                        Intent intent = new Intent(LoginPengajar.this,
                                    PengajarActivity.class);
                        startActivity(intent);
                        finish();


                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(User.KEY_EMAIL, email);
                params.put(User.KEY_PASSWORD, password);

                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}