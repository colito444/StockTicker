package com.github.premnirmal.ticker;

import android.content.Context;
import android.provider.Settings;

import com.crashlytics.android.Crashlytics;
import com.github.premnirmal.tickerwidget.BuildConfig;
import com.github.premnirmal.tickerwidget.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by premnirmal on 4/15/15.
 */
public class Analytics {


    public static final String SCHEDULE_UPDATE_ACTION = "ScheduleUpdate";

    private static Analytics INSTANCE;

    private final Tracker tracker;

    private Analytics(Context context) {
        final GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(context);
        if (BuildConfig.DEBUG) {
            googleAnalytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
        }
        tracker = googleAnalytics.newTracker(R.xml.event_tracker);
        final String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if (android_id != null) {
            tracker.set("&uid", android_id);
        } else {
            Crashlytics.log("Android_ID is null!");
            tracker.set("&uid", "EMPTY");
        }
    }

    static void init(Context context) {
        INSTANCE = new Analytics(context);
    }

    public static void trackUpdate(String action, String label, long value) {
        INSTANCE.tracker.send(new HitBuilders.EventBuilder()
                .setCategory("WidgetStatus")
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());
    }

    public static void trackWidgetUpdate(String action) {
        INSTANCE.tracker.send(new HitBuilders.EventBuilder()
                .setCategory("WidgetStatus")
                .setAction(action)
                .build());
    }

    public static void trackWidgetSizeUpdate(long value) {
        INSTANCE.tracker.send(new HitBuilders.EventBuilder()
                .setCategory("WidgetStatus")
                .setAction("onOptionsChanged")
                .setLabel("Size")
                .setValue(value)
                .build());
    }

    public static void trackUI(String action, String label) {
        INSTANCE.tracker.send(new HitBuilders.EventBuilder()
                .setCategory("AppView")
                .setAction(action)
                .setLabel(label)
                .build());
    }

    public static void trackIntialSettings(String action, String label) {
        INSTANCE.tracker.send(new HitBuilders.EventBuilder()
                .setCategory("AppSettings")
                .setAction(action)
                .setLabel(label)
                .build());
    }

    public static void trackSettingsChange(String action, String label) {
        INSTANCE.tracker.send(new HitBuilders.EventBuilder()
                .setCategory("AppSettingsChange")
                .setAction(action)
                .setLabel(label)
                .build());
    }

}