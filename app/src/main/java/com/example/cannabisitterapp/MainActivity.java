package com.example.cannabisitterapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.LruCache;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.squareup.okhttp.OkHttpClient;


public class MainActivity extends AppCompatActivity {


    public static int DUMMY_USERID = 1;
    private static final int LRU_CACHE_SIZE = 256;
    public static final String PLANT_NAME_KEY = "com.example.cannabisitterapp.PLANT_NAME_KEY";
    public static final String NAME_KEY = "com.example.cannabisitterapp.NAME_KEY";
    public static final String USER_ID_KEY = "com.example.cannabisitterapp.USER_ID_KEY";
    public static final String PLANT_ID_KEY = "com.example.cannabisitterapp.PLANT_ID_KEY";
    public static final String PLANT_UNIQUE_ID_KEY = "com.example.cannabisitterapp.PLANT_UNIQUE_ID_KEY";

    private ProgressBar mSpinner;

    private static final int LOGIN_REQUEST_CODE = 0;

    private boolean initialized = false;

    private int mUserId = DUMMY_USERID;
    private boolean firstCacheUpdate = true;

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

    private MobileServiceTable<PlantItem> mPlantsTable;

    private static LruCache<Integer, String> mPlantsIdCache;


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

            mPlantsIdCache = new LruCache<Integer, String>(LRU_CACHE_SIZE);

            // Get the Mobile Service Table instance to use
            mPlantsPerUserTable = mClient.getTable("GHPlantsPerUser", PlantsPerUserItem.class);

            mPlantsTable = mClient.getTable("GHPlants", PlantItem.class);

            mSpinner = (ProgressBar)findViewById(R.id.spinnerProgressBar);

            // Create an mAdapter to bind the items with the view
            mAdapter = new PlantsPerUserItemAdapter(this, R.layout.row_list_plant_item);
            ListView listViewPlantItem = (ListView) findViewById(R.id.plantsList);
            listViewPlantItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    PlantsPerUserItem item = (PlantsPerUserItem) parent.getAdapter().getItem(position);
                    String plantName = getNameByPlantId(item.getPlantId());
                    Intent intent = new Intent(getApplicationContext(), PlantStatsActivity.class);
                    intent.putExtra(PLANT_NAME_KEY, plantName);
                    intent.putExtra(USER_ID_KEY, mUserId);
                    intent.putExtra(PLANT_ID_KEY, item.getPlantId());
                    intent.putExtra(PLANT_UNIQUE_ID_KEY, item.getId());
                    startActivity(intent);
                }
            });
            listViewPlantItem.setAdapter(mAdapter);

        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e){
            createAndShowDialog(e, "Error");
        }

        //todo - uncomment this to enable login/sign-up!!!
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_REQUEST_CODE);

        // Login ended!


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == LOGIN_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK) {

                try {
                    mUserId = data.getIntExtra(USER_ID_KEY, -1);
                    if (mUserId == -1) {
                        throw new Exception("Invalid user ID");
                    }
                    String name = data.getStringExtra(NAME_KEY);

                    TextView helloUserTextView = (TextView)findViewById(R.id.helloUserTextView);
                    helloUserTextView.setText(String.format(getResources().getString(R.string.hello_user), name));

                    // Load the items from the Mobile Service
                    refreshItemsFromTable();
                    initialized = true;


                } catch (MalformedURLException e) {
                    createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
                } catch (Exception e){
                    createAndShowDialog(e, "Error");
                }


            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (initialized) {
            refreshItemsFromTable();
        }
    }

    public static String getNameByPlantId(int plantId) {
        return mPlantsIdCache.get(plantId);
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
                    if (firstCacheUpdate) {
                        final List<PlantItem> PlantsTableFromDB = mPlantsTable.execute().get();
                        for (PlantItem plant : PlantsTableFromDB) {
                            mPlantsIdCache.put(plant.getPlantId(), plant.getPlantName());

                        }
                        firstCacheUpdate = false;
                    }

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


        Intent intent = new Intent(this, AddPlantActivity.class);
        intent.putExtra(USER_ID_KEY, mUserId);
        startActivity(intent);
        //finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
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

    public void refreshPlantsList(View view) {
        refreshItemsFromTable();
    }

    public void signOut(View view) {
        mUserId = -1;
        initialized = false;
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_REQUEST_CODE);
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
