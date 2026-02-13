export interface GoogleNavigationPlugin {
  /**
   * Initialize the Navigation SDK with API key
   */
  initialize(options: { apiKey: string }): Promise<{ success: boolean }>;

  /**
   * Start navigation to a destination
   */
  startNavigation(options: {
    destinationLatitude: number;
    destinationLongitude: number;
    travelMode?: 'DRIVING' | 'WALKING' | 'CYCLING' | 'TWO_WHEELER';
  }): Promise<{ success: boolean }>;

  /**
   * Stop navigation
   */
  stopNavigation(): Promise<{ success: boolean }>;

  /**
   * Show/hide navigation view
   */
  showNavigationView(options: { show: boolean }): Promise<{ success: boolean }>;

  /**
   * Add listener for navigation events
   */
  addListener(
    eventName: 'onArrival' | 'onRouteChanged' | 'onNavigationReady',
    listenerFunc: (event: any) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Remove all listeners
   */
  removeAllListeners(): Promise<void>;
}

export interface PluginListenerHandle {
  remove: () => Promise<void>;
}