import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import SaveStatus from './SaveStatus.vue';
import type { SaveOutcome } from './saveOutcome';
import { vuetify } from '@/test/setup';

function mountStatus(outcome: SaveOutcome | null) {
  return mount(SaveStatus, {
    props: { outcome, saved: 'Saved.', invalid: 'Invalid.', failed: 'Failed.' },
    global: { plugins: [vuetify] },
  });
}

describe('SaveStatus.vue', () => {
  it('should show nothing before a save', () => {
    // Act
    const wrapper = mountStatus(null);
    // Assert
    expect(wrapper.find('[data-test="save-status"]').exists()).toBe(false);
  });

  it.each([
    ['saved', 'Saved.', 'text-success'],
    ['invalid', 'Invalid.', 'text-error'],
    ['failed', 'Failed.', 'text-error'],
  ] as const)('should show the message of a save that is %s in its colour', (outcome, message, colour) => {
    // Act
    const wrapper = mountStatus(outcome);
    // Assert
    const status = wrapper.find('[data-test="save-status"]');
    expect(status.text()).toBe(message);
    expect(status.classes()).toContain(colour);
  });
});
