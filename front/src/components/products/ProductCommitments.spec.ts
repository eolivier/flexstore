import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import ProductCommitments from './ProductCommitments.vue';
import { vuetify, i18n } from '@/test/setup';

describe('ProductCommitments.vue', () => {
  it('should show the commitments of the shop on delivery, returns and payment', () => {
    // Act
    const wrapper = mount(ProductCommitments, { global: { plugins: [vuetify, i18n] } });
    // Assert
    expect(wrapper.text()).toContain('Fast delivery');
    expect(wrapper.text()).toContain('Easy returns');
    expect(wrapper.text()).toContain('Secure payment');
  });
});
