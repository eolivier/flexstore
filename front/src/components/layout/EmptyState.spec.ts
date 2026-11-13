import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import EmptyState from './EmptyState.vue';
import { vuetify, i18n } from '@/test/setup';

function mountState(props = {}, slots = {}) {
  return mount(EmptyState, {
    props: { message: 'Nothing here', ...props },
    slots,
    global: { plugins: [vuetify, i18n] },
  });
}

describe('EmptyState.vue', () => {
  it('should show the message', () => {
    // Act
    const wrapper = mountState();
    // Assert
    expect(wrapper.text()).toContain('Nothing here');
  });

  it('should show the description under the message', () => {
    // Act
    const wrapper = mountState({ description: 'Come back later' });
    // Assert
    expect(wrapper.text()).toBe('Nothing hereCome back later');
  });

  it('should show the icon when given', () => {
    // Act
    const wrapper = mountState({ icon: 'mdi-cart-outline' });
    // Assert
    expect(wrapper.find('.mdi-cart-outline').exists()).toBe(true);
  });

  it('should show no icon by default', () => {
    // Act
    const wrapper = mountState();
    // Assert
    expect(wrapper.find('.v-icon').exists()).toBe(false);
  });

  it('should show the action of the slot', () => {
    // Act
    const wrapper = mountState({}, { default: '<button>Go shopping</button>' });
    // Assert
    expect(wrapper.find('button').text()).toBe('Go shopping');
  });

  it('should show only the message as a line of text when compact', () => {
    // Act
    const wrapper = mountState({ compact: true, icon: 'mdi-cart-outline' });
    // Assert
    expect([wrapper.find('.v-card').exists(), wrapper.find('.v-icon').exists()]).toEqual([false, false]);
  });
});
