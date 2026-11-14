<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import AdminBreadcrumbs from '@/components/admin/AdminBreadcrumbs.vue';
  import FeedbackAlert from '@/components/layout/FeedbackAlert.vue';
  import LoadError from '@/components/layout/LoadError.vue';
  import type { JsonShippingMethod } from '@/generated';
  import { useShippingMethodsStore, type ShippingMethodSaving } from '@/stores/shippingMethods.store';
  import { formatPrice } from '@/utils/price';
  import ShippingMethodForm from './ShippingMethodForm.vue';

  // The shipping methods: the administrator adds them, changes them, and enables or disables them
  const shippingMethodsStore = useShippingMethodsStore();
  const { t, locale } = useI18n();

  // The method being edited; null to add a new one
  const editedMethod = ref<JsonShippingMethod | null>(null);
  const statusOutcome = ref<ShippingMethodSaving | null>(null);

  onMounted(() => shippingMethodsStore.fetchShippingMethods());

  const headers = computed(() => [
    { title: t('shippingMethods.label'), key: 'label' },
    { title: t('shippingMethods.cost'), key: 'cost', align: 'end' as const },
    { title: t('shippingMethods.freeFromThreshold'), key: 'freeShippingEligibility', sortable: false },
    { title: t('shippingMethods.enabled'), key: 'status', sortable: false },
    { title: '', key: 'actions', sortable: false, align: 'end' as const },
  ]);

  async function switchStatus(method: JsonShippingMethod) {
    const status = method.status === 'ENABLED' ? 'DISABLED' : 'ENABLED';
    const { label, cost, freeShippingEligibility } = method;
    statusOutcome.value = await shippingMethodsStore.updateShippingMethod(method.id, {
      label,
      cost,
      freeShippingEligibility,
      status,
    });
  }
</script>

<template>
  <v-container class="py-10">
    <AdminBreadcrumbs />
    <h1 class="text-h4 font-weight-bold mb-6">{{ t('shippingMethods.title') }}</h1>
    <ShippingMethodForm :editing="editedMethod" @done="editedMethod = null" />
    <LoadError v-if="shippingMethodsStore.loadError" :message="t('shippingMethods.loadError')" class="mb-4" />
    <FeedbackAlert v-if="statusOutcome && statusOutcome !== 'saved'" type="error" class="mb-4">
      {{ t(`shippingMethods.errors.${statusOutcome}`) }}
    </FeedbackAlert>
    <v-data-table
      :headers="headers"
      :items="shippingMethodsStore.shippingMethods"
      :loading="shippingMethodsStore.loading"
      :no-data-text="t('shippingMethods.empty')"
      item-value="id"
    >
      <template #item.label="{ item }">
        <span class="font-weight-medium">{{ item.label }}</span>
      </template>
      <template #item.cost="{ item }">{{ formatPrice(item.cost, '€', locale) }}</template>
      <template #item.freeShippingEligibility="{ item }">
        {{
          item.freeShippingEligibility === 'ELIGIBLE' ? t('shippingMethods.eligible') : t('shippingMethods.notEligible')
        }}
      </template>
      <template #item.status="{ item }">
        <v-switch
          :model-value="item.status === 'ENABLED'"
          :aria-label="t('shippingMethods.toggle', { label: item.label })"
          color="primary"
          density="compact"
          hide-details
          @update:model-value="switchStatus(item)"
        />
      </template>
      <template #item.actions="{ item }">
        <v-btn
          icon="mdi-pencil-outline"
          variant="text"
          size="small"
          :aria-label="t('shippingMethods.edit', { label: item.label })"
          @click="editedMethod = item"
        />
      </template>
    </v-data-table>
  </v-container>
</template>
