import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useLegalPagesStore } from './legalPages.store';

const getLegalPage = vi.fn();
vi.mock('@/api', () => ({
  apiBaseUrl: 'https://api.example',
  legalPagesApi: { getLegalPage: (kind: string) => getLegalPage(kind) },
}));

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const termsOfSale = { kind: 'TERMS_OF_SALE', text: 'Article 1 - Objet', publishedAt: '2026-11-02T10:00:00Z' };

describe('legalPages.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    getLegalPage.mockReset();
  });

  it('should read the current version of a legal page', async () => {
    // Arrange
    getLegalPage.mockResolvedValue({ data: termsOfSale });
    const store = useLegalPagesStore();
    // Act
    await store.fetchLegalPage('terms-of-sale');
    // Assert
    expect(getLegalPage).toHaveBeenCalledWith('terms-of-sale');
    expect(store.legalPages['terms-of-sale']).toEqual(termsOfSale);
    expect(store.loadErrors['terms-of-sale']).toBe(false);
  });

  it('should tell when a legal page could not be read', async () => {
    // Arrange
    getLegalPage.mockRejectedValue(new Error('network'));
    const store = useLegalPagesStore();
    // Act
    await store.fetchLegalPage('legal-notice');
    // Assert
    expect(store.loadErrors['legal-notice']).toBe(true);
  });

  it('should keep the published version as the current one', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(termsOfSale) });
    const store = useLegalPagesStore();
    // Act
    const publication = await store.publishLegalPage('terms-of-sale', 'Article 1 - Objet');
    // Assert
    expect(publication).toBe('saved');
    expect(mockFetch.mock.calls[0][0]).toContain('/api/admin/legal-pages/terms-of-sale');
    expect(JSON.parse(mockFetch.mock.calls[0][1].body)).toEqual({ text: 'Article 1 - Objet' });
    expect(store.legalPages['terms-of-sale']).toEqual(termsOfSale);
  });

  it.each([
    { status: 400, ok: false, expected: 'invalid' },
    { status: 500, ok: false, expected: 'failed' },
  ])('a $status answer should read $expected', async ({ status, ok, expected }) => {
    // Arrange
    mockFetch.mockResolvedValue({ ok, status });
    const store = useLegalPagesStore();
    // Act
    const publication = await store.publishLegalPage('legal-notice', ' ');
    // Assert
    expect(publication).toBe(expected);
  });
});
