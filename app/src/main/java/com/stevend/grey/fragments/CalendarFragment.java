package com.stevend.grey.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.stevend.grey.Common;
import com.stevend.grey.FeedingEntry;
import com.stevend.grey.R;

import java.text.ParseException;

/**
 * Created by Steven on 6/23/2017.
 */

public class CalendarFragment extends Fragment implements CalendarView.OnDateChangeListener {
    private static final CalendarFragment ourInstance = new CalendarFragment();

    public static CalendarFragment getInstance() {
        return ourInstance;
    }
    public CalendarFragment() {}

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_calender, container, false);

        CalendarView calendarView = (CalendarView) view.findViewById(R.id.calendar);
        calendarView.setOnDateChangeListener(this);

        setFirebaseConnection(Common.roundEpochToDay(((CalendarView) view.findViewById(R.id.calendar)).getDate()));

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
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
        try {
            Toast.
                    makeText(getContext(),
                    String.valueOf(Common.createEpochByDate(year, month + 1, dayOfMonth)),
                    Toast.LENGTH_SHORT)
                    .show();

            setFirebaseConnection(Common.createEpochByDate(year, month + 1, dayOfMonth));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
