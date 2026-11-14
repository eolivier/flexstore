import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import FeedbackAlert from '@/components/layout/FeedbackAlert.vue';
import { vuetify } from '@/test/setup';

describe('FeedbackAlert', () => {
  it('should show its message', () => {
    // Act
    const wrapper = mount(FeedbackAlert, {
      props: { type: 'error' },
      slots: { default: 'Failed' },
      global: { plugins: [vuetify] },
    });
    // Assert
    expect(wrapper.text()).toContain('Failed');
  });

  it('should be compact on demand', () => {
    // Act
    const wrapper = mount(FeedbackAlert, { props: { type: 'warning', compact: true }, global: { plugins: [vuetify] } });
    // Assert
    expect(wrapper.find('.v-alert--density-compact').exists()).toBe(true);
  });
});
