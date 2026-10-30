<script setup lang="ts">
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import { nextTick, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import ProductFields from './ProductFields.vue';
  import NewProductImageField from './NewProductImageField.vue';
  import StockField from './StockField.vue';
  import ProductCreationSnackbar, { type ProductCreationFeedback } from './ProductCreationSnackbar.vue';
  import { DEFAULT_LANGUAGE } from '@/models/product';
  import { useProductCreation } from '@/composables/useProductCreation';
  import { emptyTexts, translationsOf } from '@/utils/productTranslations';
  import { normalizedSku } from '@/utils/sku';

  const sku = ref('');
  const skuAlreadyUsed = ref(false);
  const texts = ref(emptyTexts());
  const category = ref('CLOTHING');
  const price = ref<string | null>(null);
  const image = ref<File | null>(null);
  const stock = ref('0');
  const valid = ref(false);
  const saving = ref(false);
  const formRef = ref<{ resetValidation: () => void } | null>(null);
  const feedback = ref<ProductCreationFeedback>({ show: false, color: 'primary', text: '', editLink: '' });

  const { createProduct } = useProductCreation();
  const { t } = useI18n();

  const submitForm = async () => {
    if (!valid.value || price.value === null) return;
    saving.value = true;
    const name = texts.value[DEFAULT_LANGUAGE].name;
    const product = {
      sku: normalizedSku(sku.value),
      category: category.value,
      price: parseFloat(price.value),
      currency: 'EUR',
      translations: translationsOf(texts.value),
    };
    const creation = await createProduct(product, image.value, parseInt(stock.value, 10));
    saving.value = false;
    skuAlreadyUsed.value = creation.outcome === 'skuAlreadyUsed';
    if (creation.outcome === 'skuAlreadyUsed') return;
    if (creation.outcome === 'failed') {
      feedback.value = { show: true, color: 'error', text: t('createProduct.error'), editLink: '' };
      return;
    }
    const partly = creation.outcome === 'partlyCreated';
    const text = t(partly ? 'createProduct.partlyCreated' : 'createProduct.success', { name });
    const editLink = partly ? `/admin/products/${creation.productId}/edit` : '';
    feedback.value = { show: true, color: partly ? 'warning' : 'primary', text, editLink };
    resetForm();
  };

  // A SKU typed again may be free
  watch(sku, () => (skuAlreadyUsed.value = false));

  const resetForm = () => {
    sku.value = '';
    texts.value = emptyTexts();
    price.value = null;
    image.value = null;
    stock.value = '0';
    valid.value = false;
    nextTick(() => formRef.value?.resetValidation());
  };
</script>

<template>
  <v-container class="page py-10">
    <AdminBreadcrumbs />
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('createProduct.title') }}</h1>
    <v-card class="pa-6">
      <NewProductImageField v-model="image" />
      <v-divider class="mb-4" />
      <v-form ref="formRef" v-model="valid" @submit.prevent="submitForm">
        <ProductFields
          v-model:sku="sku"
          v-model:texts="texts"
          v-model:category="category"
          v-model:price="price"
          :sku-already-used="skuAlreadyUsed"
        />
        <StockField v-model="stock" />
        <div class="d-flex ga-3 mt-2">
          <v-btn to="/admin/products" variant="outlined" color="primary" size="large" class="flex-grow-1">{{
            t('createProduct.cancel')
          }}</v-btn>
          <v-btn
            type="submit"
            color="primary"
            size="large"
            class="flex-grow-1"
            :loading="saving"
            :disabled="!valid"
          >
            {{ t('createProduct.submit') }}
          </v-btn>
        </div>
      </v-form>
    </v-card>
    <ProductCreationSnackbar v-model="feedback" />
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 640px;
  }
</style>
