package com.attributeai.navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.libraries.navigation.NavigationView;

/**
 * Full-screen fragment that hosts the Google Navigation SDK's NavigationView.
 * All lifecycle methods must be delegated to NavigationView — failing to do so
 * results in a blank map or crash.
 */
public class NavigationFragment extends Fragment {

    private NavigationView navigationView;

    public static NavigationFragment newInstance() {
        return new NavigationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        navigationView = new NavigationView(requireContext());
        navigationView.onCreate(savedInstanceState);
        return navigationView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (navigationView != null) navigationView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (navigationView != null) navigationView.onResume();
    }

    @Override
    public void onPause() {
        if (navigationView != null) navigationView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (navigationView != null) navigationView.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (navigationView != null) navigationView.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (navigationView != null) navigationView.onSaveInstanceState(outState);
    }
}
