<script setup lang="ts">
  import FormActions from '@/components/form/FormActions.vue';
  import { computed, reactive, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useSellerStore, type Seller, type SellerUpdate } from '@/stores/seller.store';
  import SellerSocialNetworksFields from './SellerSocialNetworksFields.vue';
  import { useSellerFieldRules } from './setup/useSellerFieldRules';

  const props = defineProps<{ seller: Seller }>();

  const { t } = useI18n();
  const sellerStore = useSellerStore();

  const copyOf = (seller: Seller): Seller => ({ ...seller, socialNetworks: { ...seller.socialNetworks } });

  // the last saved values: what "Cancel" restores and what "Save" is compared with
  const initialSeller = ref<Seller>(copyOf(props.seller));
  const editedSeller = reactive<Seller>(copyOf(props.seller));
  const modified = computed(() => JSON.stringify(editedSeller) !== JSON.stringify(initialSeller.value));
  // null until every field is checked
  const valid = ref<boolean | null>(true);
  const saving = ref(false);
  // null until saved
  const update = ref<SellerUpdate | null>(null);

  const rules = useSellerFieldRules();

  async function save() {
    if (valid.value === false) return;
    saving.value = true;
    update.value = await sellerStore.updateSeller(copyOf(editedSeller));
    if (update.value === 'saved') initialSeller.value = copyOf(editedSeller);
    saving.value = false;
  }

  function cancel() {
    Object.assign(editedSeller, copyOf(initialSeller.value));
    update.value = null;
  }
</script>

<template>
  <v-form v-model="valid" @submit.prevent="save">
    <v-text-field v-model="editedSeller.name" :label="t('seller.name')" :rules="[rules.required]" class="mb-2" />
    <v-text-field
      v-model="editedSeller.address"
      :label="t('seller.address')"
      :hint="t('seller.addressHint')"
      :rules="[rules.required]"
      class="mb-2"
    />
    <v-text-field
      v-model="editedSeller.legalForm"
      :label="t('seller.legalForm')"
      :hint="t('seller.legalFormHint')"
      :rules="[rules.required]"
      class="mb-2"
    />
    <v-text-field
      v-model="editedSeller.shareCapital"
      :label="t('seller.shareCapital')"
      :hint="t('seller.shareCapitalHint')"
      :rules="[rules.shareCapital]"
      class="mb-2"
    />
    <v-text-field
      v-model="editedSeller.siret"
      :label="t('seller.siret')"
      :hint="t('seller.siretHint')"
      :rules="[rules.siret]"
      class="mb-2"
    />
    <v-text-field
      v-model="editedSeller.registryCity"
      :label="t('seller.registryCity')"
      :hint="t('seller.registryCityHint')"
      :rules="[rules.required]"
      class="mb-2"
    />
    <v-text-field
      v-model="editedSeller.vatNumber"
      :label="t('seller.vatNumber')"
      :hint="t('seller.vatNumberHint')"
      :rules="[rules.vatNumber]"
      class="mb-2"
    />
    <v-text-field
      v-model="editedSeller.phone"
      :label="t('seller.phone')"
      :rules="[rules.phone]"
      type="tel"
      prepend-inner-icon="mdi-phone-outline"
      class="mb-2"
    />
    <v-text-field
      v-model="editedSeller.email"
      :label="t('seller.email')"
      :rules="[rules.email]"
      type="email"
      prepend-inner-icon="mdi-email-outline"
      class="mb-2"
    />
    <v-text-field
      v-model="editedSeller.website"
      :label="t('seller.website')"
      :hint="t('seller.websiteHint')"
      :rules="[rules.website]"
      prepend-inner-icon="mdi-web"
      class="mb-2"
    />
    <h2 class="text-subtitle-1 font-weight-bold mt-4 mb-2">{{ t('socialNetworks.title') }}</h2>
    <SellerSocialNetworksFields v-model="editedSeller.socialNetworks" />
    <div class="d-flex align-center justify-end ga-2 mt-2">
      <span v-if="update === 'saved'" class="text-body-2 text-success">{{ t('seller.saved') }}</span>
      <span v-else-if="update === 'invalid'" class="text-body-2 text-error">{{ t('seller.invalid') }}</span>
      <span v-else-if="update === 'failed'" class="text-body-2 text-error">{{ t('seller.saveError') }}</span>
      <FormActions
        :loading="saving"
        :disabled="valid === false"
        :cancel-disabled="!modified"
        @cancel="cancel"
      />
    </div>
  </v-form>
</template>
