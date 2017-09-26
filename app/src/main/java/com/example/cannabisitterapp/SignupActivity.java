package com.example.cannabisitterapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.squareup.okhttp.OkHttpClient;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.Bind;

import static java.util.Collections.max;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private MobileServiceClient mClient;
    private MobileServiceTable<UserItem> mUsersTable;

    @Bind(R.id.input_name) EditText mNameText;
    @Bind(R.id.input_username) EditText mUsernameText;
    @Bind(R.id.input_password) EditText mPasswordText;
    @Bind(R.id.input_reEnterPassword) EditText mReEnterPasswordText;
    @Bind(R.id.btn_signup) Button mSignupButton;
    @Bind(R.id.link_login) TextView mLoginLink;
    private String mUserName;
    private int mUserId;
    private String mName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        AzureServiceAdapter azureServiceAdapter = AzureServiceAdapter.getInstance();
        mClient = azureServiceAdapter.getClient();

        // Extend timeout from default of 10s to 20s
        mClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
            @Override
            public OkHttpClient createOkHttpClient() {
                OkHttpClient client = new OkHttpClient();
                client.setReadTimeout(20, TimeUnit.SECONDS);
                client.setWriteTimeout(20, TimeUnit.SECONDS);
                return client;
            }
        });

        mUsersTable = mClient.getTable("GHUsers", UserItem.class);

        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    signup();
                } catch (InterruptedException | ExecutionException | MobileServiceException e) {
                    e.printStackTrace();
                }
            }
        });

        mLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void signup() throws InterruptedException, ExecutionException, MobileServiceException {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        if (mClient == null) {
            return;
        }

        mSignupButton.setEnabled(false);

        mName = mNameText.getText().toString();
        mUserName = mUsernameText.getText().toString();
        final String password = mPasswordText.getText().toString();

        // TODO: Implement your own signup logic here.

        final UserItem newUser = new UserItem();
        newUser.setName(mName);
        newUser.setUser(mUserName);
        newUser.setPassword(password);


        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){

            ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this, R.style.AppTheme_Dark_Dialog);

            @Override
            protected void onPreExecute() {
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Creating Account...");
                progressDialog.show();
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params) {

                try {

                    if (isAvilableUsername(mUserName)){ // new username!
                        //loginSuccess = true;
                        mUserId = getHighestUserId() + 1;
                        newUser.setUserId(mUserId);
                        addNewUser(newUser);
                        onSignupSuccess();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onSignupFailed("Username already exists. Please choose another username");
                            }
                        });
                    }
                } catch (final Exception e){
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mSignupButton.setEnabled(true);
                progressDialog.dismiss();
                super.onPostExecute(aVoid);

            }

        };

        runAsyncTask(task);

    }

    private UserItem addNewUser(UserItem newUser) throws Exception {
        UserItem entity =  mUsersTable.insert(newUser).get();
        return entity;

    }


    private int getHighestUserId() throws MobileServiceException, ExecutionException, InterruptedException {
        List<UserItem> lst =  mUsersTable.execute().get();
        int maxID = -1;
        int userID;
        for (UserItem user : lst){
            userID = user.getUserId();
            if (userID > maxID){
                maxID = userID;
            }
        }
        return maxID;    }

    private boolean isAvilableUsername(String userName) throws ExecutionException, InterruptedException {
        final List<UserItem> usersTable = mUsersTable.where().field("User").eq(userName).execute().get();
        return usersTable.size() == 0; // if the list is empty then this username is a new one
    }


    public void onSignupSuccess() {
        Intent resultData = new Intent();
        resultData.putExtra(MainActivity.USER_ID_KEY, mUserId);
        resultData.putExtra(MainActivity.NAME_KEY, mName);
        setResult(Activity.RESULT_OK, resultData);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Signup failed", Toast.LENGTH_LONG).show();
    }

    public void onSignupFailed(String reason) {
        Toast.makeText(getBaseContext(), "Signup failed: " + reason, Toast.LENGTH_LONG).show();
    }

    public boolean validate() {
        boolean valid = true;

        String name = mNameText.getText().toString();
        String username = mUsernameText.getText().toString();
        String password = mPasswordText.getText().toString();
        String reEnterPassword = mReEnterPasswordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            mNameText.setError("at least 3 characters");
            valid = false;
        } else {
            mNameText.setError(null);
        }

        if (username.isEmpty()) {
            mUsernameText.setError("Invalid Name - Enter Valid Username");
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

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            mReEnterPasswordText.setError("Password Do not match");
            valid = false;
        } else {
            mReEnterPasswordText.setError(null);
        }

        return valid;
    }

    /**
     * Creates a dialog and shows it
     *
     * @param exception
     *            The exception to show in the dialog
     * @param title
     *            The dialog title
     */
    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    /**
     * Creates a dialog and shows it
     *
     * @param message
     *            The dialog message
     * @param title
     *            The dialog title
     */
    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    /**
     * Run an ASync task on the corresponding executor
     * @param task
     * @return
     */
    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });
    }
}