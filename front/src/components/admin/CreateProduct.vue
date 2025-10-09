<script setup>
import { ref, nextTick } from 'vue';
import { useProductsStore } from '../../stores/products.ts';

const name = ref('');
const description = ref('');
const price = ref(null);
const valid = ref(false);
const formRef = ref(null);

const productsStore = useProductsStore();

const rules = {
  required: v => !!v || 'Champ obligatoire',
  number: v => !isNaN(parseFloat(v)) || 'Doit être un nombre',
};

const createProduct = async (name, description, price) => {
  const newProduct = {
    name,
    description,
    category: 'CLOTHING', // Valeur par défaut, à adapter si nécessaire
    price: parseFloat(price),
    currency: 'EUR', // Valeur par défaut, à adapter si nécessaire
  };
  await productsStore.addProduct(newProduct);
  resetForm();
};

const submitForm = () => {
  if (valid.value) {
    createProduct(name.value, description.value, price.value);
  }
};

const resetForm = () => {
  name.value = '';
  description.value = '';
  price.value = null;
  valid.value = false;
  nextTick(() => {
    if (formRef.value) formRef.value.resetValidation();
  });
};
</script>

<template>
  <v-container class="pa-4" max-width="400">
    <v-form ref="formRef" v-model="valid" @submit.prevent="submitForm">
      <v-text-field
        v-model="name"
        label="Nom du produit"
        :rules="[rules.required]"
        required
      />
      <v-text-field
        v-model="description"
        label="Description"
        :rules="[rules.required]"
        required
      />
      <v-text-field
        v-model="price"
        label="Prix"
        :rules="[rules.required, rules.number]"
        required
        type="number"
      />
      <v-btn type="submit" color="primary" class="mt-2">Créer le produit</v-btn>
    </v-form>
  </v-container>
</template>