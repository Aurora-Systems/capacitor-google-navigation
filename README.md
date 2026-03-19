# capacitor-google-navigation

A Capacitor plugin for Google Navigation SDK — turn-by-turn navigation inside your iOS and Android app.

> **Native only.** This plugin requires a physical or emulated iOS/Android device. It has no web implementation.

---

## Requirements

### Google Cloud Console
1. Create or open a project at [console.cloud.google.com](https://console.cloud.google.com)
2. Enable the **Navigation SDK** API for your project
3. Enable **billing** on your project
4. Create an API key under **APIs & Services > Credentials**

### iOS
- iOS 15.0 or later
- Xcode 14+
- CocoaPods (the plugin is **not** available via Swift Package Manager — Google Navigation SDK has no SPM distribution)
- Add the `GoogleNavigation` pod (handled automatically via `pod install`)

**Location permissions** — add to your app's `Info.plist`:
```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>This app uses your location for turn-by-turn navigation.</string>
<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
<string>This app uses your location for navigation, including in the background.</string>
```

**API key** — add to `AppDelegate.swift`:
```swift
import GoogleNavigation

func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: ...) -> Bool {
    GMSServices.provideAPIKey("YOUR_IOS_API_KEY")
    return true
}
```

### Android
- Android API 24 (Android 7.0) or later
- Google Play Services installed on the device

**API key and permissions** — add to `android/app/src/main/AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

<application>
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="YOUR_ANDROID_API_KEY" />
</application>
```

> **Note:** On Android, the API key is read from `AndroidManifest.xml` at startup by the Navigation SDK. The `apiKey` passed to `initialize()` is used on iOS only.

---

## Install

```bash
npm install capacitor-google-navigation
npx cap sync
```

---

## How it works

When you call `showNavigationView({ show: true })`, the plugin presents a **full-screen native navigation UI** on top of your app. Your Ionic/web UI is still alive underneath. When you call `showNavigationView({ show: false })`, the native view is dismissed and your app UI is restored.

**Call order matters:**
1. `initialize()` — sets up the Navigation SDK, fires `onNavigationReady` when ready
2. `showNavigationView({ show: true })` — presents the native map UI
3. `startNavigation()` — sets the destination and begins guidance
4. `stopNavigation()` — ends guidance
5. `showNavigationView({ show: false })` — dismisses the native map UI

---

## Usage with React / Ionic React

### Basic hook

```typescript
// hooks/useGoogleNavigation.ts
import { useEffect, useRef, useCallback } from 'react';
import { GoogleNavigation } from 'capacitor-google-navigation';
import type { PluginListenerHandle } from 'capacitor-google-navigation';

interface UseNavigationOptions {
  apiKey: string;
  onArrival?: () => void;
  onRouteChanged?: () => void;
}

export function useGoogleNavigation({ apiKey, onArrival, onRouteChanged }: UseNavigationOptions) {
  const listeners = useRef<PluginListenerHandle[]>([]);

  useEffect(() => {
    const setup = async () => {
      if (onArrival) {
        const h = await GoogleNavigation.addListener('onArrival', onArrival);
        listeners.current.push(h);
      }
      if (onRouteChanged) {
        const h = await GoogleNavigation.addListener('onRouteChanged', onRouteChanged);
        listeners.current.push(h);
      }

      const readyHandle = await GoogleNavigation.addListener('onNavigationReady', () => {
        console.log('Navigation SDK ready');
      });
      listeners.current.push(readyHandle);

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
    travelMode?: 'DRIVING' | 'WALKING' | 'CYCLING' | 'TWO_WHEELER'
  ) => {
    await GoogleNavigation.showNavigationView({ show: true });
    await GoogleNavigation.startNavigation({ destinationLatitude: latitude, destinationLongitude: longitude, travelMode });
  }, []);

  const stop = useCallback(async () => {
    await GoogleNavigation.stopNavigation();
    await GoogleNavigation.showNavigationView({ show: false });
  }, []);

  return { navigate, stop };
}
```

### Example page

```tsx
// pages/NavigationPage.tsx
import React from 'react';
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButton,
} from '@ionic/react';
import { useGoogleNavigation } from '../hooks/useGoogleNavigation';

const NavigationPage: React.FC = () => {
  const { navigate, stop } = useGoogleNavigation({
    apiKey: import.meta.env.VITE_GOOGLE_NAV_API_KEY,
    onArrival: () => alert('You have arrived!'),
    onRouteChanged: () => console.log('Route recalculated'),
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
        <IonButton expand="block" color="danger" onClick={stop}>
          Stop Navigation
        </IonButton>
      </IonContent>
    </IonPage>
  );
};

export default NavigationPage;
```

### Storing the API key

Add your key to a `.env` file at the root of your app (do not commit this file):

```
VITE_GOOGLE_NAV_API_KEY=your_api_key_here
```

---

## Usage without a framework

```typescript
import { GoogleNavigation } from 'capacitor-google-navigation';

// 1. Listen for events
await GoogleNavigation.addListener('onNavigationReady', () => {
  console.log('Ready');
});

await GoogleNavigation.addListener('onArrival', () => {
  console.log('Arrived at destination');
});

// 2. Initialize
await GoogleNavigation.initialize({ apiKey: 'YOUR_API_KEY' });

// 3. Show the native navigation view
await GoogleNavigation.showNavigationView({ show: true });

// 4. Start navigating
await GoogleNavigation.startNavigation({
  destinationLatitude: 37.7749,
  destinationLongitude: -122.4194,
  travelMode: 'DRIVING',
});

// 5. Stop and dismiss when done
await GoogleNavigation.stopNavigation();
await GoogleNavigation.showNavigationView({ show: false });

// 6. Clean up listeners
await GoogleNavigation.removeAllListeners();
```

---

## API

### `initialize(options)`

Initializes the Google Navigation SDK. Must be called before any other method.

```typescript
initialize(options: { apiKey: string }): Promise<{ success: boolean }>
```

| Option | Type | Description |
|--------|------|-------------|
| `apiKey` | `string` | Your Google Maps Platform API key (used on iOS; Android reads from `AndroidManifest.xml`) |

---

### `showNavigationView(options)`

Presents or dismisses the full-screen native navigation UI.

```typescript
showNavigationView(options: { show: boolean }): Promise<{ success: boolean }>
```

| Option | Type | Description |
|--------|------|-------------|
| `show` | `boolean` | `true` to present the map, `false` to dismiss it |

---

### `startNavigation(options)`

Sets a destination and begins turn-by-turn guidance. Call after `showNavigationView({ show: true })`.

```typescript
startNavigation(options: {
  destinationLatitude: number;
  destinationLongitude: number;
  travelMode?: 'DRIVING' | 'WALKING' | 'CYCLING' | 'TWO_WHEELER';
}): Promise<{ success: boolean }>
```

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `destinationLatitude` | `number` | — | Destination latitude |
| `destinationLongitude` | `number` | — | Destination longitude |
| `travelMode` | `string` | `'DRIVING'` | Travel mode |

---

### `stopNavigation()`

Stops active guidance and clears the destination.

```typescript
stopNavigation(): Promise<{ success: boolean }>
```

---

### `addListener(eventName, handler)`

Subscribes to a navigation event.

```typescript
addListener(
  eventName: 'onArrival' | 'onRouteChanged' | 'onNavigationReady',
  listenerFunc: (event: any) => void
): Promise<PluginListenerHandle>
```

| Event | Fired when |
|-------|-----------|
| `onNavigationReady` | SDK has initialized successfully |
| `onArrival` | User arrives at the destination |
| `onRouteChanged` | The route is recalculated |

Returns a `PluginListenerHandle` — call `.remove()` on it to unsubscribe.

---

### `removeAllListeners()`

Removes all active event listeners.

```typescript
removeAllListeners(): Promise<void>
```

---

## Platform notes

| Feature | iOS | Android |
|---------|-----|---------|
| Turn-by-turn guidance | ✅ | ✅ |
| Full-screen native UI | ✅ | ✅ |
| `onArrival` event | ✅ | ✅ |
| `onRouteChanged` event | ✅ | ✅ |
| Web | ❌ | ❌ |
| Swift Package Manager | ❌ | — |

---

## License

MIT
