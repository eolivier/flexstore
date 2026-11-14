<script setup lang="ts">
  import { computed, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useGuidedTourStore } from '@/stores/guidedTour.store';
  import { useGuidedTourFocus } from '@/composables/useGuidedTourFocus';
  import { guidedTourSections } from '@/utils/guidedTour';
  import GuidedTourActions from './GuidedTourActions.vue';
  import GuidedTourSpotlight from './GuidedTourSpotlight.vue';
  import GuidedTourStep from './GuidedTourStep.vue';

  // The guided tour: a dialog that presents the sections of the shop one after the other, bringing the part of
  // the page concerned into focus (the rest is darkened). The customer may ignore it at any step; at its end,
  // they may ask not to see it again
  const { t } = useI18n();
  const guidedTourStore = useGuidedTourStore();
  const stepIndex = ref(0);
  const doNotShowAgain = ref(false);
  const isLast = computed(() => stepIndex.value === guidedTourSections.length - 1);
  const focus = computed(() => guidedTourSections[stepIndex.value].focus);
  const { focusArea } = useGuidedTourFocus(focus, computed(() => guidedTourStore.isOpen));

  watch(
    () => guidedTourStore.isOpen,
    (isOpen) => {
      if (isOpen) {
        stepIndex.value = 0;
        doNotShowAgain.value = false;
      }
    }
  );
</script>

<template>
  <GuidedTourSpotlight v-if="guidedTourStore.isOpen" :area="focusArea" />
  <v-dialog
    :model-value="guidedTourStore.isOpen"
    max-width="480"
    persistent
    :scrim="false"
    @keydown.esc="guidedTourStore.skip()"
  >
    <v-card class="pa-2" role="dialog" :aria-label="t('guidedTour.title')">
      <v-card-title class="text-subtitle-2 text-medium-emphasis">
        {{ t('guidedTour.progress', { step: stepIndex + 1, total: guidedTourSections.length }) }}
      </v-card-title>
      <v-card-text>
        <GuidedTourStep :section="guidedTourSections[stepIndex]" />
        <v-checkbox
          v-if="isLast"
          v-model="doNotShowAgain"
          :label="t('guidedTour.doNotShowAgain')"
          color="primary"
          hide-details
        />
      </v-card-text>
      <GuidedTourActions
        :is-first="stepIndex === 0"
        :is-last="isLast"
        @skip="guidedTourStore.skip()"
        @previous="stepIndex--"
        @next="stepIndex++"
        @finish="guidedTourStore.finish(doNotShowAgain)"
      />
    </v-card>
  </v-dialog>
</template>
