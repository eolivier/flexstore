<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { hasPromotion, isValidPercentage, isValidPeriod, type PromotionForm } from '@/utils/promotion';

  // The promotion of the product: a percentage off the price, from a moment to a later one. Without
  // percentage, the product is not on promotion
  const promotion = defineModel<PromotionForm>({ required: true });

  const { t } = useI18n();

  const rules = {
    percentage: (typed: string) => !typed || isValidPercentage(typed) || t('promotion.invalidPercentage'),
    start: (typed: string) => !hasPromotion(promotion.value) || !!typed || t('promotion.mandatoryMoment'),
    end: (typed: string) =>
      !hasPromotion(promotion.value) ||
      !typed ||
      !promotion.value.startsAt ||
      isValidPeriod(promotion.value) ||
      t('promotion.invalidPeriod'),
  };
</script>

<template>
  <div>
    <v-text-field
      v-model="promotion.percentage"
      :label="t('promotion.title')"
      :hint="t('promotion.hint')"
      :rules="[rules.percentage]"
      persistent-hint
      type="number"
      min="0"
      max="100"
      step="0.01"
      suffix="%"
      clearable
    />
    <v-row v-if="promotion.percentage">
      <v-col cols="12" sm="6">
        <v-text-field
          v-model="promotion.startsAt"
          :label="t('promotion.startsAt')"
          :rules="[rules.start]"
          type="datetime-local"
          data-test="promotion-start"
        />
      </v-col>
      <v-col cols="12" sm="6">
        <v-text-field
          v-model="promotion.endsAt"
          :label="t('promotion.endsAt')"
          :rules="[rules.start, rules.end]"
          type="datetime-local"
          data-test="promotion-end"
        />
      </v-col>
    </v-row>
  </div>
</template>
