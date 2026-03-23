package com.attributeai.navigation;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

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
    private Runnable onCloseListener;

    public static NavigationFragment newInstance() {
        return new NavigationFragment();
    }

    public void setOnCloseListener(Runnable listener) {
        this.onCloseListener = listener;
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

        FrameLayout root = new FrameLayout(requireContext());
        root.addView(navigationView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ));

        Button closeButton = new Button(requireContext());
        closeButton.setText("✕");
        closeButton.setTextColor(Color.WHITE);
        closeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        closeButton.setTypeface(null, Typeface.BOLD);
        closeButton.setBackgroundColor(Color.argb(153, 0, 0, 0)); // 60% black
        closeButton.setOnClickListener(v -> {
            if (onCloseListener != null) onCloseListener.run();
        });

        int sizePx = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()
        );
        int marginPx = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()
        );

        FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(sizePx, sizePx);
        btnParams.gravity = Gravity.TOP | Gravity.START;
        btnParams.topMargin = marginPx;
        btnParams.leftMargin = marginPx;
        root.addView(closeButton, btnParams);

        return root;
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
