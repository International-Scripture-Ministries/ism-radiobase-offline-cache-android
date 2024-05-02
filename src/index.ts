import { registerPlugin } from '@capacitor/core';

import type { CachePlugin } from './definitions';

const Cache = registerPlugin<CachePlugin>('Cache', {
  web: () => import('./web').then(m => new m.CacheWeb()),
});

export * from './definitions';
export { Cache };
