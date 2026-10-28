<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useProductsStore } from '@/stores/products.store';
  import { isWellFormedSku, SKU_MAX_LENGTH } from '@/utils/sku';

  // The SKU of a product, with a tooltip that explains what it is, and a button that fills it with a SKU
  // proposed from the category and the name (in the default language) of the product
  const sku = defineModel<string>({ required: true });
  const props = defineProps<{ alreadyUsed: boolean; category: string; name: string }>();

  const { t } = useI18n();
  const productsStore = useProductsStore();

  const generating = ref(false);
  const generationFailed = ref(false);

  async function generate() {
    generating.value = true;
    const proposedSku = await productsStore.proposeSku(props.category, props.name);
    generating.value = false;
    generationFailed.value = proposedSku === null;
    if (proposedSku !== null) sku.value = proposedSku;
  }

  function errorMessages() {
    if (props.alreadyUsed) return [t('sku.alreadyUsed')];
    if (generationFailed.value) return [t('sku.generationFailed')];
    return [];
  }

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
    :error-messages="errorMessages()"
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
    <template #append>
      <v-btn
        :loading="generating"
        variant="tonal"
        color="primary"
        prepend-icon="mdi-auto-fix"
        class="generate"
        @click="generate"
      >
        {{ t('sku.generate') }}
      </v-btn>
    </template>
  </v-text-field>
</template>

<style scoped>
  .sku :deep(input) {
    text-transform: uppercase;
  }
</style>
