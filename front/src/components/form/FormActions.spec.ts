import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import FormActions from './FormActions.vue';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

function mountActions(props = {}, slots = {}) {
  return mount(FormActions, { props, slots, global: { plugins: [vuetify, i18n, router] } });
}

const labels = (wrapper: ReturnType<typeof mountActions>) =>
  wrapper.findAll('.v-btn').map((button) => button.text());

describe('FormActions.vue', () => {
  it('should put Cancel on the left of Save', () => {
    // Act
    const wrapper = mountActions();
    // Assert
    expect(labels(wrapper)).toEqual(['Cancel', 'Save']);
  });

  it('should write the labels in the language of the shop', () => {
    // Arrange
    i18n.global.locale.value = 'fr';
    // Act
    const wrapper = mountActions();
    // Assert
    i18n.global.locale.value = 'en';
    expect(labels(wrapper)).toEqual(['Annuler', 'Enregistrer']);
  });

  it('should submit the form with Save', () => {
    // Act
    const wrapper = mountActions();
    // Assert
    expect(wrapper.find('[data-test="form-save"]').attributes('type')).toBe('submit');
  });

  it('should keep Save enabled by default', () => {
    // Act
    const wrapper = mountActions();
    // Assert
    expect(wrapper.find('[data-test="form-save"]').attributes('disabled')).toBeUndefined();
  });

  it('should disable Save when disabled', () => {
    // Act
    const wrapper = mountActions({ disabled: true });
    // Assert
    expect(wrapper.find('[data-test="form-save"]').attributes('disabled')).toBeDefined();
  });

  it('should disable Cancel while loading', () => {
    // Act
    const wrapper = mountActions({ loading: true });
    // Assert
    expect(wrapper.find('[data-test="form-cancel"]').attributes('disabled')).toBeDefined();
  });

  it('should disable Cancel when asked to', () => {
    // Act
    const wrapper = mountActions({ cancelDisabled: true });
    // Assert
    expect(wrapper.find('[data-test="form-cancel"]').attributes('disabled')).toBeDefined();
  });

  it('should hide Cancel when the form is not cancellable', () => {
    // Act
    const wrapper = mountActions({ cancellable: false });
    // Assert
    expect(labels(wrapper)).toEqual(['Save']);
  });

  it('should emit cancel when Cancel is clicked', async () => {
    // Arrange
    const wrapper = mountActions();
    // Act
    await wrapper.find('[data-test="form-cancel"]').trigger('click');
    // Assert
    expect(wrapper.emitted('cancel')).toHaveLength(1);
  });

  it('should make Cancel a link to the given address', () => {
    // Act
    const wrapper = mountActions({ cancelTo: '/admin/users' });
    // Assert
    expect(wrapper.find('a[data-test="form-cancel"]').attributes('href')).toBe('/admin/users');
  });

  it('should replace the label of Save with the slot', () => {
    // Act
    const wrapper = mountActions({}, { default: 'Create user' });
    // Assert
    expect(labels(wrapper)).toEqual(['Cancel', 'Create user']);
  });
});
