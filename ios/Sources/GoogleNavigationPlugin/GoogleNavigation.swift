import Foundation
import GoogleNavigation

@objc public class GoogleNavigation: NSObject {
    private weak var plugin: CAPPlugin?
    private var navViewController: GMSNavigationViewController?
    private var navigator: GMSNavigator?

    init(plugin: CAPPlugin) {
        self.plugin = plugin
        super.init()
    }

    func initialize(apiKey: String, completion: @escaping (Bool, String?) -> Void) {
        DispatchQueue.main.async {
            GMSServices.provideAPIKey(apiKey)
            completion(true, nil)
        }
    }

    func presentNavigationViewController(
        from viewController: UIViewController?,
        completion: @escaping (Bool, String?) -> Void
    ) {
        guard let presentingVC = viewController else {
            completion(false, "No presenting view controller available")
            return
        }

        let navVC = GMSNavigationViewController()
        navVC.modalPresentationStyle = .fullScreen
        navVC.delegate = self
        self.navViewController = navVC
        self.navigator = navVC.mapView.navigator

        presentingVC.present(navVC, animated: true) {
            self.plugin?.notifyListeners("onNavigationReady", data: [:])
            completion(true, nil)
        }
    }

    func dismissNavigationViewController(completion: @escaping () -> Void) {
        navViewController?.dismiss(animated: true) {
            self.navViewController = nil
            self.navigator = nil
            completion()
        }
    }

    func startNavigation(
        lat: Double,
        lng: Double,
        travelMode: String,
        completion: @escaping (Bool, String?) -> Void
    ) {
        guard let navigator = self.navigator else {
            completion(false, "Navigation view must be shown before starting navigation")
            return
        }

        let coordinate = CLLocationCoordinate2D(latitude: lat, longitude: lng)
        guard let waypoint = try? GMSNavigationWaypoint(location: coordinate, title: "Destination") else {
            completion(false, "Failed to create waypoint")
            return
        }

        let mode: GMSNavigationTravelMode
        switch travelMode {
        case "WALKING": mode = .walking
        case "CYCLING": mode = .cycling
        case "TWO_WHEELER": mode = .twoWheeler
        default: mode = .driving
        }
        navViewController?.mapView.travelMode = mode

        navigator.setDestinations([waypoint]) { routeStatus in
            if routeStatus == .OK {
                navigator.isGuidanceActive = true
                self.navViewController?.mapView.cameraMode = .following
                completion(true, nil)
            } else {
                completion(false, "Route calculation failed: \(routeStatus.rawValue)")
            }
        }
    }

    func stopNavigation() {
        navigator?.isGuidanceActive = false
        navigator?.clearDestinations()
    }
}

extension GoogleNavigation: GMSNavigationViewControllerDelegate {
    public func navigationViewController(
        _ navigationViewController: GMSNavigationViewController,
        didArriveAtWaypoint waypoint: GMSNavigationWaypoint
    ) {
        plugin?.notifyListeners("onArrival", data: [
            "latitude": waypoint.coordinate.latitude,
            "longitude": waypoint.coordinate.longitude,
            "title": waypoint.title ?? ""
        ])
    }

    public func navigationViewControllerDidChangeRoute(
        _ navigationViewController: GMSNavigationViewController
    ) {
        plugin?.notifyListeners("onRouteChanged", data: [:])
    }
}
