package com.google.developer.taskmaker;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.developer.taskmaker.data.DatabaseContract.TaskColumns;
import com.google.developer.taskmaker.data.TaskUpdateService;
import com.google.developer.taskmaker.view.DatePickerFragment;

import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener,
        View.OnClickListener {

    //Selected due date, stored as a timestamp
    private long mDueDate = Long.MAX_VALUE;

    private TextInputEditText mDescriptionView;
    private SwitchCompat mPrioritySelect;
    private TextView mDueDateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        mDescriptionView = (TextInputEditText) findViewById(R.id.text_input_description);
        mPrioritySelect = (SwitchCompat) findViewById(R.id.switch_priority);
        mDueDateView = (TextView) findViewById(R.id.text_date);

        mDueDateView.setOnClickListener(this);
        updateDateDisplay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_save) {
            saveItem();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /* Manage the selected date value */
    public void setDateSelection(long selectedTimestamp) {
        mDueDate = selectedTimestamp;
        updateDateDisplay();
    }

    public long getDateSelection() {
        return mDueDate;
    }

    /* Click events on Due Date */
    @Override
    public void onClick(View v) {
        DatePickerFragment dialogFragment = new DatePickerFragment();
        dialogFragment.show(getSupportFragmentManager(), "datePicker");
    }

    /* Date set events from dialog */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        //Set to noon on the selected day
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        setDateSelection(c.getTimeInMillis());
    }

    private void updateDateDisplay() {
        if (getDateSelection() == Long.MAX_VALUE) {
            mDueDateView.setText(R.string.date_empty);
        } else {
            CharSequence formatted = DateFormat.format("MM/dd/yyyy", mDueDate);
            mDueDateView.setText(formatted);
        }
    }

    private void saveItem() {
        //Insert a new item
        ContentValues values = new ContentValues(4);
        values.put(TaskColumns.DESCRIPTION, mDescriptionView.getText().toString());
        values.put(TaskColumns.IS_PRIORITY, mPrioritySelect.isChecked() ? 1 : 0);
        values.put(TaskColumns.IS_COMPLETE, 0);
        values.put(TaskColumns.DUE_DATE, getDateSelection());

        TaskUpdateService.insertNewTask(this, values);
        finish();
    }

}
