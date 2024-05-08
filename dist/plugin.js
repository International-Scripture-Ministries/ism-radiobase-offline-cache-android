var capacitorCache = (function (exports, core) {
    'use strict';

    const Cache = core.registerPlugin('Cache', {
        web: () => Promise.resolve().then(function () { return web; }).then(m => new m.CacheWeb()),
    });

    class CacheWeb extends core.WebPlugin {
        action(options) {
            console.log('ACTION', options);
            return Promise.resolve({ value: "" });
        }
    }

    var web = /*#__PURE__*/Object.freeze({
        __proto__: null,
        CacheWeb: CacheWeb
    });

    exports.Cache = Cache;

    Object.defineProperty(exports, '__esModule', { value: true });

    return exports;

})({}, capacitorExports);
//# sourceMappingURL=plugin.js.map
