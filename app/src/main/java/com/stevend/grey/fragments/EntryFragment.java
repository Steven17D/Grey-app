package com.stevend.grey.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.stevend.grey.Feeder;
import com.stevend.grey.FeedingEntry;
import com.stevend.grey.R;

/**
 * Created by Steven on 6/23/2017.
 */

public class EntryFragment extends Fragment {
    private static final EntryFragment ourInstance = new EntryFragment();

    public static EntryFragment getInstance() {
        return ourInstance;
    }

    public EntryFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_entry, container, false);
        Button entryButton = (Button) v.findViewById(R.id.entry_button);
        entryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null){
                    FirebaseDatabase.getInstance().getReference("Cats").child("Grey").child("feedings").push()
                            .setValue(new FeedingEntry(new Feeder(currentUser), System.currentTimeMillis(), 100));
                }
            }
        });
        return v;
    }
}
