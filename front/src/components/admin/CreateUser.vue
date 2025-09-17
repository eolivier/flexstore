<script setup>
  import { useUsersStore } from '../../stores/users.ts';
  import { nextTick, ref } from 'vue';

  const name = ref('');
  const email = ref('');
  const valid = ref(false);
  const formRef = ref(null);

  const usersStore = useUsersStore();

  const rules = {
    required: (v) => !!v || 'Champ obligatoire',
  };

  // 👉 Fonction que tu veux appeler
  const createUser = async (name, email) => {
    const newUser = {
      id: Date.now(), // ou utilise uuid si besoin
      name: name,
      email: email,
    };
    await usersStore.addUser(newUser);
    resetForm();
  };

  const submitForm = () => {
    if (valid.value) {
      createUser(name.value, email.value);
    }
  };

  const resetForm = () => {
    name.value = '';
    email.value = '';
    valid.value = false;
    nextTick(() => {
      if (formRef.value) {
        formRef.value.resetValidation();
      }
    });
  };
</script>

<template>
  <v-container class="pa-4" max-width="400">
    <v-form ref="formRef" v-model="valid" @submit.prevent="submitForm">
      <v-text-field
        v-model="name"
        label="Nom de l'utilisateur"
        :rules="[rules.required]"
        required
      />

      <v-text-field
        v-model="email"
        label="Email de l'utilisateur"
        :rules="[rules.required]"
        required
      />

      <v-btn type="submit" color="primary" class="mt-2"> Créer l’utilisateur </v-btn>
    </v-form>
  </v-container>
</template>
