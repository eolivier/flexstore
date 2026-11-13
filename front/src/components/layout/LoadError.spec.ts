import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import LoadError from './LoadError.vue';
import { vuetify, i18n } from '@/test/setup';

function mountError(props = {}) {
  return mount(LoadError, {
    props: { message: 'It could not be loaded.', ...props },
    global: { plugins: [vuetify, i18n] },
  });
}

describe('LoadError.vue', () => {
  it('should show the message as an error alert', () => {
    // Act
    const wrapper = mountError();
    // Assert
    expect(wrapper.find('[role="alert"]').text()).toBe('It could not be loaded.');
  });

  it('should show no Retry button by default', () => {
    // Act
    const wrapper = mountError();
    // Assert
    expect(wrapper.find('[data-test="load-error-retry"]').exists()).toBe(false);
  });

  it('should show a Retry button when retryable', () => {
    // Act
    const wrapper = mountError({ retryable: true });
    // Assert
    expect(wrapper.find('[data-test="load-error-retry"]').text()).toBe('Retry');
  });

  it('should write Retry in the language of the shop', () => {
    // Arrange
    i18n.global.locale.value = 'fr';
    // Act
    const wrapper = mountError({ retryable: true });
    // Assert
    i18n.global.locale.value = 'en';
    expect(wrapper.find('[data-test="load-error-retry"]').text()).toBe('Réessayer');
  });

  it('should emit retry when Retry is clicked', async () => {
    // Arrange
    const wrapper = mountError({ retryable: true });
    // Act
    await wrapper.find('[data-test="load-error-retry"]').trigger('click');
    // Assert
    expect(wrapper.emitted('retry')).toHaveLength(1);
  });
});
