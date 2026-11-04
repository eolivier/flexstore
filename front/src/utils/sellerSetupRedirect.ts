import { useSettingsStore } from '@/stores/settings.store';

export const SELLER_SETUP_PATH = '/admin/seller-setup';

// While the seller setup of the shop is off, the administrators are sent to the setup assistant of the seller
// before any other page of the administration. When the settings cannot be read, the administration stays open
export async function sellerSetupIsPendingFor(path: string) {
  if (path === SELLER_SETUP_PATH) return false;
  const settingsStore = useSettingsStore();
  if (!settingsStore.settings) await settingsStore.fetchSettings();
  return settingsStore.settings?.sellerSetup === 'DISABLED';
}
