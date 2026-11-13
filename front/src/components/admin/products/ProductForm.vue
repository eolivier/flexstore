<script setup lang="ts">
  import { ref } from 'vue';
  import FormActions from '@/components/form/FormActions.vue';
  import type { ProductTexts, VatRate } from '@/models/product';
  import type { PromotionForm } from '@/utils/promotion';
  import ProductFields from './ProductFields.vue';
  import StockField from './StockField.vue';
  import AlertThresholdField from './AlertThresholdField.vue';

  export interface ProductFormValues {
    sku: string;
    texts: ProductTexts;
    category: string;
    price: string | null;
    vatRate: VatRate;
    promotion: PromotionForm;
    stock: string;
    // null when the alert threshold is not asked (a product being created)
    alertThreshold: string | null;
  }

  const props = defineProps<{
    initial: ProductFormValues;
    saving: boolean;
    skuAlreadyUsed: boolean;
    submitLabel?: string;
    errorMessage?: string;
  }>();
  const emit = defineEmits<{ submit: [values: ProductFormValues]; skuChange: [] }>();

  const sku = ref(props.initial.sku);
  const texts = ref(props.initial.texts);
  const category = ref(props.initial.category);
  const price = ref(props.initial.price);
  const vatRate = ref(props.initial.vatRate);
  const promotion = ref(props.initial.promotion);
  const stock = ref(props.initial.stock);
  const alertThreshold = ref(props.initial.alertThreshold ?? '');
  const valid = ref(false);

  function submit() {
    if (!valid.value) return;
    emit('submit', {
      sku: sku.value,
      texts: texts.value,
      category: category.value,
      price: price.value,
      vatRate: vatRate.value,
      promotion: promotion.value,
      stock: stock.value,
      alertThreshold: props.initial.alertThreshold === null ? null : alertThreshold.value,
    });
  }
</script>

<template>
  <v-form v-model="valid" @submit.prevent="submit">
    <ProductFields
      v-model:sku="sku"
      v-model:texts="texts"
      v-model:category="category"
      v-model:price="price"
      v-model:vat-rate="vatRate"
      v-model:promotion="promotion"
      :sku-already-used="skuAlreadyUsed"
      @update:sku="emit('skuChange')"
    />
    <StockField v-model="stock" />
    <AlertThresholdField v-if="initial.alertThreshold !== null" v-model="alertThreshold" />
    <v-alert v-if="errorMessage" type="error" variant="tonal" density="compact" class="my-4">{{
      errorMessage
    }}</v-alert>
    <FormActions class="mt-2" cancel-to="/admin/products" :loading="saving" :disabled="!valid">
      <template v-if="submitLabel">{{ submitLabel }}</template>
    </FormActions>
  </v-form>
</template>
