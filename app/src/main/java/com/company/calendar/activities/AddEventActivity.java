package com.company.calendar.activities;

import java.util.Calendar;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.company.calendar.R;

import java.util.Date;

/**
 * Created by abdul on 17-Jun-17.
 */

public class AddEventActivity extends AppCompatActivity{

    private Calendar rightNow;

    private int hour = 0;
    private int minutes = 0;

    private int day = 0;
    private int month = 0;
    private int year = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        rightNow = Calendar.getInstance();
    }

    private void setDateTimeVariablesToCurrent() {
        hour = rightNow.get(Calendar.HOUR_OF_DAY);
        minutes = rightNow.get(Calendar.MINUTE);
        day = rightNow.get(Calendar.DAY_OF_MONTH);
        month = rightNow.get(Calendar.MONTH);
        year = rightNow.get(Calendar.YEAR);
    }

    private void setDateTimeVariables(DatePicker date, TimePicker time) {
        //hour = time.get();
        //minutes = time.getMinute();
    }

    private void displayDateTimeDialog () {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle("Select Date and Time")
                .setView(R.layout.dialog_datetime_picker)
                .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
    }

}
