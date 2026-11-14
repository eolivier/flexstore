import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import GuidedTourSpotlight from './GuidedTourSpotlight.vue';

describe('GuidedTourSpotlight.vue', () => {
  it('should darken the whole page when no part is presented', () => {
    // Act
    const wrapper = mount(GuidedTourSpotlight, { props: { area: null } });
    // Assert
    expect(wrapper.classes()).toContain('dark');
    expect(wrapper.find('[data-testid="guided-tour-hole"]').exists()).toBe(false);
  });

  it('should leave the presented part clear, with a margin around it', () => {
    // Act
    const wrapper = mount(GuidedTourSpotlight, { props: { area: { top: 10, left: 20, width: 100, height: 40 } } });
    // Assert
    const hole = wrapper.find<HTMLElement>('[data-testid="guided-tour-hole"]');
    expect(wrapper.classes()).not.toContain('dark');
    expect(hole.element.style.top).toBe('4px');
    expect(hole.element.style.left).toBe('14px');
    expect(hole.element.style.width).toBe('112px');
    expect(hole.element.style.height).toBe('52px');
  });
});
