import { afterEach, describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import FormDialog from '@/components/layout/FormDialog.vue';
import { vuetify, i18n } from '@/test/setup';

let mounted: ReturnType<typeof mountDialog> | undefined;

function mountDialog(slots = {}) {
  mounted = mount(FormDialog, {
    props: { modelValue: true, title: 'Disable it', text: 'Are you sure?' },
    slots,
    global: { plugins: [vuetify, i18n] },
    attachTo: document.body,
  });
  return mounted;
}

const dialogText = () => document.body.textContent ?? '';

describe('FormDialog', () => {
  afterEach(() => mounted?.unmount());

  it('should show its title, its explanation and its content', () => {
    // Act
    mountDialog({ default: '<p>The content</p>' });
    // Assert
    expect(dialogText()).toContain('Disable it');
    expect(dialogText()).toContain('Are you sure?');
    expect(dialogText()).toContain('The content');
  });

  it('should close on cancel', async () => {
    // Arrange
    const wrapper = mountDialog({ actions: '<button>Go</button>' });
    // Act
    document.querySelector<HTMLElement>('[data-test="dialog-cancel"]')!.click();
    // Assert
    expect(wrapper.emitted('update:modelValue')).toEqual([[false]]);
  });

  it('should show its actions after the cancel button', () => {
    // Act
    mountDialog({ actions: '<button data-test="go">Go</button>' });
    // Assert
    const buttons = [...document.querySelectorAll('.v-card form button')];
    expect(buttons.map((button) => button.textContent?.trim())).toEqual(['Cancel', 'Go']);
  });
});
