import { WebPlugin } from '@capacitor/core';

import type { CachePlugin } from './definitions';

export class CacheWeb extends WebPlugin implements CachePlugin {
  async action(options: { value: string }): Promise<{ value: string }> {
    console.log('ACTION', options);
    return options;
  }
}
