package com.example.cannabisitterapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.tasks.RuntimeExecutionException;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.DateTimeOffset;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOrder;
import com.squareup.okhttp.OkHttpClient;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;

import static com.example.cannabisitterapp.R.id.temperatureGraph;

public class PlantStatsActivity extends AppCompatActivity {

    private MobileServiceClient mClient;

    private MobileServiceTable<LogsPerUserItem> mStatsTable;
    private int mPlantId;
    private int mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_stats);
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

        Intent intent =  getIntent();
        String plantName = intent.getStringExtra(MainActivity.PLANT_NAME_KEY);
        TextView plantNameTextView = (TextView) findViewById(R.id.plantNameTextView);
        plantNameTextView.setText(plantName);

        mUserId = intent.getIntExtra(MainActivity.USER_ID_KEY, -1);
        if (mUserId == -1) {
            throw new RuntimeExecutionException(new Throwable("Invalid User ID"));
        }
        mPlantId = intent.getIntExtra(MainActivity.PLANT_ID_KEY, -1);
        if (mPlantId == -1) {
            throw new RuntimeExecutionException(new Throwable("Invalid Plant ID"));
        }

        mStatsTable = mClient.getTable("GHLogsPerUser", LogsPerUserItem.class);


        fillGraphs();

    }

    private void fillGraphs() {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {

                try {

                    final List<LogsPerUserItem> results = getLogsFromStatsTable();
                    final String msg = getLastIrrigationDate();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if(results.size() > 0) {

                                DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);

                                DataPoint[] humidityMeasurements = new DataPoint[results.size()];
                                DataPoint[] temperatureMeasurements = new DataPoint[results.size()];
                                for (int i = 0; i < results.size(); i++) {
                                    LogsPerUserItem currentItem = results.get(i);
                                    humidityMeasurements[i] = new DataPoint(currentItem.getMessageDate(), currentItem.getHumidity() / 1023 * 100);
                                    temperatureMeasurements[i] = new DataPoint(currentItem.getMessageDate(), currentItem.getTemperature());
                                }

                                LineGraphSeries<DataPoint> humiditySeries = new LineGraphSeries<DataPoint>(humidityMeasurements);
                                LineGraphSeries<DataPoint> temperatureSeries = new LineGraphSeries<DataPoint>(temperatureMeasurements);

                                GraphView humidityGraph = (GraphView) findViewById(R.id.soilMoistureGraph);
                                setGraph(humidityGraph, humiditySeries, results);

                                GraphView temperatureGraph = (GraphView) findViewById(R.id.temperatureGraph);
                                setGraph(temperatureGraph, temperatureSeries, results);
                            }

                            // update last irrigation:
                            TextView irrigationTextView = (TextView) findViewById(R.id.lastIrrigationTextView);
                            irrigationTextView.setText(msg);
                        }
                    });

                } catch (final Exception e){
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }

        };

        runAsyncTask(task);
    }

    private void setGraph(GraphView graph, LineGraphSeries<DataPoint> series, List<LogsPerUserItem> results){

        DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
        //graph.getViewport().setScalable(true);
        //graph.getViewport().setScalableY(true);
        //graph.getViewport().setScrollable(true);
        //graph.getViewport().setScrollableY(true);
        graph.getGridLabelRenderer().setVerticalAxisTitleColor(Color.BLACK);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);
        graph.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.BLACK);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);

        graph.addSeries(series);

        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(PlantStatsActivity.this, df));
        graph.getGridLabelRenderer().setNumHorizontalLabels(5); // only 4 because of the space

        // set manual x bounds to have nice steps
        graph.getViewport().setMinX(results.get(0).getMessageDate().getTime());
        graph.getViewport().setMaxX(results.get(results.size() - 1).getMessageDate().getTime());
        graph.getViewport().setXAxisBoundsManual(true);
//
//                                graph.getViewport().setYAxisBoundsManual(true);
//                                graph.getViewport().setMinY(0);
//                                graph.getViewport().setMaxY(5);

        // as we use dates as labels, the human rounding to nice readable numbers
        // is not necessary
        //graph.getGridLabelRenderer().setHumanRounding(false);

    }


    private String getLastIrrigationDate() throws ExecutionException, InterruptedException {
        List<LogsPerUserItem> lst =  mStatsTable.where().field("UserID").eq(mUserId).
                and().field("PlantID").eq(mPlantId).
                and().field("didWater").gt(0).
                orderBy("createdAt", QueryOrder.Ascending).
                execute().get();

        String msg;
        if (lst.isEmpty()){
            msg =  "No last irrigation date available";

        } else {
            Date lastIrrigationDate = lst.get(lst.size() - 1).getMessageDate();
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            msg = String.format(getResources().getString(R.string.last_irrigation), df.format(lastIrrigationDate));
        }
        return msg;
    }

    private List<LogsPerUserItem> getLogsFromStatsTable() throws ExecutionException, InterruptedException {

        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DATE, -1);
        Date yestarday = cal.getTime();
        //DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

        List<LogsPerUserItem> lst =  mStatsTable.where().field("UserID").eq(mUserId).
                and().field("PlantID").eq(mPlantId).
                and().field("createdAt").ge(yestarday).
                and().field("Humidity").le(1023).
                and().field("Tempreture").lt(1000).
                orderBy("createdAt", QueryOrder.Ascending).
                execute().get();
        return lst;


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
