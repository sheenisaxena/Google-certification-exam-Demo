package com.google.developer.taskmaker;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.developer.taskmaker.data.DatabaseContract;
import com.google.developer.taskmaker.data.DatabaseContract.TaskColumns;
import com.google.developer.taskmaker.data.TaskAdapter;
import com.google.developer.taskmaker.data.TaskUpdateService;

public class MainActivity extends AppCompatActivity implements
        TaskAdapter.OnItemClickListener,
        View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private TaskAdapter mAdapter;
    RecyclerView recyclerView;
    String currValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        try {
            currValue = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_sortBy_key), "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // get loader manager object
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Click events in Floating Action Button */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, AddTaskActivity.class);
        startActivity(intent);
    }

    /* Click events in RecyclerView items */
    @Override
    public void onItemClick(View v, int position) {
        //TODO: Handle list item click event

    }

    /* Click events on RecyclerView item checkboxes */
    @Override
    public void onItemToggled(boolean active, int position) {
        //TODO: Handle task item checkbox event
        ContentValues values;
        values = new ContentValues();
        values.put(TaskColumns.DESCRIPTION, mAdapter.getItem(position).description);
        values.put(TaskColumns.IS_PRIORITY, mAdapter.getItem(position).isPriority);
        values.put(TaskColumns.IS_COMPLETE, active ? 1 : 0);
        values.put(TaskColumns.DUE_DATE, mAdapter.getItem(position).dueDateMillis);

        TaskUpdateService.updateTask(getApplicationContext(), Uri.withAppendedPath(DatabaseContract.CONTENT_URI, String.valueOf(mAdapter.getItem(position).id)), values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection =
                {
                        TaskColumns._ID,
                        TaskColumns.DESCRIPTION,
                        TaskColumns.IS_COMPLETE,
                        TaskColumns.IS_PRIORITY,
                        TaskColumns.DUE_DATE
                };


        if (currValue.equalsIgnoreCase("default") || currValue.equalsIgnoreCase("")) {
            return new CursorLoader(this, DatabaseContract.CONTENT_URI, projection, null, null, "is_priority desc,due_date asc,is_complete asc");
        } else {
            return new CursorLoader(this, DatabaseContract.CONTENT_URI, projection, null, null, "due_date asc,is_priority desc,is_complete asc");
        }
//        return new CursorLoader(this, DatabaseContract.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter = new TaskAdapter(data);
        mAdapter.setOnItemClickListener(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
