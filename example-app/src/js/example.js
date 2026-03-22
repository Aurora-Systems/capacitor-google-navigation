import { GoogleNavigation } from 'capacitor-google-navigation';

// Log navigation events
GoogleNavigation.addListener('onNavigationReady', () => {
  log('Event: onNavigationReady');
});
GoogleNavigation.addListener('onArrival', (e) => {
  log('Event: onArrival — ' + JSON.stringify(e));
});
GoogleNavigation.addListener('onRouteChanged', () => {
  log('Event: onRouteChanged');
});

window.testInitialize = async () => {
  const apiKey = document.getElementById('apiKeyInput').value;
  try {
    const result = await GoogleNavigation.initialize({ apiKey });
    log('initialize() → ' + JSON.stringify(result));
  } catch (e) {
    log('initialize() error: ' + e.message);
  }
};

window.testShowView = async () => {
  try {
    const result = await GoogleNavigation.showNavigationView({ show: true });
    log('showNavigationView(true) → ' + JSON.stringify(result));
  } catch (e) {
    log('showNavigationView() error: ' + e.message);
  }
};

window.testStartNav = async () => {
  const lat = parseFloat(document.getElementById('latInput').value);
  const lng = parseFloat(document.getElementById('lngInput').value);
  try {
    const result = await GoogleNavigation.startNavigation({
      destinationLatitude: lat,
      destinationLongitude: lng,
      travelMode: 'DRIVING',
    });
    log('startNavigation() → ' + JSON.stringify(result));
  } catch (e) {
    log('startNavigation() error: ' + e.message);
  }
};

window.testStop = async () => {
  try {
    const result = await GoogleNavigation.stopNavigation();
    log('stopNavigation() → ' + JSON.stringify(result));
    await GoogleNavigation.showNavigationView({ show: false });
    log('showNavigationView(false) → done');
  } catch (e) {
    log('stopNavigation() error: ' + e.message);
  }
};

function log(msg) {
  const el = document.getElementById('log');
  el.textContent = msg + '\n' + el.textContent;
}
