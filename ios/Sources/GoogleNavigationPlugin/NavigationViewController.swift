import UIKit
import GoogleMaps
import GoogleNavigation

class NavigationMapViewController: UIViewController {
    private let session: GMSNavigationSession
    private var mapView: GMSMapView?
    var onDismiss: (() -> Void)?

    init(session: GMSNavigationSession) {
        self.session = session
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) not supported")
    }

    override func loadView() {
        let container = UIView(frame: UIScreen.main.bounds)

        let options = GMSMapViewOptions()
        options.frame = container.bounds
        let mapView = GMSMapView(options: options)
        mapView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        container.addSubview(mapView)
        self.mapView = mapView

        // Overlay sits above the map and all Google-rendered UI
        let overlay = UIView()
        overlay.translatesAutoresizingMaskIntoConstraints = false
        overlay.isUserInteractionEnabled = true
        overlay.backgroundColor = .clear
        container.addSubview(overlay)
        NSLayoutConstraint.activate([
            overlay.topAnchor.constraint(equalTo: container.topAnchor),
            overlay.bottomAnchor.constraint(equalTo: container.bottomAnchor),
            overlay.leadingAnchor.constraint(equalTo: container.leadingAnchor),
            overlay.trailingAnchor.constraint(equalTo: container.trailingAnchor),
        ])

        let button = UIButton(type: .system)
        button.setImage(UIImage(systemName: "xmark"), for: .normal)
        button.tintColor = .white
        button.backgroundColor = UIColor.black.withAlphaComponent(0.6)
        button.layer.cornerRadius = 20
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: #selector(closeTapped), for: .touchUpInside)
        overlay.addSubview(button)
        NSLayoutConstraint.activate([
            button.topAnchor.constraint(equalTo: overlay.safeAreaLayoutGuide.topAnchor, constant: 16),
            button.leadingAnchor.constraint(equalTo: overlay.leadingAnchor, constant: 16),
            button.widthAnchor.constraint(equalToConstant: 40),
            button.heightAnchor.constraint(equalToConstant: 40),
        ])

        self.view = container
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        guard let mapView = mapView else { return }
        let enabled = mapView.enableNavigation(with: session)
        if enabled {
            mapView.cameraMode = .following
        }
    }

    @objc private func closeTapped() {
        dismiss(animated: true) { [weak self] in
            self?.onDismiss?()
        }
    }

    func setCameraFollowing() {
        mapView?.cameraMode = .following
    }
}
