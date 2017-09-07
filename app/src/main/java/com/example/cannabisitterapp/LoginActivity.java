package com.example.cannabisitterapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.Bind;

//import java.net.MalformedURLException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.os.AsyncTask;
//import android.os.Build;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.ListView;
//import android.widget.ProgressBar;
//
//import com.google.common.util.concurrent.FutureCallback;
//import com.google.common.util.concurrent.Futures;
//import com.google.common.util.concurrent.ListenableFuture;
//import com.google.common.util.concurrent.SettableFuture;
//import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
//import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
//import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
//import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
//import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
//import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
//import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
//import com.microsoft.windowsazure.mobileservices.table.query.Query;
//import com.microsoft.windowsazure.mobileservices.table.query.QueryOperations;
//import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
//import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
//import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
//import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
//import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
//import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
//import com.squareup.okhttp.OkHttpClient;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @Bind(R.id.input_username) EditText mUsernameText;
    @Bind(R.id.input_password) EditText mPasswordText;
    @Bind(R.id.btn_login) Button mLoginButton;
    @Bind(R.id.link_signup) TextView mSignupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mLoginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        mSignupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        mLoginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = mUsernameText.getText().toString();
        String password = mPasswordText.getText().toString();

        // TODO: Implement your own authentication logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        onLoginSuccess();
                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        mLoginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        mLoginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String username = mUsernameText.getText().toString();
        String password = mPasswordText.getText().toString();

        if (username.isEmpty() ) {
            mUsernameText.setError("enter a valid username");
            valid = false;
        } else {
            mUsernameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPasswordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            mPasswordText.setError(null);
        }

        return valid;
    }
}