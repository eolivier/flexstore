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
  it('puts Cancel on the left of Save', () => {
    // Act
    const wrapper = mountActions();
    // Assert
    expect(labels(wrapper)).toEqual(['Cancel', 'Save']);
  });

  it('writes the labels in the language of the shop', () => {
    // Arrange
    i18n.global.locale.value = 'fr';
    // Act
    const wrapper = mountActions();
    // Assert
    i18n.global.locale.value = 'en';
    expect(labels(wrapper)).toEqual(['Annuler', 'Enregistrer']);
  });

  it('submits the form with Save', () => {
    // Act
    const wrapper = mountActions();
    // Assert
    expect(wrapper.find('[data-test="form-save"]').attributes('type')).toBe('submit');
  });

  it('keeps Save enabled by default', () => {
    // Act
    const wrapper = mountActions();
    // Assert
    expect(wrapper.find('[data-test="form-save"]').attributes('disabled')).toBeUndefined();
  });

  it('disables Save when disabled', () => {
    // Act
    const wrapper = mountActions({ disabled: true });
    // Assert
    expect(wrapper.find('[data-test="form-save"]').attributes('disabled')).toBeDefined();
  });

  it('disables Cancel while loading', () => {
    // Act
    const wrapper = mountActions({ loading: true });
    // Assert
    expect(wrapper.find('[data-test="form-cancel"]').attributes('disabled')).toBeDefined();
  });

  it('disables Cancel when asked to', () => {
    // Act
    const wrapper = mountActions({ cancelDisabled: true });
    // Assert
    expect(wrapper.find('[data-test="form-cancel"]').attributes('disabled')).toBeDefined();
  });

  it('hides Cancel when the form is not cancellable', () => {
    // Act
    const wrapper = mountActions({ cancellable: false });
    // Assert
    expect(labels(wrapper)).toEqual(['Save']);
  });

  it('emits cancel when Cancel is clicked', async () => {
    // Arrange
    const wrapper = mountActions();
    // Act
    await wrapper.find('[data-test="form-cancel"]').trigger('click');
    // Assert
    expect(wrapper.emitted('cancel')).toHaveLength(1);
  });

  it('makes Cancel a link to the given address', () => {
    // Act
    const wrapper = mountActions({ cancelTo: '/admin/users' });
    // Assert
    expect(wrapper.find('a[data-test="form-cancel"]').attributes('href')).toBe('/admin/users');
  });

  it('replaces the label of Save with the slot', () => {
    // Act
    const wrapper = mountActions({}, { default: 'Create user' });
    // Assert
    expect(labels(wrapper)).toEqual(['Cancel', 'Create user']);
  });
});
