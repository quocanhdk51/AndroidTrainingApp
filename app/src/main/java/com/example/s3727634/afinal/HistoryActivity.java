package com.example.s3727634.afinal;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryListActivity";

    DatabaseHelper mDatabaseHelper;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_activity);
        mListView = findViewById(R.id.listView);
        mDatabaseHelper = new DatabaseHelper(this);
//        AddData("19-Jan-2019", "3", "500", "2");
//        AddData("18-Jan-2019", "3", "400", "1");
//        AddData("17-Jan-2019", "1", "50", "0");
        populateListView();
    }

    public void AddData(String nDate, String nWords, String nSteps, String nLoc){
        boolean insertData = mDatabaseHelper.addData(nDate, nWords, nSteps, nLoc);

    }

    private void populateListView(){
        Cursor data = mDatabaseHelper.getData();
        ArrayList<Stats> listData = new ArrayList<>();
        Log.d(TAG, "Setting up the buffer array.");
        //data.moveToPosition(0);
        while(data.moveToNext()){
            Stats temp = new Stats("date", "words", "steps", "locations");
            temp.setDate(data.getString(0));
            temp.setWords(data.getString(1));
            temp.setSteps(data.getString(2));
            temp.setLocations(data.getString(3));
            listData.add(temp);
            Log.d(TAG, "Row: " + data.getPosition() + "successfully added.");
        }
        data.close();
        Log.d(TAG, "Checking the adapter.");
        HistoryListAdapter adapter = new HistoryListAdapter(this, R.layout.adapter_view_layout, listData);
        Log.d(TAG, "Adapter created successfully.");
        mListView.setAdapter(adapter);
        Log.d(TAG, "Adapter displayed successfully.");

    }

    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
