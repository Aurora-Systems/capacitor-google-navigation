# capacitor-google-navigation

A Capacitor 8 plugin for Google Navigation SDK — turn-by-turn navigation inside your iOS and Android app.

> **Native only.** This plugin has no web implementation. It must be run on a physical or emulated iOS/Android device.

---

## Requirements

### Google Cloud Console

1. Open or create a project at [console.cloud.google.com](https://console.cloud.google.com)
2. Enable the **Navigation SDK** API
3. Enable **billing** on your project
4. Create an API key under **APIs & Services → Credentials**

### iOS

- iOS 15.0+
- Xcode 14+
- CocoaPods — **Swift Package Manager is not supported** (Google Navigation SDK has no SPM distribution)

### Android

- Android API 24 (Android 7.0)+
- Google Play Services on the device

---

## Installation

```bash
npm install capacitor-google-navigation
npx cap sync
```

---

## iOS Setup

### 1. Install pods

```bash
cd ios/App
pod install
```

The `GoogleNavigation ~> 9.0` pod is declared in the plugin's podspec and is pulled in automatically.

### 2. Add location permissions to `Info.plist`

```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>This app uses your location for turn-by-turn navigation.</string>
<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
<string>This app uses your location for navigation, including in the background.</string>
```

### 3. Register your API key in `AppDelegate.swift`

The iOS SDK requires the key to be provided before any map or navigator is created.

```swift
import UIKit
import Capacitor
import CapacitorGoogleNavigation
import GoogleNavigation  // required for GMSServices

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        GMSServices.provideAPIKey("YOUR_IOS_API_KEY")
        return true
    }
}
```

> You can also pass the key via `GoogleNavigation.initialize({ apiKey })` at runtime — the plugin calls `GMSServices.provideAPIKey()` for you. Either approach works; calling it in `AppDelegate` is the earlier-initialization option.

---

## Android Setup

### 1. Add the API key and permissions to `android/app/src/main/AndroidManifest.xml`

```xml
<manifest>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

    <application>
        <!-- Required: Navigation SDK reads this at startup -->
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="YOUR_ANDROID_API_KEY" />
    </application>
</manifest>
```

> **Important:** On Android the Navigation SDK reads the API key from `AndroidManifest.xml` — not from the `apiKey` parameter passed to `initialize()`. The `apiKey` parameter is used on iOS only.

### 2. Request location permission at runtime

The plugin does not request permissions itself. You must request `ACCESS_FINE_LOCATION` before calling `initialize()`. In an Ionic React app use `@capacitor/geolocation` or the browser Permissions API:

```typescript
import { Geolocation } from '@capacitor/geolocation';

await Geolocation.requestPermissions();
```

---

## How it works

Calling `showNavigationView({ show: true })` presents a **full-screen native navigation UI** on top of your app. Your Ionic/web UI stays alive underneath. Calling `showNavigationView({ show: false })` dismisses the native view and restores your app UI.

**Call order:**

```
initialize()
    ↓  fires onNavigationReady when SDK is ready
showNavigationView({ show: true })
    ↓  presents native map full-screen
startNavigation({ lat, lng, travelMode })
    ↓  sets destination, begins guidance
    ↓  fires onArrival when user arrives
    ↓  fires onRouteChanged on recalculation
stopNavigation()
    ↓  ends guidance, clears destination
showNavigationView({ show: false })  — or user taps the ✕ close button
    ↓  dismisses native map, fires onNavigationClosed, restores app UI
```

---

## Usage — Ionic React

### 1. Create a `useGoogleNavigation` hook

```typescript
// src/hooks/useGoogleNavigation.ts
import { useEffect, useRef, useCallback } from 'react';
import { GoogleNavigation } from 'capacitor-google-navigation';
import type { PluginListenerHandle } from 'capacitor-google-navigation';

interface UseNavigationOptions {
  apiKey: string;
  onArrival?: (event: any) => void;
  onRouteChanged?: () => void;
  onNavigationClosed?: () => void;
}

export function useGoogleNavigation({ apiKey, onArrival, onRouteChanged, onNavigationClosed }: UseNavigationOptions) {
  const listeners = useRef<PluginListenerHandle[]>([]);

  useEffect(() => {
    const setup = async () => {
      const readyHandle = await GoogleNavigation.addListener('onNavigationReady', () => {
        console.log('Navigation SDK ready');
      });
      listeners.current.push(readyHandle);

      if (onArrival) {
        const h = await GoogleNavigation.addListener('onArrival', onArrival);
        listeners.current.push(h);
      }

      if (onRouteChanged) {
        const h = await GoogleNavigation.addListener('onRouteChanged', onRouteChanged);
        listeners.current.push(h);
      }

      if (onNavigationClosed) {
        const h = await GoogleNavigation.addListener('onNavigationClosed', onNavigationClosed);
        listeners.current.push(h);
      }

      await GoogleNavigation.initialize({ apiKey });
    };

    setup().catch(console.error);

    return () => {
      listeners.current.forEach(h => h.remove());
      listeners.current = [];
    };
  }, [apiKey]);

  const navigate = useCallback(async (
    latitude: number,
    longitude: number,
    travelMode: 'DRIVING' | 'WALKING' | 'CYCLING' | 'TWO_WHEELER' = 'DRIVING',
  ) => {
    await GoogleNavigation.showNavigationView({ show: true });
    await GoogleNavigation.startNavigation({
      destinationLatitude: latitude,
      destinationLongitude: longitude,
      travelMode,
    });
  }, []);

  const stop = useCallback(async () => {
    await GoogleNavigation.stopNavigation();
    await GoogleNavigation.showNavigationView({ show: false });
  }, []);

  return { navigate, stop };
}
```

### 2. Use the hook in a page

```tsx
// src/pages/NavigationPage.tsx
import React from 'react';
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButton,
  IonAlert,
} from '@ionic/react';
import { useGoogleNavigation } from '../hooks/useGoogleNavigation';

const NavigationPage: React.FC = () => {
  const [showArrival, setShowArrival] = React.useState(false);

  const { navigate, stop } = useGoogleNavigation({
    apiKey: import.meta.env.VITE_GOOGLE_NAV_API_KEY as string,
    onArrival: () => setShowArrival(true),
    onRouteChanged: () => console.log('Route recalculated'),
    onNavigationClosed: () => console.log('User closed navigation'),
  });

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Navigation</IonTitle>
        </IonToolbar>
      </IonHeader>

      <IonContent className="ion-padding">
        <IonButton
          expand="block"
          onClick={() => navigate(37.7749, -122.4194, 'DRIVING')}
        >
          Navigate to San Francisco
        </IonButton>

        <IonButton expand="block" color="medium" onClick={() => navigate(34.0522, -118.2437, 'WALKING')}>
          Walk to Los Angeles
        </IonButton>

        <IonButton expand="block" color="danger" onClick={stop}>
          Stop Navigation
        </IonButton>
      </IonContent>

      <IonAlert
        isOpen={showArrival}
        header="Arrived!"
        message="You have reached your destination."
        buttons={['OK']}
        onDidDismiss={() => setShowArrival(false)}
      />
    </IonPage>
  );
};

export default NavigationPage;
```

### 3. Store your API key in `.env`

Create a `.env` file in your app root (never commit this):

```
VITE_GOOGLE_NAV_API_KEY=AIzaSy...
```

---

## Usage — Vanilla TypeScript / JavaScript

```typescript
import { GoogleNavigation } from 'capacitor-google-navigation';

// 1. Attach event listeners first
const readyHandle = await GoogleNavigation.addListener('onNavigationReady', () => {
  console.log('SDK ready');
});

const arrivalHandle = await GoogleNavigation.addListener('onArrival', (event) => {
  console.log('Arrived:', event);
});

const routeHandle = await GoogleNavigation.addListener('onRouteChanged', () => {
  console.log('Route recalculated');
});

const closedHandle = await GoogleNavigation.addListener('onNavigationClosed', () => {
  // Fired when the user taps the ✕ close button on the native navigation view
  console.log('Navigation closed by user');
});

// 2. Initialize the SDK (fires onNavigationReady when done)
await GoogleNavigation.initialize({ apiKey: 'YOUR_API_KEY' });

// 3. Show the native navigation view
await GoogleNavigation.showNavigationView({ show: true });

// 4. Start navigation
await GoogleNavigation.startNavigation({
  destinationLatitude: 37.7749,
  destinationLongitude: -122.4194,
  travelMode: 'DRIVING', // DRIVING | WALKING | CYCLING | TWO_WHEELER
});

// 5. Stop guidance and dismiss
await GoogleNavigation.stopNavigation();
await GoogleNavigation.showNavigationView({ show: false });

// 6. Clean up
await readyHandle.remove();
await arrivalHandle.remove();
await routeHandle.remove();
// or:
await GoogleNavigation.removeAllListeners();
```

---

## API

<!-- docgen-api -->
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### initialize(...)

```typescript
initialize(options: { apiKey: string; }) => Promise<{ success: boolean; }>
```

Initialize the Navigation SDK with API key

| Param         | Type                             |
| ------------- | -------------------------------- |
| **`options`** | <code>{ apiKey: string; }</code> |

**Returns:** <code>Promise&lt;{ success: boolean; }&gt;</code>

--------------------


### startNavigation(...)

```typescript
startNavigation(options: { destinationLatitude: number; destinationLongitude: number; travelMode?: 'DRIVING' | 'WALKING' | 'CYCLING' | 'TWO_WHEELER'; }) => Promise<{ success: boolean; }>
```

Start navigation to a destination

| Param         | Type                                                                                                                                      |
| ------------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ destinationLatitude: number; destinationLongitude: number; travelMode?: 'DRIVING' \| 'WALKING' \| 'CYCLING' \| 'TWO_WHEELER'; }</code> |

