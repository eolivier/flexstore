<script setup lang="ts">
  import { computed, onBeforeUnmount, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { MAX_IMAGE_BYTES } from '@/stores/productImages.store';

  // The image of a product not created yet: chosen and previewed here, given to the product once it is created
  const image = defineModel<File | null>({ required: true });

  const ACCEPTED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];

  const { t } = useI18n();

  const fileInput = ref<HTMLInputElement | null>(null);
  const refusal = ref<'tooLarge' | 'unsupported' | null>(null);
  const previewUrl = computed(() => (image.value ? URL.createObjectURL(image.value) : null));

  watch(previewUrl, (_current, previous) => previous && URL.revokeObjectURL(previous));
  onBeforeUnmount(() => previewUrl.value && URL.revokeObjectURL(previewUrl.value));

  function choose(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (fileInput.value) fileInput.value.value = '';
    if (!file) return;
    refusal.value = null;
    if (!ACCEPTED_TYPES.includes(file.type)) refusal.value = 'unsupported';
    else if (file.size > MAX_IMAGE_BYTES) refusal.value = 'tooLarge';
    else image.value = file;
  }
</script>

<template>
  <div class="mb-4">
    <div class="text-subtitle-2 font-weight-bold mb-2">{{ t('productImage.title') }}</div>
    <div class="d-flex flex-wrap align-center ga-4">
      <div class="preview d-flex align-center justify-center">
        <img v-if="previewUrl" :src="previewUrl" :alt="t('productImage.title')" class="preview-image" />
        <v-icon v-else icon="mdi-image-off-outline" size="40" class="text-medium-emphasis" />
      </div>
      <div class="d-flex flex-column ga-2">
        <input
          ref="fileInput"
          type="file"
          :accept="ACCEPTED_TYPES.join(',')"
          class="d-none"
          data-test="image-input"
          @change="choose"
        />
        <v-btn color="primary" variant="tonal" prepend-icon="mdi-upload" @click="fileInput?.click()">
          {{ image ? t('productImage.replace') : t('productImage.add') }}
        </v-btn>
        <v-btn v-if="image" variant="text" color="error" prepend-icon="mdi-delete-outline" @click="image = null">
          {{ t('productImage.remove') }}
        </v-btn>
        <span class="text-caption text-medium-emphasis">{{ t('productImage.hint') }}</span>
      </div>
    </div>
    <v-alert v-if="refusal" type="error" variant="tonal" density="compact" class="mt-3">
      {{ t(`productImage.errors.${refusal}`) }}
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
