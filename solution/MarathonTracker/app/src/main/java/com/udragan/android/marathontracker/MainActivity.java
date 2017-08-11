package com.udragan.android.marathontracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.udragan.android.marathontracker.adapters.CheckpointAdapter;
import com.udragan.android.marathontracker.models.CheckpointModel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // members **********************************************************************************************************

    private RecyclerView mCheckpointsRecyclerView;
    private CheckpointAdapter mCheckpointAdapter;

    // AppCompatActivity ************************************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar appBar = (Toolbar) findViewById(R.id.toolbar_main_activity);
        setSupportActionBar(appBar);

        // test data //
        ArrayList<CheckpointModel> testDataCheckpoints = new ArrayList<>(4);
        testDataCheckpoints.add(new CheckpointModel(16, 45));
        testDataCheckpoints.add(new CheckpointModel(19, 45));
        testDataCheckpoints.add(new CheckpointModel(16, 45));
        testDataCheckpoints.add(new CheckpointModel(19, 45));
        ///////////////

        mCheckpointAdapter = new CheckpointAdapter(this, testDataCheckpoints);
        mCheckpointsRecyclerView = (RecyclerView) findViewById(R.id.checkpoints_recycler_view_main_activity);
        mCheckpointsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCheckpointsRecyclerView.setAdapter(mCheckpointAdapter);
    }
}
