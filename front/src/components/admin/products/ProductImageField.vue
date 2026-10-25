<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useProductImagesStore, type ImageUpload } from '@/stores/productImages.store';

  // The image of the product: given at once (2 MB at most, resized by the back end), replaced or removed
  const props = defineProps<{ productId: string }>();

  const { t } = useI18n();
  const productImagesStore = useProductImagesStore();

  const fileInput = ref<HTMLInputElement | null>(null);
  const uploading = ref(false);
  const feedback = ref<ImageUpload | 'removed' | 'removalError' | null>(null);

  const hasImage = computed(() => productImagesStore.hasImage(props.productId));
  const imageUrl = computed(() => productImagesStore.imageUrl(props.productId, 'large'));

  onMounted(() => productImagesStore.fetchProductsWithImage());

  async function upload(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    uploading.value = true;
    feedback.value = await productImagesStore.uploadImage(props.productId, file);
    uploading.value = false;
    if (fileInput.value) fileInput.value.value = '';
  }

  async function remove() {
    const removed = await productImagesStore.removeImage(props.productId);
    feedback.value = removed ? 'removed' : 'removalError';
  }
</script>

<template>
  <div class="mb-4">
    <div class="text-subtitle-2 font-weight-bold mb-2">{{ t('productImage.title') }}</div>
    <div class="d-flex flex-wrap align-center ga-4">
      <div class="preview d-flex align-center justify-center">
        <img v-if="hasImage" :src="imageUrl" :alt="t('productImage.title')" class="preview-image" />
        <v-icon v-else icon="mdi-image-off-outline" size="40" class="text-medium-emphasis" />
      </div>
      <div class="d-flex flex-column ga-2">
        <input
          ref="fileInput"
          type="file"
          accept="image/jpeg,image/png,image/webp"
          class="d-none"
          data-test="image-input"
          @change="upload"
        />
        <v-btn
          color="primary"
          variant="tonal"
          prepend-icon="mdi-upload"
          :loading="uploading"
          @click="fileInput?.click()"
        >
          {{ hasImage ? t('productImage.replace') : t('productImage.add') }}
        </v-btn>
        <v-btn v-if="hasImage" variant="text" color="error" prepend-icon="mdi-delete-outline" @click="remove">
          {{ t('productImage.remove') }}
        </v-btn>
        <span class="text-caption text-medium-emphasis">{{ t('productImage.hint') }}</span>
      </div>
    </div>
    <v-alert
      v-if="feedback && feedback !== 'saved' && feedback !== 'removed'"
      type="error"
      variant="tonal"
      density="compact"
      class="mt-3"
    >
      {{ t(`productImage.errors.${feedback}`) }}
    </v-alert>
  </div>
</template>

<style scoped>
  .preview {
    width: 160px;
    aspect-ratio: 4 / 3;
    border-radius: 12px;
    overflow: hidden;
    background: rgb(var(--v-theme-surface-variant));
  }
  .preview-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
</style>
