<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useFieldRules } from '@/composables/useFieldRules';
  import FeedbackAlert from '@/components/layout/FeedbackAlert.vue';
  import type { JsonShippingMethod } from '@/generated';
  import { useShippingMethodsStore, type ShippingMethodSaving } from '@/stores/shippingMethods.store';

  // A shipping method: added (nothing to edit), or changed (the method to edit): its name, its cost, and whether it
  // is free from the threshold of the shop settings
  const props = defineProps<{ editing: JsonShippingMethod | null }>();
  const emit = defineEmits<{ done: [] }>();

  const { t } = useI18n();
  const shippingMethodsStore = useShippingMethodsStore();

  const label = ref('');
  const cost = ref('4.90');
  const eligibility = ref<'ELIGIBLE' | 'NOT_ELIGIBLE'>('NOT_ELIGIBLE');
  const valid = ref(false);
  const saving = ref(false);
  const outcome = ref<ShippingMethodSaving | null>(null);

  const eligibilities = [
    { title: t('shippingMethods.eligible'), value: 'ELIGIBLE' },
    { title: t('shippingMethods.notEligible'), value: 'NOT_ELIGIBLE' },
  ];

  const rules = {
    ...useFieldRules(),
    cost: (v: string) => (v !== '' && Number(v) >= 0) || t('shippingMethods.costRule'),
  };

  watch(
    () => props.editing,
    (method) => {
      outcome.value = null;
      label.value = method?.label ?? '';
      cost.value = method ? String(method.cost) : '4.90';
      eligibility.value = method?.freeShippingEligibility ?? 'NOT_ELIGIBLE';
    },
    { immediate: true }
  );

  async function save() {
    if (!valid.value) return;
    saving.value = true;
    const definition = {
      label: label.value.trim(),
      cost: Number(cost.value),
      freeShippingEligibility: eligibility.value,
    };
    outcome.value = props.editing
      ? await shippingMethodsStore.updateShippingMethod(props.editing.id, {
          ...definition,
          status: props.editing.status,
        })
      : await shippingMethodsStore.createShippingMethod(definition);
    saving.value = false;
    if (outcome.value === 'saved') {
      label.value = '';
      emit('done');
    }
  }
</script>

<template>
  <v-card class="pa-6 mb-6">
    <h2 class="text-subtitle-1 font-weight-bold mb-4">
      {{ editing ? t('shippingMethods.edit', { label: editing.label }) : t('shippingMethods.new') }}
    </h2>
    <v-form v-model="valid" @submit.prevent="save">
      <v-row dense>
        <v-col cols="12" md="4">
          <v-text-field v-model="label" :label="t('shippingMethods.label')" :rules="[rules.required]" />
        </v-col>
        <v-col cols="6" md="2">
          <v-text-field
            v-model="cost"
            type="number"
            min="0"
            step="0.01"
            :label="t('shippingMethods.cost')"
            :rules="[rules.cost]"
          />
        </v-col>
        <v-col cols="6" md="3">
          <v-select v-model="eligibility" :items="eligibilities" :label="t('shippingMethods.freeFromThreshold')" />
        </v-col>
        <v-col cols="12" md="3" class="d-flex align-start ga-2">
          <v-btn type="submit" color="primary" size="large" :loading="saving" :disabled="!valid">
            {{ editing ? t('shippingMethods.save') : t('shippingMethods.create') }}
          </v-btn>
          <v-btn v-if="editing" variant="text" size="large" @click="emit('done')">
            {{ t('shippingMethods.cancel') }}
          </v-btn>
        </v-col>
      </v-row>
    </v-form>
    <FeedbackAlert v-if="outcome && outcome !== 'saved'" type="error" compact>
      {{ t(`shippingMethods.errors.${outcome}`) }}
    </FeedbackAlert>
  </v-card>
</template>
