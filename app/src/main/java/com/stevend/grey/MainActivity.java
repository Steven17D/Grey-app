package com.stevend.grey;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.john.waveview.WaveView;
import com.roger.catloadinglibrary.CatLoadingView;

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
    private Calendar currentDay = Calendar.getInstance();
    private ListView entryList;
    private FloatingActionMenu materialDesignFAM;
    private FirebaseListAdapter<FeedingEntry> feedingEntryFirebaseListAdapter;
    private CatLoadingView catLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        catLoadingView = new CatLoadingView();
        catLoadingView.show(getSupportFragmentManager(), "");
        initCalendar();
        initEntryListView();
        initFloatingActionMenu();
    }

    private void initCalendar() {
        class CalendarControllerOnClickListener implements View.OnClickListener {
            private final boolean isLeft;

            private CalendarControllerOnClickListener(boolean isLeft) {
                this.isLeft = isLeft;
            }

            @Override
            public void onClick(View v) {
                changeMonth(isLeft);
            }
        }

        MonthCalendarConfiguration.Builder builder = new MonthCalendarConfiguration.Builder();
        builder.setFirstDayOfWeek(Calendar.SUNDAY);
        builder.setDisplayDaysOutOfMonth(false);

        monthName = (TextView) findViewById(R.id.month_name);
        monthCalendar = (MonthCalendar) findViewById(R.id.calendar);
        monthCalendar.setOnDayChangeListener(this);
        monthCalendar.setOnMonthChangeListener(this);
        monthCalendar.prepareCalendar(builder.build());

        ImageButton leftButton = (ImageButton) findViewById(R.id.left_button);
        leftButton.setOnClickListener(new CalendarControllerOnClickListener(true));
        ImageButton rightButton = (ImageButton) findViewById(R.id.right_button);
        rightButton.setOnClickListener(new CalendarControllerOnClickListener(false));

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateEntryViewAdapter(Common.roundEpochToDay(System.currentTimeMillis()));
    }

    private void initEntryListView() {
        entryList = (ListView) findViewById(R.id.entry_list);
        updateMonthTitle(Calendar.getInstance().getTime());
        entryList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final FeedingEntry feedingEntry;
                final FeedingEntry originalFeedingEntry = (FeedingEntry) parent.getItemAtPosition(position);
                try {
                    feedingEntry = originalFeedingEntry.clone();
                } catch (CloneNotSupportedException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    return false;
                }

                final DatabaseReference feedingEntryRef = feedingEntryFirebaseListAdapter.getRef(position).getRef();

                MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                        .title(R.string.edit_entry_title)
                        .customView(R.layout.edit_entry_dialog, false)
                        .cancelable(true)
                        .canceledOnTouchOutside(true)
                        .negativeText(R.string.cancel)
                        .positiveText(R.string.save)
                        .neutralText(R.string.delete)
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                feedingEntryRef.removeValue();
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                final String amountString = ((EditText) dialog.findViewById(R.id.amount_editText)).getText().toString().replace("%", "").replace(" ", "");
                                try {
                                    int amount = Integer.parseInt(amountString);
                                    feedingEntry.setAmount(amount <= 100 ? amount : 100);
                                } finally {
                                    feedingEntryRef.setValue(feedingEntry);
                                }
                            }
                        })
                        .show();

                final View dialogView = dialog.getCustomView();
                if (dialogView == null) return false;

                TextView feederNameEditText = (TextView) dialogView.findViewById(R.id.feeder_name_text_view);
                feederNameEditText.setText(feedingEntry.getFeeder().getName());
                feederNameEditText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, R.string.disabled_feeder_edit, Toast.LENGTH_LONG).show();
                    }
                });

                final TextView dateTextView = (TextView) dialogView.findViewById(R.id.date_text_view);
                String dateString = new java.text.SimpleDateFormat("EEEE, d MMMM\nHH:mm", Locale.getDefault()).format(new Date(feedingEntry.getTime()));
                dateTextView.setText(dateString);

                ImageButton dateImageButton = (ImageButton) dialogView.findViewById(R.id.change_data_imageButton);

                View.OnClickListener dateChangeOnClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(feedingEntry.getTime());
                        new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
                                new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        calendar.set(year, month, dayOfMonth, hourOfDay, minute);
                                        feedingEntry.setTime(calendar.getTimeInMillis());
                                        String dateString = new java.text.SimpleDateFormat("EEEE, d MMMM\nHH:mm", Locale.getDefault()).format(new Date(feedingEntry.getTime()));
                                        dateTextView.setText(dateString);
                                    }
                                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
                            }
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                    }
                };

                dateTextView.setOnClickListener(dateChangeOnClickListener);
                dateImageButton.setOnClickListener(dateChangeOnClickListener);


                EditText amountEditText = (EditText) dialogView.findViewById(R.id.amount_editText);
                amountEditText.setText(String.format("%s %%", feedingEntry.getAmount()));

                return false;
            }
        });
    }

    private void initFloatingActionMenu() {
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
                        .customView(R.layout.add_entry_dialog, false)
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
                            TextView progressTextView = (TextView) dialogView.findViewById(R.id.amount_text_view);
                            progressTextView.setText(String.valueOf(value));
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
        updateEntryViewAdapter(Common.roundEpochToDay(currentDay.getTimeInMillis()));
        monthCalendar.setSelectedDay(currentDay, true);
    }

    private void updateEntryViewAdapter(long selectedData) {
        final long epochDay = 24 * 60 * 60 * 1000;
        Query feedingsQuery = FirebaseDatabase.getInstance().getReference("Cats").child("Grey").child("feedings").orderByChild("time").startAt(selectedData).endAt(selectedData + epochDay);
        feedingEntryFirebaseListAdapter =
                new FirebaseListAdapter<FeedingEntry>(this, FeedingEntry.class, R.layout.feeding_entry, feedingsQuery) {
                    @Override
                    protected void populateView(View v, final FeedingEntry model, int position) {
                        final ImageView feederImage = (ImageView) v.findViewById(R.id.feeder_image);
                        catLoadingView.dismiss();
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
                        ((TextView)v.findViewById(R.id.time)).setText(new java.text.SimpleDateFormat("EEEE, d MMMM HH:mm", Locale.getDefault()).format(new java.util.Date(model.getTime())));
                        ((TextView)v.findViewById(R.id.amount_text)).setText(String.format(Locale.getDefault(), "%d%%", model.getAmount()));

                        final WaveView cupLevelWaveView = (WaveView) v.findViewById(R.id.cup_level);
                        v.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                cupLevelWaveView.setProgress(model.getAmount());
                                return true;
                            }
                        });
                    }
                };
        entryList.setAdapter(feedingEntryFirebaseListAdapter);
    }

    @Override
    public void onDayChanged(Calendar calendar) {
        updateEntryViewAdapter(Common.roundEpochToDay(calendar.getTimeInMillis()));
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
