import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import BillingDocumentButton, { type BillingDocument } from './BillingDocumentButton.vue';
import { twoLinesOrder } from './orderFixture';
import { vuetify, i18n } from '@/test/setup';
import { saveFile } from '@/utils/download';

vi.mock('@/utils/download', () => ({ saveFile: vi.fn() }));

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const pdf = new Blob(['%PDF-'], { type: 'application/pdf' });

function mountButton(document: BillingDocument) {
  return mount(BillingDocumentButton, {
    props: { order: twoLinesOrder, ordersPath: '/api/orders', document },
    global: { plugins: [vuetify, i18n] },
  });
}

describe('BillingDocumentButton.vue', () => {
  beforeAll(() => setActivePinia(createPinia()));

  beforeEach(() => {
    mockFetch.mockReset();
    vi.mocked(saveFile).mockReset();
  });

  it.each([
    { document: 'invoice' as const, path: 'invoice', fileName: 'facture-46555C9F.pdf' },
    { document: 'creditNote' as const, path: 'credit-note', fileName: 'avoir-46555C9F.pdf' },
  ])('saves the $document of the order as $fileName', async ({ document, path, fileName }) => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 200, blob: () => Promise.resolve(pdf) });
    const wrapper = mountButton(document);
    // Act
    await wrapper.find('button').trigger('click');
    await flushPromises();
    // Assert
    const [url] = mockFetch.mock.calls[0];
    expect(url).toMatch(new RegExp(`/api/orders/${twoLinesOrder.id}/${path}$`));
    expect(saveFile).toHaveBeenCalledWith(pdf, fileName);
  });

  it('names the document it downloads', () => {
    // Act
    const wrapper = mountButton('creditNote');
    // Assert
    expect(wrapper.find('button').text()).toBe('Download the credit note');
  });

  it('says that the document could not be downloaded', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 404 });
    const wrapper = mountButton('invoice');
    // Act
    await wrapper.find('button').trigger('click');
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain('The invoice could not be downloaded.');
    expect(saveFile).not.toHaveBeenCalled();
  });
});
