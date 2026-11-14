import { afterEach, describe, expect, it } from 'vitest';
import { enableAutoUnmount, mount } from '@vue/test-utils';
import BrandLogo from './BrandLogo.vue';
import { vuetify } from '@/test/setup';

enableAutoUnmount(afterEach);

describe('BrandLogo.vue', () => {
  it('should show the name of the shop next to its mark', () => {
    // Act
    const wrapper = mount(BrandLogo, { global: { plugins: [vuetify] } });
    // Assert
    expect(wrapper.text()).toBe('Flexstore');
    expect(wrapper.find('.brand-mark').exists()).toBe(true);
  });
});
