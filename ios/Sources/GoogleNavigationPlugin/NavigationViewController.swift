import UIKit
import GoogleMaps
import GoogleNavigation

class NavigationMapViewController: UIViewController {
    private let session: GMSNavigationSession
    private var mapView: GMSMapView?
    private var overlayWindow: UIWindow?
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

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        addCloseButtonWindow()
    }

    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        tearDownCloseButtonWindow()
    }

    // MARK: - Close button in a top-level UIWindow above all SDK UI

    private func addCloseButtonWindow() {
        guard let scene = view.window?.windowScene else { return }

        let window = UIWindow(windowScene: scene)
        window.windowLevel = .alert + 1
        window.backgroundColor = .clear
        window.isHidden = false

        let overlayVC = UIViewController()
        overlayVC.view.backgroundColor = .clear
        window.rootViewController = overlayVC

        let button = UIButton(type: .system)
        button.setImage(UIImage(systemName: "xmark"), for: .normal)
        button.tintColor = .white
        button.backgroundColor = UIColor.black.withAlphaComponent(0.6)
        button.layer.cornerRadius = 20
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: #selector(closeTapped), for: .touchUpInside)
        overlayVC.view.addSubview(button)

        NSLayoutConstraint.activate([
            button.topAnchor.constraint(equalTo: overlayVC.view.safeAreaLayoutGuide.topAnchor, constant: 16),
            button.leadingAnchor.constraint(equalTo: overlayVC.view.leadingAnchor, constant: 16),
            button.widthAnchor.constraint(equalToConstant: 40),
            button.heightAnchor.constraint(equalToConstant: 40),
        ])

        self.overlayWindow = window
    }

    private func tearDownCloseButtonWindow() {
        overlayWindow?.isHidden = true
        overlayWindow = nil
    }

    @objc private func closeTapped() {
        tearDownCloseButtonWindow()
        dismiss(animated: true) { [weak self] in
            self?.onDismiss?()
        }
    }

    func setCameraFollowing() {
        mapView?.cameraMode = .following
    }
}
