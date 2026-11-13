import { afterEach, describe, expect, it } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import EditTrackingButton from './EditTrackingButton.vue';
import type { JsonOrderTracking } from '@/generated';
import { vuetify, i18n } from '@/test/setup';

const current: JsonOrderTracking = { carrier: 'COLISSIMO', trackingNumber: '6A111', trackingUrl: null };

async function openDialogOf(tracking: JsonOrderTracking | null) {
  const wrapper = mount(EditTrackingButton, {
    props: { current: tracking, saving: false },
    global: { plugins: [vuetify, i18n] },
    attachTo: document.body,
  });
  await wrapper.find('[data-test="edit-tracking"]').trigger('click');
  await flushPromises();
  return wrapper;
}

const saveButton = () => document.querySelector<HTMLButtonElement>('[data-test="save-tracking"]')!;

describe('EditTrackingButton.vue', () => {
  afterEach(() => {
    document.body.innerHTML = '';
  });

  it('should start from the tracking of the order', async () => {
    // Act
    await openDialogOf(current);
    // Assert
    const numberInput = document.querySelector<HTMLInputElement>('[data-test="tracking-number"] input')!;
    expect(numberInput.value).toBe('6A111');
    expect(saveButton().disabled).toBe(false);
  });

  it('should save the tracking as it was left', async () => {
    // Arrange
    const wrapper = await openDialogOf(current);
    // Act
    saveButton().click();
    await flushPromises();
    // Assert
    expect(wrapper.emitted('correct')).toEqual([[{ carrier: 'COLISSIMO', trackingNumber: '6A111' }]]);
  });

  it('should not save before a carrier and a number are given, for an order shipped without tracking', async () => {
    // Act
    await openDialogOf(null);
    // Assert
    expect(saveButton().disabled).toBe(true);
  });
});
