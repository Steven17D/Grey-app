package com.stevend.grey.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;

import com.stevend.grey.Common;
import com.stevend.grey.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Steven on 6/23/2017.
 */

public class CalendarFragment extends Fragment implements CalendarView.OnDateChangeListener {
    private static final CalendarFragment ourInstance = new CalendarFragment();

    public static CalendarFragment getInstance() {
        return ourInstance;
    }

    public CalendarFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_calender, container, false);
        ((CalendarView)view.findViewById(R.id.calendar)).setOnDateChangeListener(this);
        Toast.makeText(getContext(),
                new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.UK).format(new Date(Common.roundEpochToDay(((CalendarView) view.findViewById(R.id.calendar)).getDate())))
                , Toast.LENGTH_LONG).show();

//        FirebaseListAdapter<FeedingEntry> feedingEntryFirebaseListAdapter = new FirebaseListAdapter<FeedingEntry>(getActivity(), FeedingEntry.class, R.layout.feeding_entry,
//                FirebaseDatabase.getInstance().getReference("Cats").child("Grey").child("feedings")) {
//            @Override
//            protected void populateView(View v, FeedingEntry model, int position) {
////                ((TextView)v.findViewById(R.id.feeder_name)).setText(model.getFeeder().getName());
////                ((TextView)v.findViewById(R.id.time)).setText(new java.text.SimpleDateFormat("EEEE, d MMMM yyyy HH:mm").format(new java.util.Date (model.getTime()*1000)));
//            }
//        };
//        ((ListView)view.findViewById(R.id.entry_list)).setAdapter(feedingEntryFirebaseListAdapter);
        return view;
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
        try {
            Toast.makeText(getContext(), Common.createEpochByDate(year, month + 1, dayOfMonth), Toast.LENGTH_LONG).show();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
