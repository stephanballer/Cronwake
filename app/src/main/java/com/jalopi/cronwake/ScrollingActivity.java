package com.jalopi.cronwake;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jalopi.cronwake.databinding.ActivityScrollingBinding;
import com.jcraft.jsch.JSchException;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ScrollingActivity extends AppCompatActivity {

    private ActivityScrollingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        binding = ActivityScrollingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        findViewById(R.id.addAlarmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), AlarmEditActivity.class));
            }
        });

        ActivityResultLauncher<ScanOptions> activityResultLauncher = registerForActivityResult(new ScanContract(), new ActivityResultCallback<ScanIntentResult>() {
            @Override
            public void onActivityResult(ScanIntentResult result) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ScrollingActivity.this);
                String user = prefs.getString(getString(R.string.user), "pi");
                String host = prefs.getString(getString(R.string.host), "host");
                String password = prefs.getString(getString(R.string.password), "password");
                String stopCommand = prefs.getString(getString(R.string.stop_command), "pkill ffplay");
                int port = Integer.parseInt(prefs.getString(getString(R.string.port), "22"));
                AlertDialog.Builder builder = new AlertDialog.Builder(ScrollingActivity.this);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                if (result.getContents() == null) {
                    return;
                }
                else if (prefs.getString(getString(R.string.barcode), "x").equals(result.getContents())) {
                    try {
                        AlarmData.stopAlarm(host, port, user, password, stopCommand);
                    } catch (JSchException e) {
                        builder.setTitle("Connection failed");
                        builder.setMessage(user + "@" + host + ":" + port);
                        builder.show();
                        e.printStackTrace();
                    }
                }
                else {
                    builder.setTitle("Barcode mismatch");
                    builder.setMessage(getString(R.string.change_qr_message) + "\n" + result.getContents());
                    builder.setNeutralButton(R.string.change_qr, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(getString(R.string.barcode), result.getContents());
                            editor.apply();
                            dialogInterface.dismiss();
                        }
                    });
                    builder.show();
                }
            }
        });
        findViewById(R.id.qrButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ScanOptions scanOptions = new ScanOptions();
                    scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
                    scanOptions.setCaptureActivity(MyCaptureActivity.class);
                    scanOptions.setOrientationLocked(true);
                    activityResultLauncher.launch(scanOptions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //List<AlarmData> alarms = new ArrayList<>();
        //boolean[] days = {true, true, true, true, true, true, true};
        //alarms.add(new AlarmData("0", 12, 4, days));
        //alarms.add(new AlarmData("1", 12, 4, days));
        //alarms.add(new AlarmData("2", 12, 4, days));

        List<AlarmData> alarms = AlarmData.readFromFile(this);

        RecyclerView mainListView = findViewById(R.id.mainListView);
        MyAdapter adapter = new MyAdapter(alarms);
        mainListView.setAdapter(adapter);
        mainListView.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                adapter.getAlarms().remove(viewHolder.getAdapterPosition());
                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());

                AlarmData.writeToFile(MyAdapter.adapter.getAlarms(), ScrollingActivity.this);
            }
        });

        itemTouchHelper.attachToRecyclerView(mainListView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        else if (id == R.id.action_upload || id == R.id.action_download) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ScrollingActivity.this);
            AlertDialog.Builder builder = new AlertDialog.Builder(ScrollingActivity.this);
            String user = prefs.getString(getString(R.string.user), "pi");
            String host = prefs.getString(getString(R.string.host), "host");
            String password = prefs.getString(getString(R.string.password), "password");
            String command = prefs.getString(getString(R.string.exec_command), "exec_command");
            int port = Integer.parseInt(prefs.getString(getString(R.string.port), "22"));
            builder.setMessage(user + "@" + host + ":" + port);
            try {
                if (id == R.id.action_upload) {
                    AlarmData.sendAlarms(MyAdapter.adapter.getAlarms(), host, port, user, password, command);
                }
                else {
                    AlarmData.getAlarms(host, port, user, password);
                    MyAdapter.adapter.notifyDataSetChanged();
                    AlarmData.writeToFile(MyAdapter.adapter.getAlarms(), this);
                }
                builder.setTitle("Sync successful");
            } catch (JSchException | IOException e) {
                e.printStackTrace();
                builder.setTitle("Sync failed");
            }
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.create().show();
        }
        return super.onOptionsItemSelected(item);
    }
}