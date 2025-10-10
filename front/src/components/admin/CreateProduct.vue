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
  required: v => !!v || 'Mandatory field',
  number: v => !isNaN(parseFloat(v)) || 'Must be a number',
};

const createProduct = async (name, description, price) => {
  const newProduct = {
    name,
    description,
    category: 'CLOTHING',
    price: parseFloat(price),
    currency: 'EUR',
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
    <h1 class="text-h5 font-weight-bold mb-6">Create product</h1>
    <v-form ref="formRef" v-model="valid" @submit.prevent="submitForm">
      <v-text-field
        v-model="name"
        label="Product Name"
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
        label="Price"
        :rules="[rules.required, rules.number]"
        required
        type="number"
      />
      <v-btn type="submit" color="primary" class="mt-2">Create product</v-btn>
    </v-form>
  </v-container>
</template>