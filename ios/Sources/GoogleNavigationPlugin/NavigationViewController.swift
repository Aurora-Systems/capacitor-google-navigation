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
        let options = GMSMapViewOptions()
        options.frame = UIScreen.main.bounds
        let mapView = GMSMapView(options: options)
        self.mapView = mapView
        self.view = mapView
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        guard let mapView = mapView else { return }
        let enabled = mapView.enableNavigation(with: session)
        if enabled {
            mapView.cameraMode = .following
        }

        addCloseButton()
    }

    private func addCloseButton() {
        let button = UIButton(type: .system)
        button.setImage(UIImage(systemName: "xmark"), for: .normal)
        button.tintColor = .white
        button.backgroundColor = UIColor.black.withAlphaComponent(0.6)
        button.layer.cornerRadius = 20
        button.translatesAutoresizingMaskIntoConstraints = false
        button.addTarget(self, action: #selector(closeTapped), for: .touchUpInside)
        view.addSubview(button)

        NSLayoutConstraint.activate([
            button.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 16),
            button.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16),
            button.widthAnchor.constraint(equalToConstant: 40),
            button.heightAnchor.constraint(equalToConstant: 40)
        ])
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
