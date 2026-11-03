<script setup lang="ts">
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { pageCountOf } from '@/utils/pagination';

  // The pages of a list: the page shown (numbered from 0) and the number of items of the whole list
  const props = defineProps<{ modelValue: number; total: number }>();
  const emit = defineEmits<{ 'update:modelValue': [page: number] }>();

  const { t } = useI18n();
  const pageCount = computed(() => pageCountOf(props.total));
  // Vuetify numbers the pages from 1
  const shownPage = computed(() => props.modelValue + 1);

  function choose(page: number) {
    emit('update:modelValue', page - 1);
  }
</script>

<template>
  <v-pagination
    v-if="pageCount > 1"
    :model-value="shownPage"
    :length="pageCount"
    :aria-label="t('pagination.label')"
    density="comfortable"
    class="mt-6"
    @update:model-value="choose"
  />
</template>
