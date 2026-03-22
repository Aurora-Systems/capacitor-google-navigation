import { WebPlugin } from '@capacitor/core';

import type { GoogleNavigationPlugin } from './definitions';

export class GoogleNavigationWeb extends WebPlugin implements GoogleNavigationPlugin {
  async initialize(_options: { apiKey: string }): Promise<{ success: boolean }> {
    throw this.unimplemented('Not available on web.');
  }

  async startNavigation(_options: {
    destinationLatitude: number;
    destinationLongitude: number;
    travelMode?: 'DRIVING' | 'WALKING' | 'CYCLING' | 'TWO_WHEELER';
  }): Promise<{ success: boolean }> {
    throw this.unimplemented('Not available on web.');
  }

  async stopNavigation(): Promise<{ success: boolean }> {
    throw this.unimplemented('Not available on web.');
  }

  async showNavigationView(_options: { show: boolean }): Promise<{ success: boolean }> {
    throw this.unimplemented('Not available on web.');
  }
}
