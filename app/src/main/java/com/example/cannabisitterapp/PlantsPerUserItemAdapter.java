package com.example.cannabisitterapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

import static com.example.cannabisitterapp.MainActivity.getNameByPlantId;

/**
 * Created by noamg on 17/09/2017.
 */

public class PlantsPerUserItemAdapter extends ArrayAdapter<PlantsPerUserItem> {

    /**
     * Adapter context
     */
    Context mContext;

    /**
     * Adapter View layout
     */
    int mLayoutResourceId;

    public PlantsPerUserItemAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);
        mContext = context;
        mLayoutResourceId = layoutResourceId;
    }

    /**
     * Returns the view for a specific item on the list
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        final PlantsPerUserItem currentItem = getItem(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
        }

        row.setTag(currentItem);
        final TextView textView = (TextView) row.findViewById(R.id.plantText);
        if (currentItem != null) {
            //textView.setText(String.valueOf(currentItem.getPlantId()));
            String s = MainActivity.getNameByPlantId(currentItem.getPlantId());
            textView.setText(MainActivity.getNameByPlantId(currentItem.getPlantId()));
        }

        return row;
    }



}


