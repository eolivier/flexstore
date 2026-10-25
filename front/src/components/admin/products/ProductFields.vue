<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { categoryInfo, shopCategories } from '@/components/products/categories';

  // The fields of a product, shared by its creation and its edition
  const name = defineModel<string>('name', { required: true });
  const description = defineModel<string>('description', { required: true });
  const category = defineModel<string>('category', { required: true });
  const price = defineModel<string | null>('price', { required: true });

  const { t } = useI18n();

  const categories = computed(() =>
    shopCategories.map(({ code }) => ({
      title: t(categoryInfo(code).label),
      value: code,
      props: { prependIcon: categoryInfo(code).icon },
    }))
  );

  const rules = {
    required: (v: string | null) => !!v || t('createProduct.mandatoryField'),
    price: (v: string | null) => (v !== null && parseFloat(v) > 0) || t('createProduct.invalidPrice'),
  };
</script>

<template>
  <v-text-field
    v-model="name"
    :label="t('createProduct.name')"
    :rules="[rules.required]"
    class="mb-2"
    required
  />
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
</template>
