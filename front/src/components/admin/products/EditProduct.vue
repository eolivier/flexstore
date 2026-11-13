<script setup lang="ts">
  import FormActions from '@/components/form/FormActions.vue';
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import { onMounted, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useProductsStore } from '@/stores/products.store.ts';
  import type { ProductTexts, VatRate } from '@/models/product';
  import { textsOf, translationsOf } from '@/utils/productTranslations';
  import { useStocksStore } from '@/stores/stocks.store';
  import { useStockAlertsStore } from '@/stores/stockAlerts.store';
  import ProductFields from './ProductFields.vue';
  import ProductImageField from './ProductImageField.vue';
  import StockField from './StockField.vue';
  import AlertThresholdField from './AlertThresholdField.vue';
  import { normalizedSku } from '@/utils/sku';
  import { promotionFormOf, promotionOf, type PromotionForm } from '@/utils/promotion';

  interface EditedProduct {
    sku: string;
    texts: ProductTexts;
    category: string;
    price: string | null;
    vatRate: VatRate;
    promotion: PromotionForm;
    stock: string;
    alertThreshold: string;
  }

  const props = defineProps<{ id: string }>();

  // The threshold of the shop when it cannot be read
  const DEFAULT_ALERT_THRESHOLD = 5;

  const { t } = useI18n();
  const productsStore = useProductsStore();
  const stocksStore = useStocksStore();
  const stockAlertsStore = useStockAlertsStore();

  // null while loading, false when the product cannot be read
  const editedProduct = ref<EditedProduct | null | false>(null);
  const valid = ref(false);
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

  async function save() {
    if (!valid.value || !editedProduct.value || editedProduct.value.price === null) return;
    saving.value = true;
    const { stock, alertThreshold, sku, texts, category, price, vatRate, promotion } = editedProduct.value;
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
  }

  // A SKU typed again may be free
  watch(
    () => editedProduct.value && editedProduct.value.sku,
    () => (skuAlreadyUsed.value = false)
  );
</script>

<template>
  <v-container class="page py-10">
    <AdminBreadcrumbs />
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('editProduct.title') }}</h1>
    <v-skeleton-loader v-if="editedProduct === null" type="article" class="rounded-xl" />
    <v-alert v-else-if="editedProduct === false" type="error" variant="tonal">{{
      t('editProduct.loadError')
    }}</v-alert>
    <v-card v-else class="pa-6">
      <ProductImageField :product-id="id" />
      <v-divider class="mb-4" />
      <v-form v-model="valid" @submit.prevent="save">
        <ProductFields
          v-model:sku="editedProduct.sku"
          v-model:texts="editedProduct.texts"
          v-model:category="editedProduct.category"
          v-model:price="editedProduct.price"
          v-model:vat-rate="editedProduct.vatRate"
          v-model:promotion="editedProduct.promotion"
          :sku-already-used="skuAlreadyUsed"
        />
        <StockField v-model="editedProduct.stock" />
        <AlertThresholdField v-model="editedProduct.alertThreshold" />
        <v-alert v-if="saved === false" type="error" variant="tonal" density="compact" class="my-4">{{
          t('editProduct.error')
        }}</v-alert>
        <FormActions class="mt-2" cancel-to="/admin/products" :loading="saving" :disabled="!valid" />
      </v-form>
    </v-card>
    <v-snackbar
      :model-value="saved === true"
      color="primary"
      timeout="3000"
      location="bottom right"
      @update:model-value="saved = null"
    >
      {{ t('editProduct.saved') }}
      <template #actions>
        <v-btn to="/admin/products" variant="text" color="secondary">{{ t('nav.products') }}</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 640px;
  }
</style>
