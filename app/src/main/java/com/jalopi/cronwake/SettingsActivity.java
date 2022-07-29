package com.jalopi.cronwake;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            EditTextPreference.OnBindEditTextListener listener = new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setSingleLine();
                }
            };
            EditTextPreference.OnBindEditTextListener listener_num = new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setSingleLine();
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            };
            ((EditTextPreference) findPreference(getString(R.string.host))).setOnBindEditTextListener(listener);
            ((EditTextPreference) findPreference(getString(R.string.port))).setOnBindEditTextListener(listener_num);
            ((EditTextPreference) findPreference(getString(R.string.user))).setOnBindEditTextListener(listener);
            ((EditTextPreference) findPreference(getString(R.string.password))).setOnBindEditTextListener(listener);
            ((EditTextPreference) findPreference(getString(R.string.barcode))).setOnBindEditTextListener(listener);
            ((EditTextPreference) findPreference(getString(R.string.exec_command))).setOnBindEditTextListener(listener);
            ((EditTextPreference) findPreference(getString(R.string.stop_command))).setOnBindEditTextListener(listener);
        }
    }
}