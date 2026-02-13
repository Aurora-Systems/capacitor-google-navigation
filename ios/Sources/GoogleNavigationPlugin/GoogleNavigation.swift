import Foundation

@objc public class GoogleNavigation: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
