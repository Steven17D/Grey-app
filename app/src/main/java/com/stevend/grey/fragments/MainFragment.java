package com.stevend.grey.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stevend.grey.R;

/**
 * Created by Steven on 6/23/2017.
 */

public class MainFragment extends Fragment {
    private static final MainFragment ourInstance = new MainFragment();

    public static MainFragment getInstance() {
        return ourInstance;
    }

    public MainFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        return view;
    }
}
