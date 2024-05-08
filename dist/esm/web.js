import { WebPlugin } from '@capacitor/core';
export class CacheWeb extends WebPlugin {
    action(options) {
        console.log('ACTION', options);
        return Promise.resolve({ value: "" });
    }
}
//# sourceMappingURL=web.js.map