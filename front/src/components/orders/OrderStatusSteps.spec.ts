import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import OrderStatusSteps from './OrderStatusSteps.vue';
import { vuetify, i18n } from '@/test/setup';

describe('OrderStatusSteps.vue', () => {
  it.each([
    { status: 'PROCESSING' as const, currentLabel: 'Processing', doneCount: 0 },
    { status: 'SHIPPED' as const, currentLabel: 'Shipped', doneCount: 1 },
    { status: 'DELIVERED' as const, currentLabel: 'Delivered', doneCount: 2 },
  ])('highlights $currentLabel as the current step of an order $status', ({ status, currentLabel, doneCount }) => {
    // Act
    const wrapper = mount(OrderStatusSteps, { props: { status }, global: { plugins: [vuetify, i18n] } });
    // Assert
    const currentStep = wrapper.find('[aria-current="step"]');
    expect(currentStep.text()).toBe(currentLabel);
    expect(currentStep.classes()).toContain('step--current');
    expect(wrapper.findAll('.step--done')).toHaveLength(doneCount);
    expect(wrapper.findAll('li.step')).toHaveLength(3);
  });
});
