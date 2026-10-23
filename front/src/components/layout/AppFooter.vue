<script setup>
  import { useI18n } from 'vue-i18n';
  import { shopLinks } from '../menu/navigation';

  const { t } = useI18n();
  const year = new Date().getFullYear();
</script>

<template>
  <v-footer color="primary" class="footer pt-10 pb-6">
    <div class="footer-inner w-100 px-4">
      <v-row>
        <v-col cols="12" md="5">
          <div class="d-flex align-center mb-3">
            <span class="brand-mark d-flex align-center justify-center mr-2">
              <v-icon icon="mdi-shopping" size="20" color="white" />
            </span>
            <span class="text-h6 font-weight-bold">Flexstore</span>
          </div>
          <p class="text-body-2 footer-muted">{{ t('footer.tagline') }}</p>
        </v-col>
        <v-col cols="6" md="4">
          <div class="text-overline footer-muted mb-2">{{ t('nav.shop') }}</div>
          <router-link
            v-for="link in shopLinks"
            :key="link.to"
            :to="link.to"
            class="footer-link d-block text-body-2 mb-1"
          >
            {{ t(link.label) }}
          </router-link>
        </v-col>
        <v-col cols="6" md="3">
          <div class="text-overline footer-muted mb-2">Flexstore</div>
          <router-link to="/about" class="footer-link d-block text-body-2 mb-1">{{ t('nav.about') }}</router-link>
          <router-link to="/orders" class="footer-link d-block text-body-2 mb-1">{{ t('nav.orders') }}</router-link>
          <router-link to="/user-profile" class="footer-link d-block text-body-2 mb-1">{{ t('nav.account') }}</router-link>
        </v-col>
      </v-row>
      <v-divider class="my-6" />
      <div class="text-caption footer-muted">© {{ year }} Flexstore. {{ t('footer.rights') }}</div>
    </div>
  </v-footer>
</template>

<style scoped>
  .footer {
    flex: 0 0 auto;
  }
  .footer-muted {
    opacity: 0.72;
  }
  .footer-inner {
    max-width: 1200px;
    margin: 0 auto;
  }
  .brand-mark {
    width: 34px;
    height: 34px;
    border-radius: 10px;
    background: rgb(var(--v-theme-secondary));
  }
  .footer-link {
    color: inherit;
    opacity: 0.72;
    text-decoration: none;
  }
  .footer-link:hover {
    opacity: 1;
  }
</style>
