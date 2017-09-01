package com.stevend.grey;

import com.google.android.gms.common.api.GoogleApiClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Steven on 6/23/2017.
 */

public class Common {
    private static final Common ourInstance = new Common();

    public static Common getInstance() {
        return ourInstance;
    }

    private Common() {
    }

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setmGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    private GoogleApiClient mGoogleApiClient;

    public static long createEpochByDate(int year, int month, int dayOfMonth) throws ParseException {
        String str = String.format("%d %d %d", dayOfMonth, month, year);
        SimpleDateFormat df = new SimpleDateFormat("d M y");
        Date date = df.parse(str);
        return date.getTime();
    }

    public static Long roundEpochToDay(long epoch) {
        long newEpoch = epoch / 1000;
        newEpoch -= newEpoch % 86400;
        return newEpoch * 1000;
    }


}
