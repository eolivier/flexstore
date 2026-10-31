import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import InvoiceDownloadButton from './InvoiceDownloadButton.vue';
import { twoLinesOrder } from './orderFixture';
import { vuetify, i18n } from '@/test/setup';
import { saveFile } from '@/utils/download';

vi.mock('@/utils/download', () => ({ saveFile: vi.fn() }));

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const pdf = new Blob(['%PDF-'], { type: 'application/pdf' });

function mountButton() {
  return mount(InvoiceDownloadButton, {
    props: { order: twoLinesOrder, invoicePath: `/api/orders/${twoLinesOrder.id}/invoice` },
    global: { plugins: [vuetify, i18n] },
  });
}

describe('InvoiceDownloadButton.vue', () => {
  beforeAll(() => setActivePinia(createPinia()));

  beforeEach(() => {
    mockFetch.mockReset();
    vi.mocked(saveFile).mockReset();
  });

  it('saves the invoice of the order as facture-<number>.pdf', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 200, blob: () => Promise.resolve(pdf) });
    const wrapper = mountButton();
    // Act
    await wrapper.find('button').trigger('click');
    await flushPromises();
    // Assert
    const [url] = mockFetch.mock.calls[0];
    expect(url).toMatch(new RegExp(`/api/orders/${twoLinesOrder.id}/invoice$`));
    expect(saveFile).toHaveBeenCalledWith(pdf, 'facture-46555C9F.pdf');
  });

  it('says that the invoice could not be downloaded', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 404 });
    const wrapper = mountButton();
    // Act
    await wrapper.find('button').trigger('click');
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain('The invoice could not be downloaded.');
    expect(saveFile).not.toHaveBeenCalled();
  });
});
