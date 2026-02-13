export interface GoogleNavigationPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
