import { WebPlugin } from '@capacitor/core';
export class CacheWeb extends WebPlugin {
    async action(options) {
        console.log('ACTION', options);
        return options;
    }
}
//# sourceMappingURL=web.js.map