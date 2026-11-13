import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import ListPager from './ListPager.vue';

const mountPager = (page: number, total: number) => mount(ListPager, { props: { modelValue: page, total } });
const pageButton = (wrapper: ReturnType<typeof mountPager>, label: string) =>
  wrapper.findAll('button').find((button) => button.text() === label)!;

describe('ListPager.vue', () => {
  it.each([0, 1, 20])('should show no pages for a list of %i items', (total) => {
    // Act
    const wrapper = mountPager(0, total);
    // Assert
    expect(wrapper.find('button').exists()).toBe(false);
  });

  it('should show the pages of a list of more than 20 items', () => {
    // Act
    const wrapper = mountPager(0, 41);
    // Assert
    expect(pageButton(wrapper, '1').exists()).toBe(true);
    expect(pageButton(wrapper, '3').exists()).toBe(true);
    expect(pageButton(wrapper, '4')).toBeUndefined();
  });

  it('should give the page chosen, numbered from 0', async () => {
    // Arrange
    const wrapper = mountPager(0, 41);
    // Act
    await pageButton(wrapper, '3').trigger('click');
    // Assert
    expect(wrapper.emitted('update:modelValue')).toEqual([[2]]);
  });
});
