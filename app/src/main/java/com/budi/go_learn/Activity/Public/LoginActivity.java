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

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.budi.go_learn.Activity.Admin.AdminActivity;
import com.budi.go_learn.Activity.Pengajar.PengajarActivity;
import com.budi.go_learn.Activity.User.UserActivity;
import com.budi.go_learn.Controller.AppController;
import com.budi.go_learn.Controller.SQLiteHandler;
import com.budi.go_learn.Controller.SessionManager;
import com.budi.go_learn.Variable.User;
import com.budi.go_learn.R;
import com.budi.go_learn.Server.Konfigurasi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnLogin;
    private Button btnLinkToRegister, btnLoginPengajar, btnForgotPassword;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login User");
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        btnLoginPengajar = (Button) findViewById(R.id.btnLoginPengajar);
        btnForgotPassword = (Button) findViewById(R.id.btnForgotPassword);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        if (session.isLoggedIn()) {
            HashMap<String, String> user = db.getUserDetails();

            String status = user.get("status");
            if (status.equalsIgnoreCase("admin")){
                Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
            }else if (status.equalsIgnoreCase("pengajar")){
                Intent intent = new Intent(LoginActivity.this, PengajarActivity.class);
                startActivity(intent);
                finish();
            }else{
                Intent intent = new Intent(LoginActivity.this, UserActivity.class);
                startActivity(intent);
                finish();
            }
        }

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

        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });

        btnLoginPengajar.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginPengajar.class);
                startActivity(i);
                finish();
            }
        });

        btnForgotPassword.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        ForgotPassword.class);
                startActivity(i);
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

        StringRequest strReq = new StringRequest(Method.POST,
                Konfigurasi.URL_LOGIN_USER, new Response.Listener<String>() {

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
                        String name = user.getString(User.KEY_NAME);
                        String email = user.getString(User.KEY_EMAIL);
                        String phone = user.getString(User.KEY_PHONE);
                        String gender = user.getString(User.KEY_GENDER);
                        String address = user.getString(User.KEY_ADDRESS);
                        String created_at = user.getString(User.KEY_CREATED);
                        String status = user.getString(User.KEY_STATUS);

                        db.addUser(name, email, phone, gender, address, uid, created_at, status);

                        if(status.equalsIgnoreCase("admin")){
                            Intent intent = new Intent(LoginActivity.this,
                                    AdminActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            Intent intent = new Intent(LoginActivity.this,
                                    UserActivity.class);
                            startActivity(intent);
                            finish();
                        }
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