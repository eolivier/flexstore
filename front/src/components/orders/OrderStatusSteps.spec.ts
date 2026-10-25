import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import OrderStatusSteps from './OrderStatusSteps.vue';
import { vuetify, i18n } from '@/test/setup';

describe('OrderStatusSteps.vue', () => {
  it.each([
    { status: 'AWAITING_PAYMENT' as const, currentLabel: 'Awaiting payment', doneCount: 0 },
    { status: 'PAID' as const, currentLabel: 'Paid', doneCount: 1 },
    { status: 'PROCESSING' as const, currentLabel: 'Processing', doneCount: 2 },
    { status: 'SHIPPED' as const, currentLabel: 'Shipped', doneCount: 3 },
    { status: 'DELIVERED' as const, currentLabel: 'Delivered', doneCount: 4 },
  ])(
    'highlights $currentLabel as the current step of an order $status',
    ({ status, currentLabel, doneCount }) => {
      // Act
      const wrapper = mount(OrderStatusSteps, {
        props: { status },
        global: { plugins: [vuetify, i18n] },
      });
      // Assert
      const currentStep = wrapper.find('[aria-current="step"]');
      expect(currentStep.text()).toBe(currentLabel);
      expect(currentStep.classes()).toContain('step--current');
      expect(wrapper.findAll('.step--done')).toHaveLength(doneCount);
      expect(wrapper.findAll('li.step')).toHaveLength(5);
    }
  );

  it.each([
    { status: 'PROCESSING' as const, visibleLabel: 'Processing' },
    { status: 'SHIPPED' as const, visibleLabel: 'Shipped' },
  ])(
    'in the compact layout, only the current step $visibleLabel is labelled',
    ({ status, visibleLabel }) => {
      // Act
      const wrapper = mount(OrderStatusSteps, {
        props: { status, layout: 'compact' },
        global: { plugins: [vuetify, i18n] },
      });
      // Assert
      const visibleLabels = wrapper
        .findAll('.step-label')
        .filter((label) => !label.classes('d-sr-only'))
        .map((label) => label.text());
      expect(visibleLabels).toEqual([visibleLabel]);
      expect(wrapper.findAll('.step-label')).toHaveLength(5);
      expect(wrapper.find('[aria-current="step"]').classes()).toContain('step--current');
    }
  );
});
