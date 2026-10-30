<script lang="ts">
  // editLink: the page of the product created, to finish it when its image or its stock was not saved
  export interface ProductCreationFeedback {
    show: boolean;
    color: string;
    text: string;
    editLink: string;
  }
</script>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  // How the creation of a product ended: created (link to the products), created in part (link to the page of
  // the product, to finish it), or not created
  const feedback = defineModel<ProductCreationFeedback>({ required: true });

  const { t } = useI18n();
</script>

<template>
  <v-snackbar
    v-model="feedback.show"
    :color="feedback.color"
    :timeout="feedback.editLink ? -1 : 3000"
    location="bottom right"
  >
    {{ feedback.text }}
    <template v-if="feedback.color !== 'error'" #actions>
      <v-btn v-if="feedback.editLink" :to="feedback.editLink" variant="text">{{
        t('createProduct.finish')
      }}</v-btn>
      <v-btn v-else to="/admin/products" variant="text" color="secondary">{{ t('nav.products') }}</v-btn>
    </template>
  </v-snackbar>
</template>
