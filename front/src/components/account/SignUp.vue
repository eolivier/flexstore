<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRoute, useRouter } from 'vue-router';
  import { useAuthStore, type Registration } from '@/stores/auth.store';
  import type { AccountRegistration, Location } from '@/models/account';
  import UserNameFields from './UserNameFields.vue';
  import PasswordField from './PasswordField.vue';
  import PostalAddressFields from './PostalAddressFields.vue';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const authStore = useAuthStore();

  const registration = ref<Omit<AccountRegistration, 'postalAddress'>>({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    phoneNumber: '',
  });
  // The customer receives what is sent to the address they give
  const location = ref<Location>({
    street: '',
    complement: '',
    postalCode: '',
    city: '',
    country: 'France',
  });
  const valid = ref(false);
  const saving = ref(false);
  const result = ref<Registration | null>(null);

  const rules = {
    required: (v: string) => !!v?.trim() || t('createUser.mandatoryField'),
    email: (v: string) => /.+@.+\..+/.test(v) || t('createUser.invalidEmail'),
    passwordLength: (v: string) => v.length >= 8 || t('createUser.passwordTooShort'),
    phoneNumber: (v: string) => /^\+?[0-9][0-9 .()-]{5,19}$/.test(v.trim()) || t('signUp.invalidPhoneNumber'),
  };

  // Once signed in, the customer goes back to where they came from (e.g. the cart, to order)
  async function submit() {
    if (!valid.value) return;
    saving.value = true;
    const { firstName, lastName } = registration.value;
    const recipient = {
      recipientType: 'PRIVATE_INDIVIDUAL' as const,
      firstName,
      lastName,
      companyName: '',
      vatNumber: '',
    };
    const postalAddress = { ...location.value, ...recipient };
    result.value = await authStore.register({ ...registration.value, postalAddress });
    saving.value = false;
    if (result.value === 'signedIn') {
      router.push((route.query.redirect as string) || '/user-profile');
    }
  }
</script>

<template>
  <v-container class="page py-10">
    <h1 class="text-h4 font-weight-bold mb-1">{{ t('signUp.title') }}</h1>
    <p class="text-body-2 text-medium-emphasis mb-6">{{ t('signUp.subtitle') }}</p>
    <v-card class="pa-6">
      <v-form v-model="valid" @submit.prevent="submit">
        <UserNameFields v-model:first-name="registration.firstName" v-model:last-name="registration.lastName" />
        <v-text-field
          v-model="registration.email"
          :label="t('login.email')"
          :rules="[rules.required, rules.email]"
          type="email"
          prepend-inner-icon="mdi-email-outline"
          autocomplete="email"
          class="mb-2"
          required
        />
        <PasswordField
          v-model="registration.password"
          :label="t('signUp.password')"
          :rules="[rules.required, rules.passwordLength]"
          class="mb-2"
        />
        <v-text-field
          v-model="registration.phoneNumber"
          :label="t('signUp.phoneNumber')"
          :rules="[rules.required, rules.phoneNumber]"
          type="tel"
          prepend-inner-icon="mdi-phone-outline"
          autocomplete="tel"
          class="mb-4"
          required
        />
        <PostalAddressFields v-model="location" />
        <v-alert
          v-if="result === 'emailUsed' || result === 'error'"
          type="error"
          variant="tonal"
          density="compact"
          class="my-4"
        >
          {{ t(`signUp.${result}`) }}
        </v-alert>
        <v-btn
          type="submit"
          color="primary"
          size="large"
          block
          class="mt-2"
          :loading="saving"
          :disabled="!valid"
          >{{ t('signUp.submit') }}</v-btn
        >
      </v-form>
      <p class="text-body-2 text-center mt-6">
        {{ t('signUp.haveAccount') }}
        <router-link :to="{ path: '/login', query: route.query }">{{ t('nav.login') }}</router-link>
      </p>
    </v-card>
  </v-container>
</template>

<style scoped>
  .page {
    max-width: 640px;
  }
</style>
