package com.example.cannabisitterapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;


public class MainActivity extends AppCompatActivity {

    public static int DUMMY_USERID = 1;
    public static final String PLANT_NAME_KEY = "com.example.cannabisitterapp.PLANT_NAME_KEY";
    private ProgressBar mSpinner;

    private int mUserId = DUMMY_USERID;

    /**
     * Mobile Service Client reference
     */
    private MobileServiceClient mClient;

    /**
     * Mobile Service Table used to access data
     */
    private MobileServiceTable<PlantsPerUserItem> mPlantsPerUserTable;

    /**
     * Adapter to sync the items list with the view
     */
    private PlantsPerUserItemAdapter mAdapter;

    @Bind(R.id.add_plant_btn) Button mAddPlantBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        try {
            // Create the Mobile Service Client instance, using the provided

            // Mobile Service URL and key
            AzureServiceAdapter.Initialize(this);
            AzureServiceAdapter azureServiceAdapter = AzureServiceAdapter.getInstance();
            mClient = azureServiceAdapter.getClient();
//            mClient = new MobileServiceClient(
//                    "https://cannabisitterapp.azurewebsites.net",
//                    this);//.withFilter(new ProgressFilter());

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

            // Get the Mobile Service Table instance to use
            mPlantsPerUserTable = mClient.getTable("GHPlantsPerUser", PlantsPerUserItem.class);

            // Offline Sync
            //mToDoTable = mClient.getSyncTable("ToDoItem", ToDoItem.class);

            //Init local storage
           // initLocalStore().get();

            //mTextNewToDo = (EditText) findViewById(R.id.textNewToDo);

            mSpinner = (ProgressBar)findViewById(R.id.spinnerProgressBar);

            // Create an mAdapter to bind the items with the view
            mAdapter = new PlantsPerUserItemAdapter(this, R.layout.row_list_plant_item);
            ListView listViewPlantItem = (ListView) findViewById(R.id.plantsList);
            listViewPlantItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    PlantsPerUserItem item = (PlantsPerUserItem) parent.getAdapter().getItem(position);
                    // todo - fix this so it will pass a name and not a number!
                    String plantName = Integer.toString(item.getPlantId());
                    Intent intent = new Intent(getApplicationContext(), PlantStatsActivity.class);
                    intent.putExtra(PLANT_NAME_KEY, plantName);
                    startActivity(intent);
                }
            });
            listViewPlantItem.setAdapter(mAdapter);

//            if (mListView == null) {
//                mListView = (ListView) findViewById(R.id.plantsList);
//            }

//            mAdapter = new ArrayAdapter<String>(this,
//                    android.R.layout.simple_list_item_1,
//                    mPlantsList);
//            setListAdapter(mAdapter);

            // Load the items from the Mobile Service
            refreshItemsFromTable();


        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e){
            createAndShowDialog(e, "Error");
        }

//        if (mListView == null) {
//            mListView = (ListView) findViewById(R.id.plantsList);
//        }
//
//        mAdapter=new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1,
//                mPlantsList);
//        setListAdapter(mAdapter);


        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //todo - uncomment this to enable login/sign-up!!!
//        Intent intent = new Intent(this, LoginActivity.class);
//        startActivity(intent);

        // Login ended!


    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshItemsFromTable();
    }

    /**
     * Refresh the list with the items in the Table
     */
    private void refreshItemsFromTable() {

        mSpinner.setVisibility(View.VISIBLE);
        // Get the items and add them in the mAdapter

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<PlantsPerUserItem> results = refreshItemsFromMobileServiceTable();

                    //Offline Sync
                    //final List<ToDoItem> results = refreshItemsFromMobileServiceTableSyncTable();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.clear();

                            for (PlantsPerUserItem item : results) {
                                mAdapter.add(item);
                            }
                        }
                    });
                } catch (final Exception e){
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mSpinner.setVisibility(View.GONE);
            }
        };

        runAsyncTask(task);
    }

    private List<PlantsPerUserItem> refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException, MobileServiceException {
       return mPlantsPerUserTable.where().field("UserID").eq(mUserId).execute().get();
    }


    public void addPlant(View view) {

        //todo - user approaval dialog - to be used somewhere...
//        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                switch (which){
//                    case DialogInterface.BUTTON_POSITIVE:
//                        //Yes button clicked
//                        break;
//
//                    case DialogInterface.BUTTON_NEGATIVE:
//                        //No button clicked
//                        break;
//                }
//            }
//        };
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
//        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
//                .setNegativeButton("No", dialogClickListener).show();

        Intent intent = new Intent(this, AddPlantActivity.class);
        startActivity(intent);
        //finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


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

//    private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {
//
//        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//
//                    MobileServiceSyncContext syncContext = mClient.getSyncContext();
//
//                    if (syncContext.isInitialized())
//                        return null;
//
//                    SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "OfflineStore", null, 1);
//
//                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
//                    tableDefinition.put("id", ColumnDataType.String);
//                    tableDefinition.put("plantId", ColumnDataType.Integer);
//                    tableDefinition.put("name", ColumnDataType.String);
//
//                    localStore.defineTable("PlantTableItem", tableDefinition);
//
//                    SimpleSyncHandler handler = new SimpleSyncHandler();
//
//                    syncContext.initialize(localStore, handler).get();
//
//                } catch (final Exception e) {
//                    createAndShowDialogFromTask(e, "Error");
//                }
//
//                return null;
//            }
//        };
//
//        return runAsyncTask(task);
//    }

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

//    private class ProgressFilter implements ServiceFilter {
//
//        @Override
//        public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {
//
//            final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();
//
//
//            runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    //if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.VISIBLE);
//                }
//            });
//
//            ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);
//
//            Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
//                @Override
//                public void onFailure(Throwable e) {
//                    resultFuture.setException(e);
//                }
//
//                @Override
//                public void onSuccess(ServiceFilterResponse response) {
//                    runOnUiThread(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            //if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.GONE);
//                        }
//                    });
//
//                    resultFuture.set(response);
//                }
//            });
//
//            return resultFuture;
//        }
//    }
}
