package com.attributeai.navigation;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "GoogleNavigation")
public class GoogleNavigationPlugin extends Plugin {

    private GoogleNavigation implementation;

    @Override
    public void load() {
        implementation = new GoogleNavigation(this);
    }

    void fireEvent(String eventName, JSObject data) {
        notifyListeners(eventName, data);
    }

    @PluginMethod
    public void initialize(PluginCall call) {
        String apiKey = call.getString("apiKey", "");
        implementation.initialize(apiKey, (success, error) -> {
            if (error != null) {
                call.reject(error);
            } else {
                JSObject ret = new JSObject();
                ret.put("success", true);
                call.resolve(ret);
            }
        });
    }

    @PluginMethod
    public void startNavigation(PluginCall call) {
        Double lat = call.getDouble("destinationLatitude");
        Double lng = call.getDouble("destinationLongitude");
        if (lat == null || lng == null) {
            call.reject("destinationLatitude and destinationLongitude are required");
            return;
        }
        String travelMode = call.getString("travelMode", "DRIVING");
        implementation.startNavigation(lat, lng, travelMode, (success, error) -> {
            if (error != null) {
                call.reject(error);
            } else {
                JSObject ret = new JSObject();
                ret.put("success", true);
                call.resolve(ret);
            }
        });
    }

    @PluginMethod
    public void stopNavigation(PluginCall call) {
        implementation.stopNavigation();
        JSObject ret = new JSObject();
        ret.put("success", true);
        call.resolve(ret);
    }

    @PluginMethod
    public void showNavigationView(PluginCall call) {
        boolean show = Boolean.TRUE.equals(call.getBoolean("show", true));
        getActivity().runOnUiThread(() -> {
            if (show) {
                implementation.showNavigationView(getActivity(), (success, error) -> {
                    if (error != null) {
                        call.reject(error);
                    } else {
                        JSObject ret = new JSObject();
                        ret.put("success", true);
                        call.resolve(ret);
                    }
                });
            } else {
                implementation.hideNavigationView(getActivity());
                JSObject ret = new JSObject();
                ret.put("success", true);
                call.resolve(ret);
            }
        });
    }
}
