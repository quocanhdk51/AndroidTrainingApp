package com.example.s3727634.afinal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class HistoryListAdapter extends ArrayAdapter<Stats> {

    private static  final String TAG = "HistoryListAdapter";

    private Context mContext;
    int mResource;

    public HistoryListAdapter(@NonNull Context context, int resource, @NonNull List<Stats> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //get info from item

        String date = getItem(position).getDate();
        String words = getItem(position).getWords();
        String steps = getItem(position).getSteps();
        String locations = getItem(position).getLocations();

        //Create the Stats object with the info (what for? i'll have to check again)
        //Stats stats = new Stats(date, words, steps, locations);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvDate = (TextView) convertView.findViewById(R.id.textView1);
        TextView tvWords = (TextView) convertView.findViewById(R.id.textView2);
        TextView tvSteps = (TextView) convertView.findViewById(R.id.textView3);
        TextView tvLocations = (TextView) convertView.findViewById(R.id.textView4);

        tvDate.setText(date);
        tvWords.setText(words);
        tvSteps.setText(steps);
        tvLocations.setText(locations);

        return convertView;

    }
}

