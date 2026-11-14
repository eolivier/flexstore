import { computed, onMounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useCartStore } from '@/stores/cart.store';
import { useSnackbar } from '@/composables/useSnackbar';

// The cart of the header: loads it, counts its products and tells when the items of a visitor were merged
export function useHeaderCart() {
  const { t } = useI18n();
  const cartStore = useCartStore();
  const snackbar = useSnackbar();

  const cartCount = computed(() =>
    (cartStore.cartItems?.items ?? []).reduce((count, item) => count + item.productQuantity, 0)
  );

  // The store only tells that the items were merged: the message is shown once, then the flag is reset
  watch(
    () => cartStore.accountItemsMerged,
    (merged) => {
      if (!merged) return;
      snackbar.success(t('cart.accountItemsMerged'), {
        icon: 'mdi-cart-check',
        action: { text: t('products.viewCart'), to: '/cart' },
      });
      cartStore.accountItemsMerged = false;
    }
  );

  onMounted(() => {
    cartStore.fetchCartItems();
  });

  return { cartCount };
}
