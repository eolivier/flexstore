<script setup lang="ts">
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import { onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useProductsStore } from '@/stores/products.store.ts';
  import type { ProductTexts } from '@/models/product';
  import { textsOf, translationsOf } from '@/utils/productTranslations';
  import { useStocksStore } from '@/stores/stocks.store';
  import ProductFields from './ProductFields.vue';
  import ProductImageField from './ProductImageField.vue';

  interface EditedProduct {
    texts: ProductTexts;
    category: string;
    price: string | null;
    stock: string;
  }

  const props = defineProps<{ id: string }>();

  const { t } = useI18n();
  const productsStore = useProductsStore();
  const stocksStore = useStocksStore();

  // null while loading, false when the product cannot be read
  const editedProduct = ref<EditedProduct | null | false>(null);
  const valid = ref(false);
  const saving = ref(false);
  // null before the first save
  const saved = ref<boolean | null>(null);

  onMounted(async () => {
    // The administrator edits the product in every language
    const adminProduct = productsStore.fetchAdminProduct(props.id);
    const [product] = await Promise.all([adminProduct, stocksStore.fetchStocks()]);
    editedProduct.value = product
      ? {
          texts: textsOf(product.translations),
          category: product.category,
          price: String(product.price ?? ''),
          stock: String(stocksStore.quantityOf(props.id) ?? 0),
        }
      : false;
  });

  const stockRule = (v: string) => /^\d+$/.test(v) || t('stock.invalid');

  async function save() {
    if (!valid.value || !editedProduct.value || editedProduct.value.price === null) return;
    saving.value = true;
    const { stock, texts, category, price } = editedProduct.value;
    const productSaved = await productsStore.updateProduct(props.id, {
      category,
      price: parseFloat(price),
      currency: 'EUR',
      translations: translationsOf(texts),
    });
    const stockSaved = productSaved && (await stocksStore.changeStock(props.id, parseInt(stock, 10)));
    saved.value = stockSaved;
    saving.value = false;
  }
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
          v-model:texts="editedProduct.texts"
          v-model:category="editedProduct.category"
          v-model:price="editedProduct.price"
        />
        <v-text-field
          v-model="editedProduct.stock"
          :label="t('stock.units')"
          type="number"
          min="0"
          :rules="[stockRule]"
          prepend-inner-icon="mdi-package-variant"
          class="mt-2"
        />
        <v-alert v-if="saved === false" type="error" variant="tonal" density="compact" class="my-4">{{
          t('editProduct.error')
        }}</v-alert>
        <div class="d-flex ga-3 mt-2">
          <v-btn to="/admin/products" variant="outlined" color="primary" size="large" class="flex-grow-1">{{
            t('editProduct.cancel')
          }}</v-btn>
          <v-btn
            type="submit"
            color="primary"
            size="large"
            class="flex-grow-1"
            :loading="saving"
            :disabled="!valid"
            >{{ t('editProduct.save') }}</v-btn
          >
        </div>
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