**Returns:** <code>Promise&lt;{ success: boolean; }&gt;</code>

--------------------


### stopNavigation()

```typescript
stopNavigation() => Promise<{ success: boolean; }>
```

Stop navigation

**Returns:** <code>Promise&lt;{ success: boolean; }&gt;</code>

--------------------


### showNavigationView(...)

```typescript
showNavigationView(options: { show: boolean; }) => Promise<{ success: boolean; }>
```

Show/hide navigation view

| Param         | Type                             |
| ------------- | -------------------------------- |
| **`options`** | <code>{ show: boolean; }</code>  |

**Returns:** <code>Promise&lt;{ success: boolean; }&gt;</code>

--------------------


### addListener(...)

```typescript
addListener(eventName: 'onArrival' | 'onRouteChanged' | 'onNavigationReady' | 'onNavigationClosed', listenerFunc: (event: any) => void) => Promise<PluginListenerHandle>
```

Add listener for navigation events

| Param              | Type                                                              |
| ------------------ | ----------------------------------------------------------------- |
| **`eventName`**    | <code>'onArrival' \| 'onRouteChanged' \| 'onNavigationReady' \| 'onNavigationClosed'</code> |
| **`listenerFunc`** | <code>(event: any) =&gt; void</code>                              |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

Remove all listeners

