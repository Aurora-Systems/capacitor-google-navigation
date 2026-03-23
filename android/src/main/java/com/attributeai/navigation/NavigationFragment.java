package com.attributeai.navigation;

import android.app.Activity;
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
    private Button closeButton;
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
        return navigationView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (navigationView != null) navigationView.onResume();
        attachCloseButtonToDecorView();
    }

    @Override
    public void onPause() {
        detachCloseButtonFromDecorView();
        if (navigationView != null) navigationView.onPause();
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (navigationView != null) navigationView.onStart();
    }

    @Override
    public void onStop() {
        if (navigationView != null) navigationView.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        detachCloseButtonFromDecorView();
        if (navigationView != null) navigationView.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (navigationView != null) navigationView.onSaveInstanceState(outState);
    }

    // MARK: - Close button attached to the Activity decor view, above all SDK UI

    private void attachCloseButtonToDecorView() {
        Activity activity = getActivity();
        if (activity == null || closeButton != null) return;

        int sizePx = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()
        );
        int marginPx = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()
        );
        int statusBarHeight = getStatusBarHeight(activity);

        Button button = new Button(requireContext());
        button.setText("✕");
        button.setTextColor(Color.WHITE);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        button.setTypeface(null, Typeface.BOLD);
        button.setBackgroundColor(Color.argb(153, 0, 0, 0));
        button.setOnClickListener(v -> {
            if (onCloseListener != null) onCloseListener.run();
        });

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizePx, sizePx);
        params.gravity = Gravity.TOP | Gravity.START;
        params.topMargin = statusBarHeight + marginPx;
        params.leftMargin = marginPx;

        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        decorView.addView(button, params);
        this.closeButton = button;
    }

    private void detachCloseButtonFromDecorView() {
        Activity activity = getActivity();
        if (activity == null || closeButton == null) return;
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        decorView.removeView(closeButton);
        closeButton = null;
    }

    private int getStatusBarHeight(Activity activity) {
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? activity.getResources().getDimensionPixelSize(resourceId) : 0;
    }
}
