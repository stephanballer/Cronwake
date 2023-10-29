package com.jalopi.cronwake;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

public class AlarmEditActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_edit);
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.purple_700)));
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton doneButton = findViewById(R.id.doneButton);
        FloatingActionButton removeButton = findViewById(R.id.removeButton);

        String alarmString = getIntent().getStringExtra("alarm");
        AlarmData alarm = AlarmData.parseAlarmData(alarmString);
        if (alarm == null) {
            Calendar calendar = Calendar.getInstance();
            alarm = new AlarmData("New Alarm", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), new boolean[]{true,true,true,true,true,true,true});
            removeButton.setVisibility(View.INVISIBLE);
        }

        ((TimePicker) findViewById(R.id.timeEditView)).setIs24HourView(DateFormat.is24HourFormat(this));
        ((TextView) findViewById(R.id.nameEditView)).setText(alarm.getName());
        ((TimePicker) findViewById(R.id.timeEditView)).setHour(alarm.getHour());
        ((TimePicker) findViewById(R.id.timeEditView)).setMinute(alarm.getMinute());
        ((ToggleButton) findViewById(R.id.moButton)).setChecked(alarm.getDays()[0]);
        ((ToggleButton) findViewById(R.id.tuButton)).setChecked(alarm.getDays()[1]);
        ((ToggleButton) findViewById(R.id.weButton)).setChecked(alarm.getDays()[2]);
        ((ToggleButton) findViewById(R.id.thButton)).setChecked(alarm.getDays()[3]);
        ((ToggleButton) findViewById(R.id.frButton)).setChecked(alarm.getDays()[4]);
        ((ToggleButton) findViewById(R.id.saButton)).setChecked(alarm.getDays()[5]);
        ((ToggleButton) findViewById(R.id.suButton)).setChecked(alarm.getDays()[6]);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                AlarmData alarm;
                boolean[] days = new boolean[]{
                        ((ToggleButton) findViewById(R.id.moButton)).isChecked(),
                        ((ToggleButton) findViewById(R.id.tuButton)).isChecked(),
                        ((ToggleButton) findViewById(R.id.weButton)).isChecked(),
                        ((ToggleButton) findViewById(R.id.thButton)).isChecked(),
                        ((ToggleButton) findViewById(R.id.frButton)).isChecked(),
                        ((ToggleButton) findViewById(R.id.saButton)).isChecked(),
                        ((ToggleButton) findViewById(R.id.suButton)).isChecked()
                };
                if (!(days[0] || days[1] || days[2] || days[3] || days[4] || days[5] || days[6])) {
                    return;
                }
                MyAdapter adapter = MyAdapter.adapter;
                int alarmIndex = adapter.getAlarmIndex();
                if (alarmString != null) {
                    alarm = adapter.getAlarms().get(alarmIndex);
                }
                else {
                    alarm = new AlarmData();
                    alarmIndex = adapter.getAlarms().size();
                    adapter.getAlarms().add(alarm);
                }
                alarm.setName(((TextView) findViewById(R.id.nameEditView)).getText().toString());
                alarm.setHour(((TimePicker) findViewById(R.id.timeEditView)).getHour());
                alarm.setMinute(((TimePicker) findViewById(R.id.timeEditView)).getMinute());
                alarm.setDays(days);

                if (alarmString != null) {
                    adapter.notifyItemChanged(alarmIndex);
                }
                else {
                    adapter.notifyItemInserted(alarmIndex);
                }
                AlarmData.writeToFile(System.currentTimeMillis(), MyAdapter.adapter.getAlarms(), view.getContext());

                finish();
            }
        });
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyAdapter adapter = MyAdapter.adapter;
                adapter.getAlarms().remove(adapter.getAlarmIndex());
                adapter.notifyItemRemoved(adapter.getAlarmIndex());
                AlarmData.writeToFile(System.currentTimeMillis(), MyAdapter.adapter.getAlarms(), view.getContext());

                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}