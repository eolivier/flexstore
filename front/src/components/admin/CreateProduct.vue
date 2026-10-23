<script setup lang="ts">
  import { computed, nextTick, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useProductsStore } from '@/stores/products.store.ts';
  import { categoryInfo, shopCategories } from '@/components/products/categories';

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

  const categories = computed(() =>
    shopCategories.map(({ code }) => ({ title: t(categoryInfo(code).label), value: code, props: { prependIcon: categoryInfo(code).icon } }))
  );

  const rules = {
    required: (v: string | null) => !!v || t('createProduct.mandatoryField'),
    price: (v: string | null) => (v !== null && parseFloat(v) > 0) || t('createProduct.invalidPrice'),
  };

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
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('createProduct.title') }}</h1>
    <v-card class="pa-6">
      <v-form ref="formRef" v-model="valid" @submit.prevent="submitForm">
        <v-text-field v-model="name" :label="t('createProduct.name')" :rules="[rules.required]" class="mb-2" required />
        <v-textarea
          v-model="description"
          :label="t('createProduct.description')"
          :rules="[rules.required]"
          variant="outlined"
          rounded="lg"
          rows="3"
          auto-grow
          class="mb-2"
          required
        />
        <v-row>
          <v-col cols="12" sm="7">
            <v-select v-model="category" :items="categories" :label="t('createProduct.category')" />
          </v-col>
          <v-col cols="12" sm="5">
            <v-text-field
              v-model="price"
              :label="t('createProduct.price')"
              :rules="[rules.required, rules.price]"
              type="number"
              min="0"
              step="0.01"
              required
            />
          </v-col>
        </v-row>
        <v-btn type="submit" color="primary" size="large" block class="mt-2" :loading="saving" :disabled="!valid">
          {{ t('createProduct.submit') }}
        </v-btn>
      </v-form>
    </v-card>
    <v-snackbar v-model="feedback.show" :color="feedback.success ? 'primary' : 'error'" timeout="3000" location="bottom right">
      {{ feedback.text }}
      <template v-if="feedback.success" #actions>
        <v-btn to="/products/" variant="text" color="secondary">{{ t('nav.allProducts') }}</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 640px;
  }
</style>
