<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';

  // The words searched among the users; the list is asked once the administrator stops typing
  const SEARCH_DELAY_MILLISECONDS = 300;

  const searchText = defineModel<string>({ required: true });
  const typedText = ref(searchText.value);
  const { t } = useI18n();
  let pendingSearch: ReturnType<typeof setTimeout> | undefined;

  watch(typedText, (text) => {
    clearTimeout(pendingSearch);
    pendingSearch = setTimeout(() => {
      searchText.value = text ?? '';
    }, SEARCH_DELAY_MILLISECONDS);
  });
</script>

<template>
  <v-text-field
    v-model="typedText"
    :label="t('users.search')"
    prepend-inner-icon="mdi-magnify"
    clearable
    hide-details
    class="mb-4"
  />
</template>
