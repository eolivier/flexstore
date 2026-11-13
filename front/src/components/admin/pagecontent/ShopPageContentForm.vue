<script setup lang="ts">
  import FormActions from '@/components/form/FormActions.vue';
  import { computed, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import {
    MAXIMUM_SHOP_PAGE_CONTENT_LENGTH,
    useShopPageContentStore,
    type ShopPageContentEdit,
    type ShopPageContentType,
  } from '@/stores/shopPageContent.store';

  const props = defineProps<{ type: ShopPageContentType }>();

  const IDENTIFIER = /^[A-Za-z0-9-]{1,50}$/;

  const { t } = useI18n();
  const shopPageContentStore = useShopPageContentStore();

  const text = ref(shopPageContentStore.textOf(props.type));
  const formRef = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null);
  // null until every field is checked
  const valid = ref<boolean | null>(true);
  const saving = ref(false);
  // null until saved
  const edit = ref<ShopPageContentEdit | null>(null);

  // The button stays disabled until the text differs from the saved one
  const modified = computed(() => text.value.trim() !== shopPageContentStore.textOf(props.type));

  // An empty text clears the content
  const rules = [
    (v: string) => v.length <= MAXIMUM_SHOP_PAGE_CONTENT_LENGTH || t('shopPageContent.tooLong'),
    (v: string) =>
      props.type !== 'GOOGLE_ANALYTICS_ID' ||
      v.trim() === '' ||
      IDENTIFIER.test(v.trim()) ||
      t('shopPageContent.badIdentifier'),
  ];

  async function save() {
    const validation = await formRef.value?.validate();
    if (!validation?.valid) return;
    saving.value = true;
    edit.value = await shopPageContentStore.editContent(props.type, text.value.trim());
    saving.value = false;
  }
</script>

<template>
  <v-form ref="formRef" v-model="valid" @submit.prevent="save">
    <v-textarea
      v-model="text"
      :label="t(`shopPageContent.${type}.label`)"
      :hint="t(`shopPageContent.${type}.hint`)"
      :rules="rules"
      :counter="MAXIMUM_SHOP_PAGE_CONTENT_LENGTH"
      persistent-hint
      rows="2"
      auto-grow
      class="mb-2"
    />
    <div class="d-flex align-center ga-4 mt-2">
      <FormActions
        :cancellable="false"
        :loading="saving"
        :disabled="valid === false || !modified"
      />
      <span v-if="edit === 'saved'" class="text-body-2 text-success">{{ t('shopPageContent.saved') }}</span>
      <span v-else-if="edit === 'invalid'" class="text-body-2 text-error">{{
        t('shopPageContent.invalid')
      }}</span>
      <span v-else-if="edit === 'failed'" class="text-body-2 text-error">{{
        t('shopPageContent.saveError')
      }}</span>
    </div>
  </v-form>
</template>
