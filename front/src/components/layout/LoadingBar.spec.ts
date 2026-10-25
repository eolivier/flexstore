import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import LoadingBar from './LoadingBar.vue';
import { useLoadingStore } from '@/stores/loading.store';
import { vuetify, i18n } from '@/test/setup';

const mountBar = () => mount(LoadingBar, { global: { plugins: [vuetify, i18n] } });
const isShown = (wrapper: ReturnType<typeof mountBar>) =>
  wrapper.find('[role="progressbar"]').attributes('aria-busy') === 'true';

describe('LoadingBar.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it.each([
    { elapsedMs: 100, shown: false },
    { elapsedMs: 200, shown: true },
  ])('during a load, after $elapsedMs ms the bar is shown: $shown', async ({ elapsedMs, shown }) => {
    // Arrange
    const wrapper = mountBar();
    useLoadingStore().track(new Promise(() => undefined));
    await wrapper.vm.$nextTick();
    // Act
    await vi.advanceTimersByTimeAsync(elapsedMs);
    // Assert
    expect(isShown(wrapper)).toBe(shown);
  });
});
