import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import LegalPage from './LegalPage.vue';
import { vuetify, i18n } from '@/test/setup';

const getLegalPage = vi.fn();
vi.mock('@/api', () => ({
  apiBaseUrl: 'https://api.example',
  legalPagesApi: { getLegalPage: (kind: string) => getLegalPage(kind) },
}));

async function mountLegalPage(kind: 'legal-notice' | 'terms-of-sale') {
  const wrapper = mount(LegalPage, { props: { kind }, global: { plugins: [vuetify, i18n] } });
  await flushPromises();
  return wrapper;
}

describe('LegalPage.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    getLegalPage.mockReset();
  });

  it('should show the text of the page, with the date it was updated', async () => {
    // Arrange
    const text = 'Article 1 - Objet\nLes présentes conditions...';
    getLegalPage.mockResolvedValue({
      data: { kind: 'TERMS_OF_SALE', text, publishedAt: '2026-11-02T10:00:00Z' },
    });
    // Act
    const wrapper = await mountLegalPage('terms-of-sale');
    // Assert
    expect(wrapper.find('h1').text()).toBe('Terms of sale');
    expect(wrapper.find('.legal-text').text()).toBe(text);
    expect(wrapper.text()).toContain('2026');
  });

  it('should tell that a page not written yet has no text', async () => {
    // Arrange
    getLegalPage.mockResolvedValue({
      data: { kind: 'LEGAL_NOTICE', text: '', publishedAt: '2026-01-01T00:00:00Z' },
    });
    // Act
    const wrapper = await mountLegalPage('legal-notice');
    // Assert
    expect(wrapper.find('.legal-text').exists()).toBe(false);
    expect(wrapper.text()).toContain('This page has not been written yet.');
  });

  it('should tell when the page could not be loaded', async () => {
    // Arrange
    getLegalPage.mockRejectedValue(new Error('network'));
    // Act
    const wrapper = await mountLegalPage('legal-notice');
    // Assert
    expect(wrapper.text()).toContain('The page could not be loaded.');
  });
});
