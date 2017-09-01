package com.stevend.grey.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.stevend.grey.Common;
import com.stevend.grey.FeedingEntry;
import com.stevend.grey.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.github.memfis19.cadar.event.OnDayChangeListener;
import io.github.memfis19.cadar.event.OnMonthChangeListener;
import io.github.memfis19.cadar.settings.MonthCalendarConfiguration;
import io.github.memfis19.cadar.view.MonthCalendar;

/**
 * Created by Steven on 6/23/2017.
 */

public class CalendarFragment extends Fragment implements OnDayChangeListener, OnMonthChangeListener {
    private static final CalendarFragment ourInstance = new CalendarFragment();

    public static CalendarFragment getInstance() {
        return ourInstance;
    }
    public CalendarFragment() {}

    private View view;
    private TextView monthName;
    private MonthCalendar calendarView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_calender, container, false);

        MonthCalendarConfiguration.Builder builder = new MonthCalendarConfiguration.Builder();
        builder.setFirstDayOfWeek(Calendar.SUNDAY);
        builder.setDisplayDaysOutOfMonth(false);

        calendarView = (MonthCalendar) view.findViewById(R.id.calendar);
        calendarView.setOnDayChangeListener(this);
        calendarView.setOnMonthChangeListener(this);
        calendarView.prepareCalendar(builder.build());

        setFirebaseConnection(Common.roundEpochToDay(System.currentTimeMillis()));
        monthName = (TextView) view.findViewById(R.id.month_name);
        Date date = Calendar.getInstance().getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        monthName.setText(cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.UK));
        return view;
    }

    private void setFirebaseConnection(long selectedData) {
        final long epochDay = 24 * 60 * 60 * 1000;
        Query feedingsQuery = FirebaseDatabase.getInstance().getReference("Cats").child("Grey").child("feedings").orderByChild("time").startAt(selectedData).endAt(selectedData + epochDay);
        FirebaseListAdapter<FeedingEntry> feedingEntryFirebaseListAdapter =
                new FirebaseListAdapter<FeedingEntry>(getActivity(), FeedingEntry.class, R.layout.feeding_entry, feedingsQuery) {
            @Override
            protected void populateView(View v, FeedingEntry model, int position) {
                final ImageView feederImage = (ImageView) v.findViewById(R.id.feeder_image);
                Glide.with(getContext())
                        .load(model.getFeeder().getPhotoUrl())
                        .asBitmap()
                        .centerCrop()
                        .into(new BitmapImageViewTarget(feederImage) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                feederImage.setImageDrawable(circularBitmapDrawable);
                            }
                        });
                ((TextView)v.findViewById(R.id.feeder_name)).setText(model.getFeeder().getName());
                ((TextView)v.findViewById(R.id.time)).setText(new java.text.SimpleDateFormat("EEEE, d MMMM HH:mm").format(new java.util.Date(model.getTime())));
            }
        };
        ((ListView)view.findViewById(R.id.entry_list)).setAdapter(feedingEntryFirebaseListAdapter);
    }

    @Override
    public void onDayChanged(Calendar calendar) {
        setFirebaseConnection(Common.roundEpochToDay(calendar.getTimeInMillis()));
    }

    @Override
    public void onMonthChanged(Calendar calendar) {
        java.util.Date date = calendar.getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        monthName.setText(cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.UK));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        calendarView.releaseCalendar();
    }
}
