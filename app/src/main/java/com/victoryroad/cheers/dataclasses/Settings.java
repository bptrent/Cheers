package com.victoryroad.cheers.dataclasses;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.victoryroad.cheers.dataclasses.Settings.DesignatedDriver.CONTACT;
import static com.victoryroad.cheers.dataclasses.Settings.DesignatedDriver.NONE;
import static com.victoryroad.cheers.dataclasses.Settings.DesignatedDriver.NULL;
import static com.victoryroad.cheers.dataclasses.Settings.DesignatedDriver.UBER;


public class Settings {
    //First value is how long to have vibration pattern off, then how long to have it on, and so on for the length of the array
    private final long[] vibrationPattern = new long[] {0, 500, 200, 500};

    private SharedPreferences p;
    private Context prefManager;

    private MediaPlayer mMediaPlayer;

    private static Settings mSetting = new Settings();

    public enum DesignatedDriver {NULL, UBER, CONTACT, NONE};


    private Settings() {
        p = new SharedPreferences() {
            @Override
            public Map<String, ?> getAll() {
                return null;
            }

            @Nullable
            @Override
            public String getString(String key, String defValue) {
                return defValue;
            }

            @Nullable
            @Override
            public Set<String> getStringSet(String key, Set<String> defValues) {
                return defValues;
            }

            @Override
            public int getInt(String key, int defValue) {
                return defValue;
            }

            @Override
            public long getLong(String key, long defValue) {
                return defValue;
            }

            @Override
            public float getFloat(String key, float defValue) {
                return defValue;
            }

            @Override
            public boolean getBoolean(String key, boolean defValue) {
                return defValue;
            }

            @Override
            public boolean contains(String key) {
                return false;
            }

            @Override
            public Editor edit() {
                return null;
            }

            @Override
            public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

            }

            @Override
            public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {

            }
        };
    }

    public static Settings getSettings() {
        return mSetting;
    }

    public static Settings getSettingsAndSetPreferenceContext(Context prefContext) {
        mSetting.setManager(prefContext);
        return mSetting;
    }

    public void makeCall(Context context) {
        switch(this.getDesignatedDriver()) {
            case UBER:
                makeCallWithUber(context);
                break;
            case CONTACT:
                makeCallWithContact(context);
                break;
            case NONE:
                Toast.makeText(context, "No designated driver set", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(context, "ERROR: UNKNOWN DD STATE", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void makeCallWithUber(Context context) {
        //TODO
    }

    private void makeCallWithContact(Context context) {
        if(this.getContact() == null || this.getDesignatedDriver() != DesignatedDriver.CONTACT)
            return;
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + this.getContact().number));
        context.startActivity(intent);
    }

    public void playRingtone(Context context) {
        if(this.wantsReminders() && this.wantsRing()) {

            mMediaPlayer = new MediaPlayer();
            try {
                mMediaPlayer.setDataSource(context, this.getRingtone());
                final AudioManager audioManager = (AudioManager) context
                        .getSystemService(Context.AUDIO_SERVICE);
                if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0) {
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                }
            } catch (IOException e) {
                System.out.println("OOPS");
            }
        }
    }

    public void vibrate(Context context) {
        if(this.wantsVibrate()) {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

            v.vibrate(vibrationPattern, -1);
        }
    }

    private void setManager(Context prefManager) {
        Log.w("Settings", prefManager.toString());
        this.prefManager = prefManager;
    }

    private void updateP() {
        if(prefManager == null)
            return;
        p = PreferenceManager.getDefaultSharedPreferences(prefManager);
    }

    public DesignatedDriver getDesignatedDriver() {
        updateP();

        String result = p.getString("example_list", "0");

        DesignatedDriver dd = DesignatedDriver.NULL;

        switch(Integer.parseInt(result)) {
            case 1:
                dd = DesignatedDriver.UBER;
                break;
            case 2:
                dd = DesignatedDriver.CONTACT;
                break;
            case 3:
                dd = DesignatedDriver.NONE;
        }

        return dd;
    }

    public Contact getContact() {
        updateP();
        String contact = p.getString("custom_contact", null);
        if(contact == null)
            return null;

        return Contact.deserialize(contact);
    }

    public LatLng getHomeLocation() {
        updateP();

        String loc = p.getString("custom_set_home_location", "");

        if(loc.equals(""))
            return null;

        double lat = Double.parseDouble(loc.substring(0, loc.indexOf(',')));
        double lng = Double.parseDouble(loc.substring(loc.indexOf(',')+1));

        return new LatLng(lat, lng);
    }

    public boolean wantsReminders() {
        updateP();
        boolean r = p.getBoolean("notifications_new_message", false);
        Log.w("Settings", "wantsReminders: " + r);
        return r;
    }

    public int timeBetweenNotifications() {
        updateP();
        return Integer.parseInt(p.getString("time_between_notifications", "-1"));
    }

    public boolean wantsRing() {
        updateP();

        boolean r = p.getBoolean("notifications_new_message_ring", false);
        Log.w("Settings", "wantsRing: " + r);
        return r;
    }

    public Uri getRingtone() {
        updateP();
        return Uri.parse(p.getString("notifications_new_message_ringtone", "DEFAULT_SOUND"));
    }

    public boolean wantsVibrate() {
        updateP();

        boolean r = p.getBoolean("notifications_new_message_vibrate", false);
        Log.w("Settings", "wantsVibrate: " + r);
        return r;
    }


}