import { WebPlugin } from '@capacitor/core';

import type { CachePlugin } from './definitions';

export class CacheWeb extends WebPlugin implements CachePlugin {
  action(options: { value: JSON }): Promise<{ value: string }> {
    console.log('ACTION', options);
    return Promise.resolve({value: ""});
  }
}
