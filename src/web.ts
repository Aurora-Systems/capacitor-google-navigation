import { WebPlugin } from '@capacitor/core';

import type { GoogleNavigationPlugin } from './definitions';

export class GoogleNavigationWeb extends WebPlugin implements GoogleNavigationPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
