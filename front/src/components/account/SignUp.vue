<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRoute, useRouter } from 'vue-router';
  import { useAuthStore, type Registration } from '@/stores/auth.store';
  import type { ExpressAccountRegistration, CustomerCompany, Location } from '@/models/account';
  import { normalizedIdentifier } from '@/utils/company';
  import UserNameFields from './UserNameFields.vue';
  import PasswordField from './PasswordField.vue';
  import SignUpDetailsFields from './SignUpDetailsFields.vue';
  import FeedbackAlert from '@/components/layout/FeedbackAlert.vue';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const authStore = useAuthStore();

  // The express registration: the names, the email and the password
  const registration = ref<ExpressAccountRegistration>({ firstName: '', lastName: '', email: '', password: '' });
  // The customer may give their phone number and their postal address right now, or later from their account
  const givesDetails = ref(false);
  const phoneNumber = ref('');
  // A customer who buys for their company gives it: the address is then sent to the company, for their attention
  const isCompany = ref(false);
  const company = ref<CustomerCompany>({ companyName: '', siren: '', vatNumber: '' });
  // The customer receives what is sent to the address they give
  const location = ref<Location>({ street: '', complement: '', postalCode: '', city: '', country: 'France' });
  const valid = ref(false);
  const saving = ref(false);
  const result = ref<Registration | null>(null);

  const rules = {
    required: (v: string) => !!v?.trim() || t('createUser.mandatoryField'),
    email: (v: string) => /.+@.+\..+/.test(v) || t('createUser.invalidEmail'),
    passwordLength: (v: string) => v.length >= 8 || t('createUser.passwordTooShort'),
  };

  function companyAsSent(): CustomerCompany {
    return {
      companyName: company.value.companyName.trim(),
      siren: normalizedIdentifier(company.value.siren),
      vatNumber: normalizedIdentifier(company.value.vatNumber),
    };
  }

  function details() {
    const { firstName, lastName } = registration.value;
    const customerCompany = isCompany.value ? companyAsSent() : undefined;
    const recipient = {
      recipientType: customerCompany ? ('LEGAL_ENTITY' as const) : ('PRIVATE_INDIVIDUAL' as const),
      firstName,
      lastName,
      companyName: customerCompany?.companyName ?? '',
      vatNumber: customerCompany?.vatNumber ?? '',
    };
    const companyPart = customerCompany ? { company: customerCompany } : {};
    return {
      phoneNumber: phoneNumber.value.trim(),
      postalAddress: { ...location.value, ...recipient },
      ...companyPart,
    };
  }

  // Once signed in, the customer goes back to where they came from (e.g. the cart, to order)
  async function submit() {
    if (!valid.value) return;
    saving.value = true;
    const detailsPart = givesDetails.value ? details() : {};
    result.value = await authStore.register({ ...registration.value, ...detailsPart });
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
        <v-checkbox
          v-model="givesDetails"
          :label="t('signUp.giveDetails')"
          density="compact"
          hide-details
          class="mb-2"
          data-test="give-details"
        />
        <SignUpDetailsFields
          v-if="givesDetails"
          v-model:phone-number="phoneNumber"
          v-model:is-company="isCompany"
          v-model:company="company"
          v-model:location="location"
        />
        <FeedbackAlert
          v-if="result === 'emailUsed' || result === 'invalidCompany' || result === 'error'"
          type="error"
          compact
          class="my-4"
        >
          {{ t(`signUp.${result}`) }}
        </FeedbackAlert>
        <v-btn type="submit" color="primary" size="large" block class="mt-2" :loading="saving" :disabled="!valid">{{
          t('signUp.submit')
        }}</v-btn>
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
