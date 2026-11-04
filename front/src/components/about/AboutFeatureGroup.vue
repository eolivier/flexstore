<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import AboutFeatureCard from '@/components/about/AboutFeatureCard.vue';
  import type { AboutFeatureGroup } from '@/components/about/aboutFeatures';

  defineProps<{ group: AboutFeatureGroup }>();

  const { t } = useI18n();
</script>

<template>
  <section class="mb-10">
    <div class="d-flex align-center ga-3 mb-1">
      <v-avatar :color="group.color" variant="tonal" rounded="lg" size="44">
        <v-icon :icon="group.icon" />
      </v-avatar>
      <h3 class="text-h6 font-weight-bold">{{ t(`about.features.${group.key}.title`) }}</h3>
    </div>
    <p class="text-body-2 text-medium-emphasis mb-4">{{ t(`about.features.${group.key}.subtitle`) }}</p>
    <v-row>
      <v-col v-for="feature in group.features" :key="feature.key" cols="12" sm="6">
        <AboutFeatureCard
          :icon="feature.icon"
          :color="group.color"
          :title="t(`about.features.${group.key}.${feature.key}.title`)"
          :text="t(`about.features.${group.key}.${feature.key}.text`)"
        />
      </v-col>
    </v-row>
  </section>
</template>
