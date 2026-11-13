<script setup lang="ts">
  import LoadError from '@/components/layout/LoadError.vue';
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import { onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useSnackbar } from '@/composables/useSnackbar';
  import { useProductsStore } from '@/stores/products.store.ts';
  import { textsOf, translationsOf } from '@/utils/productTranslations';
  import { useStocksStore } from '@/stores/stocks.store';
  import { useStockAlertsStore } from '@/stores/stockAlerts.store';
  import ProductForm, { type ProductFormValues } from './ProductForm.vue';
  import ProductImageField from './ProductImageField.vue';
  import { normalizedSku } from '@/utils/sku';
  import { promotionFormOf, promotionOf } from '@/utils/promotion';

  const props = defineProps<{ id: string }>();

  // The threshold of the shop when it cannot be read
  const DEFAULT_ALERT_THRESHOLD = 5;

  const { t } = useI18n();
  const snackbar = useSnackbar();
  const productsStore = useProductsStore();
  const stocksStore = useStocksStore();
  const stockAlertsStore = useStockAlertsStore();

  // null while loading, false when the product cannot be read
  const editedProduct = ref<ProductFormValues | null | false>(null);
  const saving = ref(false);
  // null before the first save
  const saved = ref<boolean | null>(null);
  const skuAlreadyUsed = ref(false);

  onMounted(async () => {
    // The administrator edits the product in every language
    const adminProduct = productsStore.fetchAdminProduct(props.id);
    const alertThreshold = stockAlertsStore.thresholdOf(props.id);
    const [product, threshold] = await Promise.all([adminProduct, alertThreshold, stocksStore.fetchStocks()]);
    editedProduct.value = product
      ? {
          sku: product.sku,
          texts: textsOf(product.translations),
          category: product.category,
          price: String(product.price ?? ''),
          vatRate: product.vatRate ?? 'STANDARD',
          promotion: promotionFormOf(product.promotion),
          stock: String(stocksStore.quantityOf(props.id) ?? 0),
          alertThreshold: String(threshold ?? DEFAULT_ALERT_THRESHOLD),
        }
      : false;
  });

  async function save(values: ProductFormValues) {
    if (values.price === null || values.alertThreshold === null) return;
    saving.value = true;
    const { stock, alertThreshold, sku, texts, category, price, vatRate, promotion } = values;
    const productSave = await productsStore.updateProduct(props.id, {
      sku: normalizedSku(sku),
      category,
      price: parseFloat(price),
      currency: 'EUR',
      translations: translationsOf(texts),
      vatRate,
      promotion: promotionOf(promotion),
    });
    skuAlreadyUsed.value = productSave === 'skuAlreadyUsed';
    const productSaved = productSave === 'saved';
    const stockSaved = productSaved && (await stocksStore.changeStock(props.id, parseInt(stock, 10)));
    const thresholdSaved =
      stockSaved && (await stockAlertsStore.changeThreshold(props.id, parseInt(alertThreshold, 10)));
    // A SKU already used is said by its field
    saved.value = skuAlreadyUsed.value ? null : thresholdSaved;
    saving.value = false;
    if (saved.value) {
      snackbar.success(t('editProduct.saved'), { action: { text: t('nav.products'), to: '/admin/products' } });
    }
  }
</script>

<template>
  <v-container class="page py-10">
    <AdminBreadcrumbs />
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('editProduct.title') }}</h1>
    <v-skeleton-loader v-if="editedProduct === null" type="article" class="rounded-xl" />
    <LoadError v-else-if="editedProduct === false" :message="t('editProduct.loadError')" />
    <v-card v-else class="pa-6">
      <ProductImageField :product-id="id" />
      <v-divider class="mb-4" />
      <ProductForm
        :initial="editedProduct"
        :saving="saving"
        :sku-already-used="skuAlreadyUsed"
        :error-message="saved === false ? t('editProduct.error') : undefined"
        @submit="save"
        @sku-change="skuAlreadyUsed = false"
      />
    </v-card>
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 640px;
  }
</style>
