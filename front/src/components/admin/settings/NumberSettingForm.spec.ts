import { describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import NumberSettingForm from './NumberSettingForm.vue';
import { vuetify, i18n } from '@/test/setup';

function mountForm(save = vi.fn().mockResolvedValue(true)) {
  const wrapper = mount(NumberSettingForm, {
    props: { stored: 10, label: 'Delay', rule: () => true as const, save, min: 1 },
    global: { plugins: [vuetify, i18n] },
  });
  return { wrapper, save };
}

const cancelButton = (wrapper: ReturnType<typeof mountForm>['wrapper']) => wrapper.find('[data-test="form-cancel"]');
const saveButton = (wrapper: ReturnType<typeof mountForm>['wrapper']) => wrapper.find('[data-test="form-save"]');

describe('NumberSettingForm.vue', () => {
  it('should offer Save enabled and Cancel disabled while nothing changed', () => {
    // Act
    const { wrapper } = mountForm();
    // Assert
    expect(saveButton(wrapper).attributes('disabled')).toBeUndefined();
    expect(cancelButton(wrapper).attributes('disabled')).toBeDefined();
  });

  it('should enable Cancel once the value differs from the stored one', async () => {
    // Arrange
    const { wrapper } = mountForm();
    // Act
    await wrapper.find('input').setValue('25');
    // Assert
    expect(cancelButton(wrapper).attributes('disabled')).toBeUndefined();
  });

  it('should disable Cancel again when the stored value is typed back', async () => {
    // Arrange
    const { wrapper } = mountForm();
    await wrapper.find('input').setValue('25');
    // Act
    await wrapper.find('input').setValue('10');
    // Assert
    expect(cancelButton(wrapper).attributes('disabled')).toBeDefined();
  });

  it('should restore the stored value with Cancel and forget the message of the save', async () => {
    // Arrange
    const { wrapper } = mountForm(vi.fn().mockResolvedValue(false));
    await wrapper.find('input').setValue('25');
    await wrapper.find('form').trigger('submit');
    await flushPromises();
    // Act
    await cancelButton(wrapper).trigger('click');
    // Assert
    expect((wrapper.find('input').element as HTMLInputElement).value).toBe('10');
    expect(wrapper.text()).not.toContain('could not be saved');
  });

  it('should give the number to save, and take it as the new stored value', async () => {
    // Arrange
    const { wrapper, save } = mountForm();
    await wrapper.find('input').setValue('25');
    // Act
    await wrapper.find('form').trigger('submit');
    await flushPromises();
    // Assert
    expect(save).toHaveBeenCalledWith(25);
    expect(wrapper.text()).toContain('Saved.');
    expect(cancelButton(wrapper).attributes('disabled')).toBeDefined();
  });

  it('should tell that a value could not be saved, and keep it editable', async () => {
    // Arrange
    const { wrapper } = mountForm(vi.fn().mockResolvedValue(false));
    await wrapper.find('input').setValue('25');
    // Act
    await wrapper.find('form').trigger('submit');
    await flushPromises();
    // Assert
    expect(wrapper.text()).toContain('could not be saved');
    expect(cancelButton(wrapper).attributes('disabled')).toBeUndefined();
  });
});
