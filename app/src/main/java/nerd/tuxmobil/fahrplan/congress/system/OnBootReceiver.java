package nerd.tuxmobil.fahrplan.congress.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.ligi.tracedroid.logging.Log;

import java.util.List;

import info.metadude.android.eventfahrplan.commons.Clock;
import info.metadude.android.eventfahrplan.commons.SystemClock;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmReceiver;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices;
import nerd.tuxmobil.fahrplan.congress.autoupdate.UpdateService;
import nerd.tuxmobil.fahrplan.congress.dataconverters.AlarmExtensions;
import nerd.tuxmobil.fahrplan.congress.models.Alarm;
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc;

public final class OnBootReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "onBoot";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (!action.equals(AlarmReceiver.ALARM_UPDATE)) {
            return;
        }

        MyApp.LogDebug(LOG_TAG, "onReceive (reboot)");

        Clock nowClock = new SystemClock();
        Clock storedAlarmTime = new SystemClock();
        nowClock.plusSeconds(15);
        nowClock.normalize();

        AppRepository appRepository = AppRepository.Companion.getInstance(context);
        List<Alarm> alarms = appRepository.readAlarms();
        for (Alarm alarm : alarms) {
            storedAlarmTime.setToMilliseconds(alarm.getStartTime());
            if (nowClock.isBefore(storedAlarmTime)) {
                Log.d(getClass().getName(), "Scheduling alarm for event: " + alarm.getEventId() + ", " + alarm.getEventTitle());
                SchedulableAlarm schedulableAlarm = AlarmExtensions.toSchedulableAlarm(alarm);
                AlarmServices.scheduleEventAlarm(context, schedulableAlarm);
            } else {
                MyApp.LogDebug(LOG_TAG, "Deleting alarm from database: " + alarm);
                appRepository.deleteAlarmForAlarmId(alarm.getId());
            }
        }

        // start auto updates
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean defaultValue = context.getResources().getBoolean(R.bool.preferences_auto_update_enabled_default_value);
        boolean doAutoUpdates = prefs.getBoolean("auto_update", defaultValue);
        if (doAutoUpdates) {
            long lastFetch = prefs.getLong("last_fetch", 0);
            nowClock.setToNow();
            long nowMillis = nowClock.toMilliseconds();

            long interval = FahrplanMisc.setUpdateAlarm(context, true);

            MyApp.LogDebug(LOG_TAG, "now: " + nowMillis + ", last_fetch: " + lastFetch);
            if (interval > 0 && nowMillis - lastFetch >= interval) {
                UpdateService.start(context);
            }
        }
    }

}
