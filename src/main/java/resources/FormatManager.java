package resources;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class FormatManager
        extends ErrorManager {

    public final String getEnviromentVariable(String name) {
        if (System.getenv().keySet().contains(name)) {
            return System.getenv(name);
        }
        if (System.getenv().keySet().contains("process.env." + name)) {
            return System.getenv(name);
        }
        return null;
    }

    public double getDouble(String value) {
        double v = 0.0D;
        try {
            v = NumberFormat.getInstance().parse(value).doubleValue();
        } catch (ParseException e) {
            registerError("getDouble", e);
        }
        return v;
    }

    public int getInteger(String value) {
        int v = 0;
        try {
            v = NumberFormat.getInstance().parse(value).intValue();
        } catch (ParseException e) {
            registerError("getDouble", e);
        }
        return v;
    }

    public String changeDateInYYYYMMDDFormat(String date) {
        return date.substring(6) + "-" + date.substring(3, 5) + "-" + date.substring(0, 2);
    }

    public String changeDateInDDMMYYYYFormat(String date) {
        return date.substring(8) + "-" + date.substring(5, 7) + "-" + date.substring(0, 4);
    }

    public long getDayBetween(String date) {
        return getDayBetween(getCurrentSQLDate(), date);
    }

    public long getDayBetween(String beginDate, String endDate) {
        long begin = 0L;
        long end = 0L;
        long days = 0L;
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
            begin = parser.parse(beginDate).getTime();
            end = parser.parse(endDate).getTime();
            days = Math.abs(end - begin) / 86400000L;
        } catch (Exception e) {
            registerError("getDayBetween", e);
        }
        return days;
    }

    public boolean timeOccursBetween(String beginTime, String endTime) {
        String date = null;
        try {
            date = getCurrentHour() + ":" + getCurrentMinute();
        } catch (Exception e) {
            registerError("timeOccursBetween(String beginTime, String endTime)", e);
        }
        return timeOccursBetween(beginTime, endTime, date);
    }

    public boolean timeOccursBetween(String beginTime, String endTime, String compareTime) {
        Date date = null;
        try {
            SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
            date = parser.parse(compareTime);
        } catch (ParseException e) {
            registerError("timeOccursBetween", e);
        }
        return timeOccursBetween(beginTime, endTime, date);
    }

    public boolean timeOccursBetween(String beginTime, String endTime, Date date) {
        try {
            SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
            Date begin = parser.parse(beginTime);
            Date end = parser.parse(endTime);
            if ((date.compareTo(begin) == 0) || ((date.after(begin)) && (date.before(end)))) {
                return true;
            }
        } catch (ParseException e) {
            registerError("timeOccursBetween", e);
        }
        return false;
    }

    public boolean timeOccursNow(int hour, int minute) {
        try {
            if ((getCurrentHour() == hour) && (getCurrentMinute() == minute)) {
                return true;
            }
        } catch (Exception e) {
            registerError("timeOccursBetween", e);
        }
        return false;
    }

    public String getCurrentDateText() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyyy");
        return format.format(getCurrentDate().getTime());
    }

    public String getCurrentDateTimeText() {
        return getCurrentDateText() + " " + getCurrentTimeText();
    }

    public String getCurrentTimeText() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(getCurrentDate().getTime());
    }

    public String getCurrentSQLDate() {
        return getCurrentSQLDate(0);
    }

    public String getCurrentSQLDate(int dias) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(getCurrentDate(dias).getTime());
    }

    public String getCurrentSQLDateTime() {
        return getCurrentSQLDateTime(0);
    }

    public String getCurrentSQLDateTime(int dias) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(getCurrentDate(dias).getTime());
    }

    public String getMonthName(int month) {
        SimpleDateFormat format = new SimpleDateFormat("MMMM");
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(2, month);
        return format.format(calendar.getTime());
    }

    public String getMonthShortName(int month) {
        SimpleDateFormat format = new SimpleDateFormat("MMM");
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(2, month);
        return format.format(calendar.getTime());
    }

    public int getYearFrom(String date) {
        if (date.length() >= 10) {
            return Integer.parseInt(date.substring(0, 4));
        }
        return 0;
    }

    public int getMonthFrom(String date) {
        if (date.length() >= 10) {
            return Integer.parseInt(date.substring(5, 7));
        }
        return 0;
    }

    public int getDayFrom(String date) {
        if (date.length() == 10) {
            return Integer.parseInt(date.substring(8));
        }
        if (date.length() > 10) {
            return Integer.parseInt(date.substring(8, 10));
        }
        return 0;
    }

    public String getCurrentSQLTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(getCurrentDate().getTime());
    }

    public GregorianCalendar getCurrentDate() {
        return getCurrentDate(0);
    }

    public GregorianCalendar getCurrentDate(int dias) {
        GregorianCalendar calendar = new GregorianCalendar();
        if (dias != 0) {
            calendar.add(5, dias);
        }
        return calendar;
    }

    public int getCurrentYear() {
        return getCurrentDate().get(1);
    }

    public int getCurrentMonth() {
        return getCurrentDate().get(2) + 1;
    }

    public int getCurrentDay() {
        return getCurrentDate().get(5);
    }

    public int getCurrentHour() {
        return getCurrentDate().get(11);
    }

    public int getCurrentMinute() {
        return getCurrentDate().get(12);
    }

    public int getCurrentSecond() {
        return getCurrentDate().get(13);
    }

    public String getCurrencyFormat(double value) {
        return getCurrencyFormat(value, 0);
    }

    public String getCurrencyFormat(double value, int fractionDigits) {
        NumberFormat NmFr = NumberFormat.getCurrencyInstance();
        NmFr.setGroupingUsed(true);
        NmFr.setMinimumFractionDigits(fractionDigits);
        NmFr.setMaximumFractionDigits(fractionDigits);
        return NmFr.format(value);
    }

    public String getIntegerFormat(double value) {
        NumberFormat NmFr = NumberFormat.getNumberInstance();
        NmFr.setGroupingUsed(false);
        NmFr.setMinimumFractionDigits(0);
        NmFr.setMaximumFractionDigits(0);
        return NmFr.format(value);
    }

    public String getDecimalFormat(double value) {
        return getDecimalFormat(value, 0);
    }

    public String getDecimalFormat(double value, int fractionDigits) {
        NumberFormat NmFr = NumberFormat.getNumberInstance();
        NmFr.setGroupingUsed(true);
        NmFr.setMinimumFractionDigits(fractionDigits);
        NmFr.setMaximumFractionDigits(fractionDigits);
        return NmFr.format(value);
    }

    public String getPercentFormat(double value) {
        return getPercentFormat(value, 0);
    }

    public String getPercentFormat(double value, int fractionDigits) {
        NumberFormat NmFr = NumberFormat.getPercentInstance();
        NmFr.setMinimumFractionDigits(fractionDigits);
        NmFr.setMaximumFractionDigits(fractionDigits);
        return NmFr.format(value);
    }

    public String getMinutesToElapsedTime(double minutes) {
        double r = minutes;
        if (minutes == 0.0D) {
            return "0 m";
        }
        int d = (int) (r / 1440.0D);
        r %= 1440.0D;
        int h = (int) (r / 60.0D);
        r %= 60.0D;
        int m = (int) r;
        int s = (int) ((r % 60.0D - m) * 60.0D);
        return (d > 0 ? d + " d " : "") + (h > 0 ? h + " h " : "") + (m > 0 ? m + " m " : "") + (s > 0 ? s + " s " : "");
    }

    public int getMaxMonthDays(int year, int month) {
        switch (month) {
            case 2:
                if ((year % 400 == 0) || ((year % 4 == 0) && (year % 100 != 0))) {
                    return 29;
                }
                return 28;
            case 4:
            case 5:
            case 9:
            case 11:
                return 30;
        }
        return 31;
    }
}
