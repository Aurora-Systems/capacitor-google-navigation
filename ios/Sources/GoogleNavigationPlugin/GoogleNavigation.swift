import Foundation
import Capacitor
import GoogleMaps
import GoogleNavigation

@objc public class GoogleNavigation: NSObject {
    private weak var plugin: CAPPlugin?
    private var mapViewController: NavigationMapViewController?
    private var navigationSession: GMSNavigationSession?

    init(plugin: CAPPlugin) {
        self.plugin = plugin
        super.init()
    }

    func initialize(apiKey: String, completion: @escaping (Bool, String?) -> Void) {
        DispatchQueue.main.async {
            print("[GoogleNavigation] provideAPIKey called, key length: \(apiKey.count), prefix: \(String(apiKey.prefix(8)))...")
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

        DispatchQueue.main.async {
            let options = GMSNavigationTermsAndConditionsOptions(companyName: "App")
            GMSNavigationServices.showTermsAndConditionsDialogIfNeeded(with: options) { [weak self] termsAccepted in
                guard let self = self else { return }
                guard termsAccepted else {
                    completion(false, "Navigation terms and conditions not accepted")
                    return
                }

                guard let session = GMSNavigationServices.createNavigationSession() else {
                    completion(false, "Failed to create navigation session")
                    return
                }

                session.isStarted = true
                self.navigationSession = session
                session.navigator?.add(self)

                let mapVC = NavigationMapViewController(session: session)
                mapVC.modalPresentationStyle = .fullScreen
                mapVC.onDismiss = { [weak self] in
                    guard let self = self else { return }
                    self.navigationSession?.navigator?.remove(self)
                    self.navigationSession?.isStarted = false
                    self.navigationSession = nil
                    self.mapViewController = nil
                    self.plugin?.notifyListeners("onNavigationClosed", data: [:])
                }
                self.mapViewController = mapVC

                presentingVC.present(mapVC, animated: true) {
                    self.plugin?.notifyListeners("onNavigationReady", data: [:])
                    completion(true, nil)
                }
            }
        }
    }

    func dismissNavigationViewController(completion: @escaping () -> Void) {
        DispatchQueue.main.async {
            self.mapViewController?.dismiss(animated: true) {
                self.navigationSession?.navigator?.remove(self)
                self.navigationSession?.isStarted = false
                self.navigationSession = nil
                self.mapViewController = nil
                completion()
            }
        }
    }

    func startNavigation(
        lat: Double,
        lng: Double,
        travelMode: String,
        completion: @escaping (Bool, String?) -> Void
    ) {
        guard let session = navigationSession, let navigator = session.navigator else {
            completion(false, "Navigation view must be shown before starting navigation")
            return
        }

        let coordinate = CLLocationCoordinate2D(latitude: lat, longitude: lng)
        guard let waypoint = try? GMSNavigationWaypoint(location: coordinate, title: "Destination") else {
            completion(false, "Failed to create waypoint")
            return
        }

        switch travelMode {
        case "WALKING": session.travelMode = .walking
        case "CYCLING": session.travelMode = .cycling
        case "TWO_WHEELER": session.travelMode = .twoWheeler
        default: session.travelMode = .driving
        }

        navigator.setDestinations([waypoint]) { [weak self] routeStatus in
            if routeStatus == .OK {
                navigator.isGuidanceActive = true
                self?.mapViewController?.setCameraFollowing()
                completion(true, nil)
            } else {
                let reason: String
                switch routeStatus {
                case .apiKeyNotAuthorized:  reason = "API key not authorized for Navigation SDK"
                case .networkError:         reason = "Network error — check internet connection"
                case .noRouteFound:         reason = "No route found to destination"
                case .locationUnavailable:  reason = "Location unavailable — check permissions"
                case .quotaExceeded:        reason = "API quota exceeded"
                case .waypointError:        reason = "Invalid waypoint coordinates"
                case .travelModeUnsupported: reason = "Travel mode not supported"
                case .canceled:             reason = "Route request was canceled"
                default:                    reason = "Unknown error (code \(routeStatus.rawValue))"
                }
                completion(false, "Route calculation failed: \(reason)")
            }
        }
    }

    func stopNavigation() {
        navigationSession?.navigator?.isGuidanceActive = false
        navigationSession?.navigator?.clearDestinations()
    }
}

extension GoogleNavigation: GMSNavigatorListener {
    public func navigator(_ navigator: GMSNavigator, didArriveAt waypoint: GMSNavigationWaypoint) {
        plugin?.notifyListeners("onArrival", data: [
            "latitude": waypoint.coordinate.latitude,
            "longitude": waypoint.coordinate.longitude,
            "title": waypoint.title ?? ""
        ])
    }

    public func navigatorDidChangeRoute(_ navigator: GMSNavigator) {
        plugin?.notifyListeners("onRouteChanged", data: [:])
    }
}
