<script setup lang="ts">
  import { nextTick, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useDiscountCodesStore, type DiscountCodeCreation } from '@/stores/discountCodes.store';

  // A new discount code: its code, a percentage or an amount off, until a day, for a number of orders
  const { t } = useI18n();
  const discountCodesStore = useDiscountCodesStore();

  const code = ref('');
  const reductionType = ref<'PERCENTAGE' | 'AMOUNT'>('PERCENTAGE');
  const reductionValue = ref('10');
  const expiresOn = ref('');
  const usageLimit = ref('100');
  const valid = ref(false);
  const saving = ref(false);
  const creation = ref<DiscountCodeCreation | null>(null);
  const formRef = ref<{ resetValidation: () => void } | null>(null);

  const reductionTypes = [
    { title: t('discountCodes.percentage'), value: 'PERCENTAGE' },
    { title: t('discountCodes.amount'), value: 'AMOUNT' },
  ];

  const rules = {
    code: (v: string) => /^[A-Za-z0-9]{3,30}$/.test(v.trim()) || t('discountCodes.codeRule'),
    reduction: (v: string) => {
      const value = Number(v);
      const maximum = reductionType.value === 'PERCENTAGE' ? 100 : Infinity;
      return (value > 0 && value <= maximum) || t(`discountCodes.reductionRule.${reductionType.value}`);
    },
    required: (v: string) => !!v || t('createProduct.mandatoryField'),
    usageLimit: (v: string) =>
      (Number.isInteger(Number(v)) && Number(v) >= 1) || t('discountCodes.usageLimitRule'),
  };

  async function create() {
    if (!valid.value) return;
    saving.value = true;
    creation.value = await discountCodesStore.createDiscountCode({
      code: code.value.trim(),
      reductionType: reductionType.value,
      reductionValue: Number(reductionValue.value),
      // Usable until the end of the day chosen, in Paris
      expiresAt: new Date(`${expiresOn.value}T23:59:59+01:00`).toISOString(),
      usageLimit: Number(usageLimit.value),
    });
    saving.value = false;
    if (creation.value === 'created') {
      code.value = '';
      nextTick(() => formRef.value?.resetValidation());
    }
  }
</script>

<template>
  <v-card class="pa-6 mb-6">
    <h2 class="text-subtitle-1 font-weight-bold mb-4">{{ t('discountCodes.new') }}</h2>
    <v-form ref="formRef" v-model="valid" @submit.prevent="create">
      <v-row dense>
        <v-col cols="12" md="4">
          <v-text-field v-model="code" :label="t('discountCodes.code')" :rules="[rules.code]" class="code" />
        </v-col>
        <v-col cols="6" md="4">
          <v-select v-model="reductionType" :items="reductionTypes" :label="t('discountCodes.reduction')" />
        </v-col>
        <v-col cols="6" md="4">
          <v-text-field
            v-model="reductionValue"
            type="number"
            min="0"
            step="0.01"
            :label="reductionType === 'PERCENTAGE' ? '%' : '€'"
            :rules="[rules.reduction]"
          />
        </v-col>
        <v-col cols="6" md="4">
          <v-text-field
            v-model="expiresOn"
            type="date"
            :label="t('discountCodes.expiresOn')"
            :rules="[rules.required]"
          />
        </v-col>
        <v-col cols="6" md="4">
          <v-text-field
            v-model="usageLimit"
            type="number"
            min="1"
            :label="t('discountCodes.usageLimit')"
            :rules="[rules.usageLimit]"
          />
        </v-col>
        <v-col cols="12" md="4" class="d-flex align-start">
          <v-btn type="submit" color="primary" size="large" block :loading="saving" :disabled="!valid">
            {{ t('discountCodes.create') }}
          </v-btn>
        </v-col>
      </v-row>
    </v-form>
    <v-alert v-if="creation && creation !== 'created'" type="error" variant="tonal" density="compact">
      {{ t(`discountCodes.errors.${creation}`) }}
    </v-alert>
  </v-card>
</template>

<style scoped>
  .code :deep(input) {
    text-transform: uppercase;
  }
</style>
