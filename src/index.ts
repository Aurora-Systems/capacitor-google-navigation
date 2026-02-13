import { registerPlugin } from '@capacitor/core';

import type { GoogleNavigationPlugin } from './definitions';

const GoogleNavigation = registerPlugin<GoogleNavigationPlugin>('GoogleNavigation', {
  web: () => import('./web').then((m) => new m.GoogleNavigationWeb()),
});

export * from './definitions';
export { GoogleNavigation };
