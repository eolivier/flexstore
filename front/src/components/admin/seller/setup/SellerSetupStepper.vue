<script setup lang="ts">
  import { computed, reactive, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type { Seller, SellerUpdate } from '@/stores/seller.store';
  import { isStepComplete, SELLER_SETUP_STEPS } from './sellerSetupSteps';
  import SellerSetupIdentityStep from './SellerSetupIdentityStep.vue';
  import SellerSetupRegistrationStep from './SellerSetupRegistrationStep.vue';
  import SellerSetupContactStep from './SellerSetupContactStep.vue';
  import SellerSetupSocialNetworksStep from './SellerSetupSocialNetworksStep.vue';
  import SellerSetupSummary from './SellerSetupSummary.vue';

  const props = defineProps<{ seller: Seller; saving: boolean; refusal: SellerUpdate | null }>();
  const emit = defineEmits<{ finish: [seller: Seller] }>();

  const { t } = useI18n();

  const givenSeller = reactive<Seller>({ ...props.seller, socialNetworks: { ...props.seller.socialNetworks } });
  // The step shown, from 1
  const position = ref(1);

  const stepTitles = SELLER_SETUP_STEPS.map((step) => t(`sellerSetup.steps.${step}`));
  const currentStep = computed(() => SELLER_SETUP_STEPS[position.value - 1]);
  const isLastStep = computed(() => position.value === SELLER_SETUP_STEPS.length);
  const currentStepIsComplete = computed(() => isStepComplete(currentStep.value, givenSeller));
</script>

<template>
  <v-stepper v-model="position" :items="stepTitles" hide-actions>
    <template #item.1>
      <SellerSetupIdentityStep
        v-model:name="givenSeller.name"
        v-model:address="givenSeller.address"
        v-model:legal-form="givenSeller.legalForm"
        v-model:share-capital="givenSeller.shareCapital"
      />
    </template>
    <template #item.2>
      <SellerSetupRegistrationStep
        v-model:siret="givenSeller.siret"
        v-model:registry-city="givenSeller.registryCity"
        v-model:vat-number="givenSeller.vatNumber"
      />
    </template>
    <template #item.3>
      <SellerSetupContactStep
        v-model:phone="givenSeller.phone"
        v-model:email="givenSeller.email"
        v-model:website="givenSeller.website"
      />
    </template>
    <template #item.4>
      <SellerSetupSocialNetworksStep v-model:social-networks="givenSeller.socialNetworks" />
    </template>
    <template #item.5>
      <SellerSetupSummary :seller="givenSeller" />
      <p v-if="refusal === 'invalid'" class="text-body-2 text-error mt-2">{{ t('seller.invalid') }}</p>
      <p v-else-if="refusal === 'failed'" class="text-body-2 text-error mt-2">{{ t('seller.saveError') }}</p>
    </template>

    <v-stepper-actions>
      <template #prev>
        <v-btn :disabled="position === 1 || saving" @click="position--">{{ t('sellerSetup.previous') }}</v-btn>
      </template>
      <template #next>
        <v-btn
          v-if="isLastStep"
          color="secondary"
          :loading="saving"
          :disabled="!currentStepIsComplete"
          @click="emit('finish', { ...givenSeller, socialNetworks: { ...givenSeller.socialNetworks } })"
          >{{ t('sellerSetup.finish') }}</v-btn
        >
        <v-btn v-else color="secondary" :disabled="!currentStepIsComplete" @click="position++">{{
          t('sellerSetup.next')
        }}</v-btn>
      </template>
    </v-stepper-actions>
  </v-stepper>
</template>
