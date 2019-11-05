package nerd.tuxmobil.fahrplan.congress.models;

import java.util.ArrayList;

import info.metadude.android.eventfahrplan.commons.Clock;
import info.metadude.android.eventfahrplan.commons.SystemClock;
import nerd.tuxmobil.fahrplan.congress.utils.DateHelper;

public class DateInfos extends ArrayList<DateInfo> {

    private static final long serialVersionUID = 1L;

    public boolean sameDay(Clock today, int lectureListDay) {
        String currentDate = DateHelper.getFormattedDate(today);
        for (DateInfo dateInfo : this) {
            if (dateInfo.dayIdx == lectureListDay &&
                    dateInfo.date.equals(currentDate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the index of today
     *
     * @param hourOfDayChange   Hour of day change (all lectures which start before count to the
     *                          previous day)
     * @param minuteOfDayChange Minute of day change
     * @return dayIndex if found, -1 otherwise
     */
    public int getIndexOfToday(int hourOfDayChange, int minuteOfDayChange) {
        if (isEmpty()) {
            return -1;
        }
        Clock todayClock = new SystemClock();
        todayClock.minusHours(hourOfDayChange);
        todayClock.minusMinutes(minuteOfDayChange);
        todayClock.normalize();

        String currentDate = DateHelper.getFormattedDate(todayClock);

        int dayIndex = -1;
        for (DateInfo dateInfo : this) {
            dayIndex = dateInfo.getDayIndex(currentDate);
            if (dayIndex != -1) {
                return dayIndex;
            }
        }
        return dayIndex;
    }

}