--------------------


### Interfaces


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |

<!-- docgen-api-end -->

---

## Events

| Event | Payload | Fired when |
|-------|---------|-----------|
| `onNavigationReady` | `{}` | SDK has initialized and the navigator is available |
| `onArrival` | `{ latitude, longitude, title }` | User arrives at the destination waypoint |
| `onRouteChanged` | `{}` | The route is recalculated (traffic, missed turn, etc.) |
| `onNavigationClosed` | `{}` | User tapped the ✕ close button on the native navigation view (iOS) |

---

## Platform notes

| Feature | iOS | Android |
|---------|-----|---------|
| Turn-by-turn guidance | ✅ | ✅ |
| Full-screen native UI | ✅ | ✅ |
| `onArrival` event | ✅ | ✅ |
| `onRouteChanged` event | ✅ | ✅ |
| API key via `initialize()` | ✅ | ⚠️ Manifest only |
| Web | ❌ | ❌ |
| Swift Package Manager | ❌ | — |

---

## Troubleshooting

**`showNavigationView` must be called before `startNavigation`**
The navigator instance is only available after the native navigation view is presented. Calling `startNavigation` without first calling `showNavigationView({ show: true })` will return an error.

**Blank map on Android**
All `NavigationView` lifecycle events must be delegated — the plugin handles this via `NavigationFragment`. If you see a blank map, check that your `Activity` extends `AppCompatActivity` (Capacitor does this by default).

**`initialize()` fails on Android**
The Android Navigation SDK validates the API key from `AndroidManifest.xml`. Ensure the key is present and the Navigation SDK is enabled in your Google Cloud project with billing active.

**iOS — "This app has attempted to access privacy-sensitive data"**
Add both `NSLocationWhenInUseUsageDescription` and `NSLocationAlwaysAndWhenInUseUsageDescription` to `Info.plist` before calling `initialize()`.

**CocoaPods not found / pod install fails**
Make sure CocoaPods is installed (`sudo gem install cocoapods`) and run `npx cap sync` before `pod install`.

**iOS — `cap sync` fails with "transitive dependencies that include statically linked binaries"**
The GoogleNavigation SDK is distributed as a static XCFramework, which conflicts with the default dynamic `use_frameworks!` directive. In your app's `ios/App/Podfile`, change:
```ruby
use_frameworks!
```
to:
```ruby
use_frameworks! :linkage => :static
```
Then re-run `pod install` and `npx cap sync`.

---

## License

MIT
