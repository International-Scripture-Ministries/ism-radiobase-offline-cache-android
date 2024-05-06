import { WebPlugin } from '@capacitor/core';
import type { CachePlugin } from './definitions';
export declare class CacheWeb extends WebPlugin implements CachePlugin {
    action(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
}
