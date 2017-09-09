package com.stevend.grey;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.database.FirebaseListAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.john.waveview.WaveView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.github.memfis19.cadar.event.OnDayChangeListener;
import io.github.memfis19.cadar.event.OnMonthChangeListener;
import io.github.memfis19.cadar.settings.MonthCalendarConfiguration;
import io.github.memfis19.cadar.view.MonthCalendar;

public class MainActivity extends AppCompatActivity implements OnDayChangeListener, OnMonthChangeListener {
    private static final String TAG = "MainActivityLog";

    private TextView monthName;
    private MonthCalendar monthCalendar;
    private Calendar currentDay;
    private ListView entryList;
    private FloatingActionMenu materialDesignFAM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        entryList = (ListView)findViewById(R.id.entry_list);

        MonthCalendarConfiguration.Builder builder = new MonthCalendarConfiguration.Builder();
        builder.setFirstDayOfWeek(Calendar.SUNDAY);
        builder.setDisplayDaysOutOfMonth(false);

        monthName = (TextView) findViewById(R.id.month_name);
        monthCalendar = (MonthCalendar) findViewById(R.id.calendar);
        monthCalendar.setOnDayChangeListener(this);
        monthCalendar.setOnMonthChangeListener(this);
        monthCalendar.prepareCalendar(builder.build());

        ImageButton leftButton = (ImageButton) findViewById(R.id.left_button);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMonth(true);
            }
        });
        ImageButton rightButton = (ImageButton) findViewById(R.id.right_button);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMonth(false);
            }
        });

        currentDay = Calendar.getInstance();
        setFirebaseConnection(Common.roundEpochToDay(System.currentTimeMillis()));
        updateMonthTitle(Calendar.getInstance().getTime());

        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        FloatingActionButton entryButton = (FloatingActionButton) findViewById(R.id.action_menu_add_entry);
        entryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDesignFAM.close(true);
                final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) return;
                MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                        .title("Add entry")
                        .customView(R.layout.add_entry_dialog, true)
                        .cancelable(true)
                        .canceledOnTouchOutside(true)
                        .negativeText("Cancel")
                        .positiveText("OK")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                final View dialogView = dialog.getCustomView();
                                if (dialogView == null) return;
                                DiscreteSeekBar seekBar = (DiscreteSeekBar) dialogView.findViewById(R.id.seek_bar);
                                FirebaseDatabase.getInstance()
                                        .getReference("Cats").child("Grey").child("feedings").push()
                                        .setValue(new FeedingEntry(new Feeder(currentUser), System.currentTimeMillis(), seekBar.getProgress()));
                            }
                        })
                        .show();

                final View dialogView = dialog.getCustomView();
                if (dialogView != null) {
                    DiscreteSeekBar seekBar = (DiscreteSeekBar) dialogView.findViewById(R.id.seek_bar);
                    seekBar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
                        @Override
                        public int transform(int value) {
                            WaveView waveView = (WaveView) dialogView.findViewById(R.id.wave_view);
                            waveView.setProgress(value);
                            return value;
                        }
                    });
                }
            }
        });

        FloatingActionButton signOutButton = (FloatingActionButton) findViewById(R.id.action_menu_sign_out);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
                materialDesignFAM.close(true);
            }
        });

        entryList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final FeedingEntry feedingEntry = (FeedingEntry) parent.getItemAtPosition(position);

                MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                        .title("Entry options")
//                       TODO .customView()
                        .cancelable(true)
                        .canceledOnTouchOutside(true)
                        .negativeText(R.string.cancel)
                        .positiveText(R.string.save)
                        .neutralText(R.string.delete)
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                FirebaseDatabase.getInstance().getReference()
                                        .child("Cats").child("Grey").child("feedings").orderByChild("time").equalTo(feedingEntry.getTime())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                                    appleSnapshot.getRef().removeValue();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.e(TAG, "onCancelled", databaseError.toException());
                                            }
                                        });
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                // TODO save changes
                            }
                        })
                        .show();

                return false;
            }
        });
    }

    private void signOut() {
        // Firebase sign out
        FirebaseAuth.getInstance().signOut();

        // Google sign out
        final GoogleApiClient mGoogleApiClient = Common.getInstance().getmGoogleApiClient();
        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {

                FirebaseAuth.getInstance().signOut();
                if(mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.d(TAG, "User Logged out");
                                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                Log.d(TAG, "Google API Client Connection Suspended");
            }
        });
    }

    private void changeMonth(boolean isLeft) {
        currentDay.add(Calendar.MONTH, isLeft? -1 : 1);
        updateMonthTitle(currentDay.getTime());
        setFirebaseConnection(Common.roundEpochToDay(currentDay.getTimeInMillis()));
        monthCalendar.setSelectedDay(currentDay, true);
    }

    private void setFirebaseConnection(long selectedData) {
        final long epochDay = 24 * 60 * 60 * 1000;
        Query feedingsQuery = FirebaseDatabase.getInstance().getReference("Cats").child("Grey").child("feedings").orderByChild("time").startAt(selectedData).endAt(selectedData + epochDay);
        FirebaseListAdapter<FeedingEntry> feedingEntryFirebaseListAdapter =
                new FirebaseListAdapter<FeedingEntry>(this, FeedingEntry.class, R.layout.feeding_entry, feedingsQuery) {
                    @Override
                    protected void populateView(View v, FeedingEntry model, int position) {
                        final ImageView feederImage = (ImageView) v.findViewById(R.id.feeder_image);
                        Glide.with(MainActivity.this)
                                .load(model.getFeeder().getPhotoUrl())
                                .asBitmap()
                                .centerCrop()
                                .into(new BitmapImageViewTarget(feederImage) {
                                    @Override
                                    protected void setResource(Bitmap resource) {
                                        RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(MainActivity.this.getResources(), resource);
                                        circularBitmapDrawable.setCircular(true);
                                        feederImage.setImageDrawable(circularBitmapDrawable);
                                    }
                                });
                        ((TextView)v.findViewById(R.id.feeder_name)).setText(model.getFeeder().getName());
                        ((TextView)v.findViewById(R.id.time)).setText(new java.text.SimpleDateFormat("EEEE, d MMMM HH:mm").format(new java.util.Date(model.getTime())));
                    }
                };
        entryList.setAdapter(feedingEntryFirebaseListAdapter);
    }

    @Override
    public void onDayChanged(Calendar calendar) {
        setFirebaseConnection(Common.roundEpochToDay(calendar.getTimeInMillis()));
        currentDay = calendar;
    }

    @Override
    public void onMonthChanged(Calendar calendar) {
        updateMonthTitle(calendar.getTime());
    }

    private void updateMonthTitle(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        monthName.setText(cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.UK));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        monthCalendar.releaseCalendar();
    }
}
