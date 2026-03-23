import UIKit
import GoogleMaps
import GoogleNavigation

class NavigationMapViewController: UIViewController {
    private let session: GMSNavigationSession
    private var mapView: GMSMapView?

    init(session: GMSNavigationSession) {
        self.session = session
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) not supported")
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        let options = GMSMapViewOptions()
        options.frame = view.bounds
        let mapView = GMSMapView(options: options)
        mapView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(mapView)
        self.mapView = mapView

        _ = mapView.enableNavigation(with: session)
        mapView.cameraMode = .following
    }

    func setCameraFollowing() {
        mapView?.cameraMode = .following
    }
}
