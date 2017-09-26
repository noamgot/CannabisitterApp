package com.example.cannabisitterapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.vision.barcode.Barcode;
import com.example.cannabisitterapp.barcode.BarcodeCaptureActivity;


import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.squareup.okhttp.OkHttpClient;


public class AddPlantActivity extends AppCompatActivity {
    private static final String LOG_TAG = AddPlantActivity.class.getSimpleName();
    private static final int BARCODE_READER_REQUEST_CODE = 1;

    private TextView mResultTextView;

    private MobileServiceClient mClient;
    private MobileServiceTable<PlantsPerUserItem> mPlantsPerUserTable;
    private int mUserId;
    private int mPlantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);

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

        Intent intent = getIntent();
        mUserId = intent.getIntExtra(MainActivity.USER_ID_KEY, -1);
        if (mUserId == -1) {
            throw new RuntimeExecutionException(new Throwable("Invalid User ID"));
        }

        mPlantsPerUserTable = mClient.getTable("GHPlantsPerUser", PlantsPerUserItem.class);

        mResultTextView = (TextView) findViewById(R.id.result_textview);

        Button scanBarcodeButton = (Button) findViewById(R.id.scan_barcode_button);
        scanBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Point[] p = barcode.cornerPoints;
                    String plantIdStr = barcode.displayValue;
                    if (tryParseInt(plantIdStr)){
                        mPlantId = Integer.parseInt(plantIdStr);
                        addItem();

                    } else {
                        mResultTextView.setText(R.string.invalid_qr_code);
                    }
                    //mResultTextView.setText(barcode.displayValue);
                } else {
                    mResultTextView.setText(R.string.no_barcode_captured);
                }
            } else Log.e(LOG_TAG, String.format(getString(R.string.barcode_error_format),
                    CommonStatusCodes.getStatusCodeString(resultCode)));
        } else super.onActivityResult(requestCode, resultCode, data);
    }

    boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    /**
     * Add a new item
     *
     *            The view that originated the call
     */
    public void addItem() {
        if (mClient == null) {
            return;
        }

        // Create a new item
        final PlantsPerUserItem item = new PlantsPerUserItem();
        item.setUserId(mUserId);
        item.setPlantId(mPlantId);

        final String plantName = MainActivity.getNameByPlantId(mPlantId);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        // Insert the new item
                        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    if (plantIsInList()){

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getBaseContext(), "This plant is already in your list.\nTry another plant", Toast.LENGTH_LONG).show();
                                            }
                                        });


                                    } else {
                                        addItemInTable(item);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getBaseContext(), plantName + " was added successfully to your plants list", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        AddPlantActivity.this.finish();
                                    }
                                } catch (final Exception e) {
                                    createAndShowDialogFromTask(e, "Error");
                                }
                                return null;
                            }
                        };

                        runAsyncTask(task);

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(AddPlantActivity.this);
        builder.setTitle(plantName);
        builder.setMessage("Would you like to add " + plantName + " to your plants list?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();


    }

    private boolean plantIsInList() throws ExecutionException, InterruptedException {
        List<PlantsPerUserItem> lst = mPlantsPerUserTable.where().field("UserID").eq(mUserId).
                and().field("PlantID").eq(mPlantId).execute().get();

        return !lst.isEmpty();
    }

    /**
     * Add an item to the Mobile Service Table
     *
     * @param item
     *            The item to Add
     */
    public PlantsPerUserItem addItemInTable(PlantsPerUserItem item) throws Exception {
        PlantsPerUserItem entity = mPlantsPerUserTable.insert(item).get();
        return entity;
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