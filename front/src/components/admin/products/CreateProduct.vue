<script setup lang="ts">
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import ProductForm, { type ProductFormValues } from './ProductForm.vue';
  import NewProductImageField from './NewProductImageField.vue';
  import { DEFAULT_LANGUAGE } from '@/models/product';
  import { useProductCreation } from '@/composables/useProductCreation';
  import { useSnackbar } from '@/composables/useSnackbar';
  import { emptyTexts, translationsOf } from '@/utils/productTranslations';
  import { normalizedSku } from '@/utils/sku';
  import { emptyPromotion, promotionOf } from '@/utils/promotion';

  const emptyProduct = (): ProductFormValues => ({
    sku: '',
    texts: emptyTexts(),
    category: 'CLOTHING',
    price: null,
    // Most products are sold at the standard rate
    vatRate: 'STANDARD',
    promotion: emptyPromotion(),
    stock: '0',
    alertThreshold: null,
  });

  const initial = ref(emptyProduct());
  const image = ref<File | null>(null);
  const skuAlreadyUsed = ref(false);
  const saving = ref(false);
  // Changing the key gives an empty form again
  const formKey = ref(0);

  const { createProduct } = useProductCreation();
  const { t } = useI18n();
  const snackbar = useSnackbar();

  const submitForm = async (values: ProductFormValues) => {
    if (values.price === null) return;
    saving.value = true;
    const name = values.texts[DEFAULT_LANGUAGE].name;
    const product = {
      sku: normalizedSku(values.sku),
      category: values.category,
      price: parseFloat(values.price),
      currency: 'EUR',
      translations: translationsOf(values.texts),
      vatRate: values.vatRate,
      promotion: promotionOf(values.promotion),
    };
    const creation = await createProduct(product, image.value, parseInt(values.stock, 10));
    saving.value = false;
    skuAlreadyUsed.value = creation.outcome === 'skuAlreadyUsed';
    if (creation.outcome === 'skuAlreadyUsed') return;
    if (creation.outcome === 'failed') {
      snackbar.error(t('createProduct.error'));
      return;
    }
    if (creation.outcome === 'partlyCreated') {
      // Stays open: the administrator is told where to finish the product
      snackbar.warning(t('createProduct.partlyCreated', { name }), {
        persistent: true,
        action: { text: t('createProduct.finish'), to: `/admin/products/${creation.productId}/edit` },
      });
    } else {
      snackbar.success(t('createProduct.success', { name }), {
        action: { text: t('nav.products'), to: '/admin/products' },
      });
    }
    resetForm();
  };

  const resetForm = () => {
    initial.value = emptyProduct();
    image.value = null;
    formKey.value++;
  };
</script>

<template>
  <v-container class="py-10">
    <AdminBreadcrumbs />
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('createProduct.title') }}</h1>
    <v-card class="pa-6">
      <NewProductImageField v-model="image" />
      <v-divider class="mb-4" />
      <ProductForm
        :key="formKey"
        :initial="initial"
        :saving="saving"
        :sku-already-used="skuAlreadyUsed"
        :submit-label="t('createProduct.submit')"
        @submit="submitForm"
        @sku-change="skuAlreadyUsed = false"
      />
    </v-card>
  </v-container>
</template>
