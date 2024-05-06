export interface CachePlugin {
    action(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
}
