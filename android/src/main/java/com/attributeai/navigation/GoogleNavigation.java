package com.attributeai.navigation;

import android.app.Activity;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.google.android.libraries.navigation.NavigationApi;
import com.google.android.libraries.navigation.Navigator;
import com.google.android.libraries.navigation.RoutingOptions;
import com.google.android.libraries.navigation.Waypoint;

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
        NavigationApi.getNavigator(
            plugin.getActivity().getApplication(),
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

        Waypoint destination;
        try {
            destination = new Waypoint.Builder()
                .setLatLng(lat, lng)
                .setTitle("Destination")
                .build();
        } catch (Waypoint.UnsupportedTravelModeException e) {
            callback.onResult(false, "Unsupported travel mode");
            return;
        }

        RoutingOptions.TravelMode mode;
        switch (travelMode) {
            case "WALKING": mode = RoutingOptions.TravelMode.WALKING; break;
            case "CYCLING": mode = RoutingOptions.TravelMode.CYCLING; break;
            case "TWO_WHEELER": mode = RoutingOptions.TravelMode.TWO_WHEELER; break;
            default: mode = RoutingOptions.TravelMode.DRIVING; break;
        }

        RoutingOptions routingOptions = new RoutingOptions.Builder()
            .travelMode(mode)
            .build();

        navigator.setDestination(destination, routingOptions,
            new Navigator.RouteStatusListener() {
                @Override
                public void onRouteStatusResult(Navigator.RouteStatus status) {
                    if (status == Navigator.RouteStatus.OK) {
                        navigator.startGuidance();
                        callback.onResult(true, null);
                    } else {
                        callback.onResult(false, "Route error: " + status.name());
                    }
                }
            }
        );
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

        navigator.addArrivalListener(waypoint -> {
            JSObject data = new JSObject();
            data.put("latitude", waypoint.getPosition().latitude);
            data.put("longitude", waypoint.getPosition().longitude);
            data.put("title", waypoint.getTitle());
            plugin.fireEvent("onArrival", data);
        });

        navigator.addRouteChangedListener(() -> {
            plugin.fireEvent("onRouteChanged", new JSObject());
        });
    }
}
