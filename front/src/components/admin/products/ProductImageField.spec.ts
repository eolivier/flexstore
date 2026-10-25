import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ProductImageField from './ProductImageField.vue';
import { vuetify, i18n } from '@/test/setup';

vi.mock('@/api', () => ({
  apiBaseUrl: 'https://api.example',
  productImagesApi: { getProductsWithImage: vi.fn(() => Promise.resolve({ data: ['with-image'] })) },
}));

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

async function mountField(productId: string) {
  const wrapper = mount(ProductImageField, { props: { productId }, global: { plugins: [vuetify, i18n] } });
  await flushPromises();
  return wrapper;
}

async function choose(wrapper: Awaited<ReturnType<typeof mountField>>, file: File) {
  const input = wrapper.find('[data-test="image-input"]');
  Object.defineProperty(input.element, 'files', { value: [file] });
  await input.trigger('change');
  await flushPromises();
}

describe('ProductImageField.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it.each([
    { productId: 'with-image', expected: ['Replace the image', 'Remove the image'] },
    { productId: 'without-image', expected: ['Add an image'] },
  ])('offers $expected for the product $productId', async ({ productId, expected }) => {
    // Act
    const wrapper = await mountField(productId);
    // Assert
    expect(wrapper.findAll('button').map((button) => button.text())).toEqual(expected);
  });

  it('sends the chosen image to be resized', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 204 });
    const wrapper = await mountField('without-image');
    // Act
    await choose(wrapper, new File([new Uint8Array(1000)], 'lamp.png', { type: 'image/png' }));
    // Assert
    const [url, init] = mockFetch.mock.calls[0];
    expect(url).toMatch(/\/api\/admin\/products\/without-image\/image$/);
    expect((init.body as FormData).get('image')).toBeInstanceOf(File);
    expect(wrapper.text()).toContain('Replace the image');
  });

  it('tells that an image of more than 2 MB is refused', async () => {
    // Arrange
    const wrapper = await mountField('without-image');
    // Act
    await choose(wrapper, new File([new Uint8Array(2 * 1024 * 1024 + 1)], 'big.png', { type: 'image/png' }));
    // Assert
    expect(wrapper.text()).toContain('The image is larger than 2 MB.');
  });
});
