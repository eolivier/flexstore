<script setup lang="ts">
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import { nextTick, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useProductsStore } from '@/stores/products.store.ts';
  import ProductFields from './ProductFields.vue';

  const name = ref('');
  const description = ref('');
  const category = ref('CLOTHING');
  const price = ref<string | null>(null);
  const valid = ref(false);
  const saving = ref(false);
  const formRef = ref<{ resetValidation: () => void } | null>(null);
  const feedback = ref({ show: false, success: true, text: '' });

  const productsStore = useProductsStore();
  const { t } = useI18n();

  const submitForm = async () => {
    if (!valid.value || price.value === null) return;
    saving.value = true;
    const createdName = name.value;
    const created = await productsStore.addProduct({
      name: name.value,
      description: description.value,
      category: category.value,
      price: parseFloat(price.value),
      currency: 'EUR',
    });
    saving.value = false;
    feedback.value = {
      show: true,
      success: created,
      text: created ? t('createProduct.success', { name: createdName }) : t('createProduct.error'),
    };
    if (created) resetForm();
  };

  const resetForm = () => {
    name.value = '';
    description.value = '';
    price.value = null;
    valid.value = false;
    nextTick(() => formRef.value?.resetValidation());
  };
</script>

<template>
  <v-container class="page py-10">
    <AdminBreadcrumbs />
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('createProduct.title') }}</h1>
    <v-card class="pa-6">
      <v-form ref="formRef" v-model="valid" @submit.prevent="submitForm">
        <ProductFields
          v-model:name="name"
          v-model:description="description"
          v-model:category="category"
          v-model:price="price"
        />
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
    <v-snackbar
      v-model="feedback.show"
      :color="feedback.success ? 'primary' : 'error'"
      timeout="3000"
      location="bottom right"
    >
      {{ feedback.text }}
      <template v-if="feedback.success" #actions>
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
