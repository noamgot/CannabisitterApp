package com.example.cannabisitterapp;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;

public class PlantStatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_stats);
        ButterKnife.bind(this);

        Intent intent =  getIntent();
        String plantName = intent.getStringExtra(MainActivity.PLANT_NAME_KEY);
        TextView plantNameTextView = (TextView) findViewById(R.id.plantNameTextView);
        plantNameTextView.setText(plantName);

        TextView irrigationTextView = (TextView) findViewById(R.id.lastIrrigationTextView);
        //todo - change this!!!
        Date date = Calendar.getInstance().getTime();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        String msg = String.format(getResources().getString(R.string.last_irrigation), df.format(date));
        irrigationTextView.setText(msg);


        GraphView graph = (GraphView) findViewById(R.id.soilMoistureGraph);
        graph.getViewport().setScalable(true);
        graph.getGridLabelRenderer().setVerticalAxisTitleColor(Color.BLACK);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);
        graph.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.BLACK);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6),

        });
        graph.addSeries(series);

        GraphView graph1 = (GraphView) findViewById(R.id.temperatureGraph);
        graph1.getViewport().setScalable(true);
        graph1.getGridLabelRenderer().setVerticalAxisTitleColor(Color.BLACK);
        graph1.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);
        graph1.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.BLACK);
        graph1.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);
        LineGraphSeries<DataPoint> series1 = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6),

        });
        graph1.addSeries(series1);
    }
}
