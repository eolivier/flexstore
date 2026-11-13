import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useCartStore } from '@/stores/cart.store';
import { useSnackbar } from './useSnackbar';
import type { Item } from '@/models/cart';
import type { Product } from '@/models/product';

// Adds a product to the cart (or increments its quantity) and tells the result in the snackbar
export function useAddToCart() {
  const { t } = useI18n();
  const cartStore = useCartStore();
  const snackbar = useSnackbar();

  const addingProductId = ref<string | undefined>();

  async function addToCart(product: Product, quantity = 1) {
    addingProductId.value = product.id;
    const existingItem = cartStore.cartItems?.items.find((i) => i.productId === product.id);
    const item = existingItem
      ? { ...existingItem, productQuantity: existingItem.productQuantity + quantity }
      : productToCartItem(product, quantity);
    const saved = await cartStore.saveItem(item);
    addingProductId.value = undefined;
    if (saved) {
      snackbar.success(t('products.added', { name: product.name }), {
        icon: 'mdi-check-circle-outline',
        action: { text: t('products.viewCart'), to: '/cart' },
      });
    } else snackbar.error(t('products.addError'), { icon: 'mdi-alert-circle-outline' });
  }

  return { addingProductId, addToCart };
}

function productToCartItem(product: Product, quantity: number): Item {
  return {
    itemId: '',
    productId: product.id,
    productName: product.name,
    productDescription: product.description,
    productCategory: product.category,
    productPrice: product.currentPrice ?? product.price,
    regularPrice: product.price,
    promotionPercentage: product.promotionPercentage,
    productCurrency: product.currency,
    productQuantity: quantity,
    itemPrice: (product.currentPrice ?? product.price ?? 0) * quantity,
  };
}
