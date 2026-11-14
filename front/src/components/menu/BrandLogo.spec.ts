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

  it('should color the name in primary by default', () => {
    // Act
    const wrapper = mount(BrandLogo, { global: { plugins: [vuetify] } });
    // Assert
    expect(wrapper.find('.text-h6').classes()).toContain('text-primary');
  });

  it('should use the given class for the name instead of the primary color', () => {
    // Act
    const wrapper = mount(BrandLogo, { props: { nameClass: 'text-white' }, global: { plugins: [vuetify] } });
    // Assert
    const nameClasses = wrapper.find('.text-h6').classes();
    expect(nameClasses).toContain('text-white');
    expect(nameClasses).not.toContain('text-primary');
  });
});
