import { beforeAll, describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import NewProductImageField from './NewProductImageField.vue';
import { vuetify, i18n } from '@/test/setup';

beforeAll(() => {
  URL.createObjectURL = () => 'blob:preview';
  URL.revokeObjectURL = () => undefined;
});

function mountImageField() {
  return mount(NewProductImageField, { props: { modelValue: null }, global: { plugins: [vuetify, i18n] } });
}

async function choose(wrapper: ReturnType<typeof mountImageField>, file: File) {
  const input = wrapper.find('[data-test="image-input"]');
  Object.defineProperty(input.element, 'files', { value: [file], configurable: true });
  await input.trigger('change');
}

describe('NewProductImageField.vue', () => {
  it('keeps the image chosen, to give it to the product once created', async () => {
    // Arrange
    const wrapper = mountImageField();
    const photo = new File(['photo'], 'lamp.jpg', { type: 'image/jpeg' });
    // Act
    await choose(wrapper, photo);
    // Assert
    expect(wrapper.emitted('update:modelValue')).toEqual([[photo]]);
  });

  it.each([
    { file: new File(['text'], 'notes.txt', { type: 'text/plain' }), error: 'not a JPEG, PNG or WebP image' },
    {
      file: new File([new Uint8Array(2 * 1024 * 1024 + 1)], 'huge.png', { type: 'image/png' }),
      error: 'larger than 2 MB',
    },
  ])('refuses $file.name', async ({ file, error }) => {
    // Arrange
    const wrapper = mountImageField();
    // Act
    await choose(wrapper, file);
    // Assert
    expect(wrapper.text()).toContain(error);
    expect(wrapper.emitted('update:modelValue')).toBeUndefined();
  });

  it('shows the image chosen', () => {
    // Act
    const photo = new File(['photo'], 'lamp.jpg', { type: 'image/jpeg' });
    const wrapper = mount(NewProductImageField, {
      props: { modelValue: photo },
      global: { plugins: [vuetify, i18n] },
    });
    // Assert
    expect(wrapper.find('img').attributes('src')).toBe('blob:preview');
  });
});
