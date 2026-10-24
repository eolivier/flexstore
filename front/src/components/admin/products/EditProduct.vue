<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useProductsStore } from '@/stores/products.store.ts';
  import ProductFields from './ProductFields.vue';

  interface EditedProduct {
    name: string;
    description: string;
    category: string;
    price: string | null;
  }

  const props = defineProps<{ id: string }>();

  const { t } = useI18n();
  const productsStore = useProductsStore();

  // null while loading, false when the product cannot be read
  const editedProduct = ref<EditedProduct | null | false>(null);
  const valid = ref(false);
  const saving = ref(false);
  // null before the first save
  const saved = ref<boolean | null>(null);

  onMounted(async () => {
    const product = await productsStore.fetchProduct(props.id);
    editedProduct.value = product
      ? { name: product.name ?? '', description: product.description, category: product.category, price: String(product.price ?? '') }
      : false;
  });

  async function save() {
    if (!valid.value || !editedProduct.value || editedProduct.value.price === null) return;
    saving.value = true;
    saved.value = await productsStore.updateProduct(props.id, {
      ...editedProduct.value,
      price: parseFloat(editedProduct.value.price),
      currency: 'EUR',
    });
    saving.value = false;
  }
</script>

<template>
  <v-container class="page py-10">
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('editProduct.title') }}</h1>
    <v-skeleton-loader v-if="editedProduct === null" type="article" class="rounded-xl" />
    <v-alert v-else-if="editedProduct === false" type="error" variant="tonal">{{ t('editProduct.loadError') }}</v-alert>
    <v-card v-else class="pa-6">
      <v-form v-model="valid" @submit.prevent="save">
        <ProductFields
          v-model:name="editedProduct.name"
          v-model:description="editedProduct.description"
          v-model:category="editedProduct.category"
          v-model:price="editedProduct.price"
        />
        <v-alert v-if="saved === false" type="error" variant="tonal" density="compact" class="my-4">{{ t('editProduct.error') }}</v-alert>
        <div class="d-flex ga-3 mt-2">
          <v-btn to="/admin/products" variant="outlined" color="primary" size="large" class="flex-grow-1">{{ t('editProduct.cancel') }}</v-btn>
          <v-btn type="submit" color="primary" size="large" class="flex-grow-1" :loading="saving" :disabled="!valid">{{ t('editProduct.save') }}</v-btn>
        </div>
      </v-form>
    </v-card>
    <v-snackbar :model-value="saved === true" color="primary" timeout="3000" location="bottom right" @update:model-value="saved = null">
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
