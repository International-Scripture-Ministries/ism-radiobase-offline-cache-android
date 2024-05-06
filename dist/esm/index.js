import { registerPlugin } from '@capacitor/core';
const Cache = registerPlugin('Cache', {
    web: () => import('./web').then(m => new m.CacheWeb()),
});
export * from './definitions';
export { Cache };
//# sourceMappingURL=index.js.map