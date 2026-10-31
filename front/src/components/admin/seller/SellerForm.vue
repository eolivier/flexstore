<script setup lang="ts">
  import { reactive, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useSellerStore, type Seller, type SellerUpdate } from '@/stores/seller.store';
  import { isSiret, isVatNumber } from './sellerRules';

  const props = defineProps<{ seller: Seller }>();

  const { t } = useI18n();
  const sellerStore = useSellerStore();

  const editedSeller = reactive<Seller>({ ...props.seller });
  // null until every field is checked
  const valid = ref<boolean | null>(true);
  const saving = ref(false);
  // null until saved
  const update = ref<SellerUpdate | null>(null);

  const rules = {
    required: (v: string) => v.trim().length > 0 || t('seller.required'),
    siret: (v: string) => isSiret(v) || t('seller.siretFormat'),
    vatNumber: (v: string) => isVatNumber(v) || t('seller.vatNumberFormat'),
  };

  async function save() {
    if (valid.value === false) return;
    saving.value = true;
    update.value = await sellerStore.updateSeller({ ...editedSeller });
    saving.value = false;
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
      v-model="editedSeller.siret"
      :label="t('seller.siret')"
      :hint="t('seller.siretHint')"
      :rules="[rules.siret]"
      class="mb-2"
    />
    <v-text-field
      v-model="editedSeller.vatNumber"
      :label="t('seller.vatNumber')"
      :hint="t('seller.vatNumberHint')"
      :rules="[rules.vatNumber]"
      class="mb-2"
    />
    <div class="d-flex align-center ga-4 mt-2">
      <v-btn type="submit" color="secondary" :loading="saving" :disabled="valid === false">{{
        t('seller.save')
      }}</v-btn>
      <span v-if="update === 'saved'" class="text-body-2 text-success">{{ t('seller.saved') }}</span>
      <span v-else-if="update === 'invalid'" class="text-body-2 text-error">{{ t('seller.invalid') }}</span>
      <span v-else-if="update === 'failed'" class="text-body-2 text-error">{{ t('seller.saveError') }}</span>
    </div>
  </v-form>
</template>
