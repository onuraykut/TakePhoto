package com.onur.kryptow.takephoto;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import com.obsez.android.lib.filechooser.ChooserDialog;
import java.io.File;

public class SettingsActivity extends AppCompatPreferenceActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            // EditText change listener
            bindPreferenceSummaryToValue(findPreference("ip_adress_key"));
            bindPreferenceSummaryToValue(findPreference("password_key"));
            bindPreferenceSummaryToValue(findPreference("port_key"));
            bindPreferenceSummaryToValue(findPreference("server_path_key"));
            bindPreferenceSummaryToValue(findPreference("username_key"));
            bindPreferenceSummaryToValue(findPreference("image_name_key"));

            // feedback preference click listener
            Preference myPref = findPreference(getString(R.string.key_send_feedback));
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    sendFeedback(getActivity());
                    return true;
                }
            });
            final Preference myPref2 = findPreference(getString(R.string.device_folder_key));
            myPref2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    new ChooserDialog(getActivity())
                            .withFilter(true, false)
                            .withStartFile("downloads")
                            .withChosenListener(new ChooserDialog.Result() {
                                @Override
                                public void onChoosePath(String path, File pathFile) {
                                    Toast.makeText(getActivity(), "FOLDER: " + path, Toast.LENGTH_SHORT).show();
                                    myPref2.getEditor().putString("device_folder_key",path);
                                    MainActivity.device_path=path;
                                    myPref2.setSummary(path);

                                }
                            })
                            .build()
                            .show();
                    return true;
                }
            });
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            }


            else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(R.string.summary_choose_ringtone);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else if (preference instanceof EditTextPreference) {
                switch (preference.getKey()) {
                    case "ip_adress_key":
                        // update the changed gallery name to summary filed
                        preference.setSummary(stringValue);
                        MainActivity.ip_adress = stringValue;
                        break;
                    case "username_key":
                        // update the changed gallery name to summary filed
                        preference.setSummary(stringValue);
                        MainActivity.username = stringValue;
                        break;
                    case "password_key":
                        // update the changed gallery name to summary filed
                        //preference.setSummary(stringValue);
                        MainActivity.password = stringValue;
                        break;
                    case "port_key":
                        // update the changed gallery name to summary filed
                        preference.setSummary(stringValue);
                        MainActivity.port = Integer.parseInt(stringValue);
                        break;
                    case "server_path_key":
                        // update the changed gallery name to summary filed
                        preference.setSummary(stringValue);
                        MainActivity.server_path = stringValue;
                        break;
                    case "image_name_key":
                        // update the changed gallery name to summary filed
                        preference.setSummary(stringValue);
                        MainActivity.image_name = stringValue;
                        break;
                }
            }
            else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };
    /**
     * Email client intent to send support mail
     * Appends the necessary device information to email body
     * useful when providing support
     */
    public static void sendFeedback(Context context) {
        String body = null;
        try {
            body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(TAG,"Exception in sendfeedback");
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"onuraykut@outlook.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Take Photo App");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose_email_client)));
    }
}
