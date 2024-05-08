export interface CachePlugin {
  action(options: { value: JSON }): Promise<{ value: string }>;
}
