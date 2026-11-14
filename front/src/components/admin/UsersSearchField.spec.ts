import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import UsersSearchField from './UsersSearchField.vue';
import { vuetify, i18n } from '@/test/setup';

const SEARCH_DELAY_MILLISECONDS = 300;

function mountField() {
  return mount(UsersSearchField, {
    props: { modelValue: '' },
    global: { plugins: [vuetify, i18n] },
  });
}

describe('UsersSearchField.vue', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should label the field with what can be searched', () => {
    // Act
    const wrapper = mountField();
    // Assert
    expect(wrapper.text()).toContain(i18n.global.t('users.search'));
  });

  it('should announce the words typed once the administrator stops typing', async () => {
    // Arrange
    const wrapper = mountField();
    const input = wrapper.find('input');
    // Act
    await input.setValue('ja');
    await input.setValue('jane');
    vi.advanceTimersByTime(SEARCH_DELAY_MILLISECONDS);
    // Assert
    expect(wrapper.emitted('update:modelValue')).toEqual([['jane']]);
  });

  it('should not announce the words while the administrator is typing', async () => {
    // Arrange
    const wrapper = mountField();
    // Act
    await wrapper.find('input').setValue('jane');
    // Assert
    expect(wrapper.emitted('update:modelValue')).toBeUndefined();
  });
});
