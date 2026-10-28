<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { isWellFormedSku, SKU_MAX_LENGTH } from '@/utils/sku';

  // The SKU of a product, with a tooltip that explains what it is
  const sku = defineModel<string>({ required: true });
  defineProps<{ alreadyUsed: boolean }>();

  const { t } = useI18n();

  const rules = [
    (v: string) => !!v.trim() || t('createProduct.mandatoryField'),
    (v: string) => isWellFormedSku(v) || t('sku.invalid', { max: SKU_MAX_LENGTH }),
  ];
</script>

<template>
  <v-text-field
    v-model="sku"
    :label="t('sku.label')"
    :rules="rules"
    :error-messages="alreadyUsed ? t('sku.alreadyUsed') : []"
    :maxlength="SKU_MAX_LENGTH"
    class="sku"
    prepend-inner-icon="mdi-barcode"
    required
  >
    <template #append-inner>
      <v-tooltip location="top" max-width="320">
        <template #activator="{ props: tooltip }">
          <v-icon v-bind="tooltip" icon="mdi-information-outline" :aria-label="t('sku.help')" tabindex="0" />
        </template>
        {{ t('sku.tooltip') }}
      </v-tooltip>
    </template>
  </v-text-field>
</template>

<style scoped>
  .sku :deep(input) {
    text-transform: uppercase;
  }
</style>
