import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { GetLegalPageKindEnum, JsonLegalPage } from '@/generated';
import { legalPagesApi } from '@/api';
import { useAuthStore } from '@/stores/auth.store';
import { jsonRequest } from '@/utils/jsonRequest';

// The legal pages of the shop, as named in the paths: its legal notice and its terms of sale
export type LegalPageKind = GetLegalPageKindEnum;

// saved; invalid: refused by the shop (an empty text, or too long); failed: not reached
export type LegalPagePublication = 'saved' | 'invalid' | 'failed';

// The current version of each legal page: read by everyone, published by the administrators. Its text is empty
// until the administrators write it
export const useLegalPagesStore = defineStore('legalPages', () => {
  const legalPages = ref<Partial<Record<LegalPageKind, JsonLegalPage>>>({});
  const loadErrors = ref<Partial<Record<LegalPageKind, boolean>>>({});
  const authStore = useAuthStore();

  async function fetchLegalPage(kind: LegalPageKind) {
    loadErrors.value[kind] = false;
    const response = await legalPagesApi.getLegalPage(kind).catch(() => null);
    if (response) {
      legalPages.value[kind] = response.data;
    } else {
      loadErrors.value[kind] = true;
    }
  }

  async function publishLegalPage(kind: LegalPageKind, text: string): Promise<LegalPagePublication> {
    const response = await authStore
      .authFetch(`/api/admin/legal-pages/${kind}`, jsonRequest('PUT', { text }))
      .catch(() => null);
    if (response?.status === 400) return 'invalid';
    if (!response?.ok) return 'failed';
    legalPages.value[kind] = await response.json();
    return 'saved';
  }

  return { legalPages, loadErrors, fetchLegalPage, publishLegalPage };
});
