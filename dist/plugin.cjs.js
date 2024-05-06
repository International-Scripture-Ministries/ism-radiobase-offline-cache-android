'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

var core = require('@capacitor/core');

const Cache = core.registerPlugin('Cache', {
    web: () => Promise.resolve().then(function () { return web; }).then(m => new m.CacheWeb()),
});

class CacheWeb extends core.WebPlugin {
    async action(options) {
        console.log('ACTION', options);
        return options;
    }
}

var web = /*#__PURE__*/Object.freeze({
    __proto__: null,
    CacheWeb: CacheWeb
});

exports.Cache = Cache;
//# sourceMappingURL=plugin.cjs.js.map
