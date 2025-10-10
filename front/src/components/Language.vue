<template>
  <v-select
    :items="languages"
    v-model="currentLocale"
    label="Langue"
    @update:modelValue="changeLocale"
    class="ma-2"
    style="max-width: 200px"
  />
</template>

<script setup>
import { useI18n } from 'vue-i18n';
import { ref } from 'vue';

const { locale } = useI18n();
const languages = [
  { title: 'Français', value: 'fr' },
  { title: 'English', value: 'en' }
];
const currentLocale = ref(locale.value);

function changeLocale(val) {
  locale.value = val;
}
</script>