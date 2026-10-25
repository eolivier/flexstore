<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRoute, useRouter } from 'vue-router';

  // Searches the catalog from any page: the results are the page of all the products, filtered (?q=...)
  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();

  const search = ref(typeof route.query.q === 'string' ? route.query.q : '');

  watch(
    () => route.query.q,
    (q) => {
      search.value = typeof q === 'string' ? q : '';
    }
  );

  function submit() {
    const q = search.value.trim();
    router.push({ path: '/products/', query: q ? { q } : {} });
  }
</script>

<template>
  <v-text-field
    v-model="search"
    :placeholder="t('products.search')"
    :aria-label="t('products.search')"
    prepend-inner-icon="mdi-magnify"
    density="compact"
    variant="solo-filled"
    flat
    rounded="pill"
    hide-details
    single-line
    clearable
    class="catalog-search"
    @keydown.enter="submit"
    @click:clear="search = ''"
  />
</template>

<style scoped>
  .catalog-search {
    min-width: 200px;
    max-width: 320px;
  }
</style>
