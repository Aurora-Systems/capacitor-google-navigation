import Foundation
import Capacitor

@objc(GoogleNavigationPlugin)
public class GoogleNavigationPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "GoogleNavigationPlugin"
    public let jsName = "GoogleNavigation"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "initialize", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "startNavigation", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "stopNavigation", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "showNavigationView", returnType: CAPPluginReturnPromise),
    ]
    private lazy var implementation = GoogleNavigation(plugin: self)

    @objc func initialize(_ call: CAPPluginCall) {
        let jsKey = call.getString("apiKey") ?? ""
        let resolvedKey = !jsKey.isEmpty
            ? jsKey
            : Bundle.main.object(forInfoDictionaryKey: "GoogleNavigationAPIKey") as? String ?? ""

        guard !resolvedKey.isEmpty else {
            call.reject("No API key found. Add GoogleNavigationAPIKey to Info.plist (recommended) or pass apiKey to initialize().")
            return
        }

        implementation.initialize(apiKey: resolvedKey) { success, error in
            if let error = error {
                call.reject(error)
            } else {
                call.resolve(["success": success])
            }
        }
    }

    @objc func startNavigation(_ call: CAPPluginCall) {
        guard let lat = call.getDouble("destinationLatitude"),
              let lng = call.getDouble("destinationLongitude") else {
            call.reject("destinationLatitude and destinationLongitude are required")
            return
        }
        let travelMode = call.getString("travelMode") ?? "DRIVING"
        implementation.startNavigation(lat: lat, lng: lng, travelMode: travelMode) { success, error in
            if let error = error {
                call.reject(error)
            } else {
                call.resolve(["success": success])
            }
        }
    }

    @objc func stopNavigation(_ call: CAPPluginCall) {
        implementation.stopNavigation()
        call.resolve(["success": true])
    }

    @objc func showNavigationView(_ call: CAPPluginCall) {
        let show = call.getBool("show") ?? true
        DispatchQueue.main.async {
            if show {
                self.implementation.presentNavigationViewController(from: self.bridge?.viewController) { success, error in
                    if let error = error {
                        call.reject(error)
                    } else {
                        call.resolve(["success": success])
                    }
                }
            } else {
                self.implementation.dismissNavigationViewController {
                    call.resolve(["success": true])
                }
            }
        }
    }
}
