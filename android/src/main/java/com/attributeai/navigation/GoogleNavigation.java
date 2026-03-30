package com.attributeai.navigation;

import android.app.Activity;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.google.android.libraries.navigation.ArrivalEvent;
import com.google.android.libraries.navigation.NavigationApi;
import com.google.android.libraries.navigation.Navigator;

import com.google.android.libraries.navigation.Waypoint;

import java.util.ArrayList;
import java.util.List;

public class GoogleNavigation {

    interface Callback {
        void onResult(boolean success, String error);
    }

    private final GoogleNavigationPlugin plugin;
    private Navigator navigator;
    private static final String TAG = "GoogleNavigation";
    private static final String FRAGMENT_TAG = "GoogleNavigationFragment";

    public GoogleNavigation(GoogleNavigationPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize(String apiKey, Callback callback) {
        // On Android, the Navigation SDK reads the API key from AndroidManifest
        // <meta-data android:name="com.google.android.geo.API_KEY" android:value="..."/>
        // The apiKey param is accepted for API consistency but not injected programmatically.
        Activity activity = plugin.getActivity();

        if (!NavigationApi.areTermsAccepted(activity.getApplication())) {
            activity.runOnUiThread(() ->
                NavigationApi.showTermsAndConditionsDialog(
                    activity,
                    activity.getApplicationInfo().loadLabel(activity.getPackageManager()).toString(),
                    accepted -> {
                        if (accepted) {
                            getNavigator(activity, callback);
                        } else {
                            callback.onResult(false, "User declined Navigation terms and conditions");
                        }
                    }
                )
            );
        } else {
            getNavigator(activity, callback);
        }
    }

    private void getNavigator(Activity activity, Callback callback) {
        NavigationApi.getNavigator(
            activity.getApplication(),
            new NavigationApi.NavigatorListener() {
                @Override
                public void onNavigatorReady(Navigator nav) {
                    navigator = nav;
                    attachListeners();
                    JSObject data = new JSObject();
                    plugin.fireEvent("onNavigationReady", data);
                    callback.onResult(true, null);
                }

                @Override
                public void onError(int errorCode) {
                    String msg = "NavigationApi error code: " + errorCode;
                    Logger.error(TAG, msg, null);
                    callback.onResult(false, msg);
                }
            }
        );
    }

    public void startNavigation(double lat, double lng, String travelMode, Callback callback) {
        if (navigator == null) {
            callback.onResult(false, "Call initialize() first");
            return;
        }

        Waypoint destination = new Waypoint.Builder()
            .setLatLng(lat, lng)
            .setTitle("Destination")
            .build();

        List<Waypoint> destinations = new ArrayList<>();
        destinations.add(destination);

        navigator.setDestinations(destinations)
            .setOnResultListener(status -> {
                if (status == Navigator.RouteStatus.OK) {
                    navigator.startGuidance();
                    callback.onResult(true, null);
                } else {
                    callback.onResult(false, "Route error: " + status.name());
                }
            });
    }

    public void stopNavigation() {
        if (navigator != null) {
            navigator.stopGuidance();
            navigator.clearDestinations();
        }
    }

    public void showNavigationView(Activity activity, Callback callback) {
        if (!(activity instanceof FragmentActivity)) {
            callback.onResult(false, "Activity must be a FragmentActivity");
            return;
        }
        FragmentActivity fragmentActivity = (FragmentActivity) activity;
        FragmentManager fm = fragmentActivity.getSupportFragmentManager();

        if (fm.findFragmentByTag(FRAGMENT_TAG) != null) {
            callback.onResult(true, null);
            return;
        }

        NavigationFragment fragment = NavigationFragment.newInstance();
        fragment.setOnCloseListener(() -> {
            fm.beginTransaction().remove(fragment).commitAllowingStateLoss();
            plugin.fireEvent("onNavigationClosed", new JSObject());
        });
        fm.beginTransaction()
            .add(android.R.id.content, fragment, FRAGMENT_TAG)
            .commitAllowingStateLoss();
        callback.onResult(true, null);
    }

    public void hideNavigationView(Activity activity) {
        if (!(activity instanceof FragmentActivity)) return;
        FragmentActivity fragmentActivity = (FragmentActivity) activity;
        FragmentManager fm = fragmentActivity.getSupportFragmentManager();
        androidx.fragment.app.Fragment fragment = fm.findFragmentByTag(FRAGMENT_TAG);
        if (fragment != null) {
            fm.beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
    }

    private void attachListeners() {
        if (navigator == null) return;

        navigator.addArrivalListener(new Navigator.ArrivalListener() {
            @Override
            public void onArrival(ArrivalEvent arrivalEvent) {
                Waypoint wp = arrivalEvent.getWaypoint();
                JSObject data = new JSObject();
                data.put("latitude", wp.getPosition().latitude);
                data.put("longitude", wp.getPosition().longitude);
                data.put("title", wp.getTitle());
                plugin.fireEvent("onArrival", data);
            }
        });

        navigator.addRouteChangedListener(new Navigator.RouteChangedListener() {
            @Override
            public void onRouteChanged() {
                plugin.fireEvent("onRouteChanged", new JSObject());
            }
        });
    }
}
