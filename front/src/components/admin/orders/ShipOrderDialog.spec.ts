import { afterEach, describe, expect, it } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import ShipOrderDialog from './ShipOrderDialog.vue';
import TrackingFields from './TrackingFields.vue';
import { NO_TRACKING } from '@/utils/tracking';
import { vuetify, i18n } from '@/test/setup';

async function mountOpenDialog() {
  const wrapper = mount(ShipOrderDialog, {
    props: { modelValue: true },
    global: { plugins: [vuetify, i18n] },
    attachTo: document.body,
  });
  await flushPromises();
  return wrapper;
}

const buttonLabelled = (label: string) =>
  [...document.querySelectorAll('button')].find((button) => button.textContent?.trim() === label)!;

describe('ShipOrderDialog.vue', () => {
  afterEach(() => {
    document.body.innerHTML = '';
  });

  it('asks for the carrier and the tracking number', async () => {
    // Act
    await mountOpenDialog();
    // Assert
    expect(document.body.textContent).toContain('Ship this order');
    expect(document.querySelector('[data-test="tracking-carrier"]')).not.toBeNull();
    expect(document.querySelector('[data-test="tracking-number"]')).not.toBeNull();
  });

  it('does not ship with an incomplete tracking', async () => {
    // Act
    await mountOpenDialog();
    // Assert
    expect(buttonLabelled('Ship').disabled).toBe(true);
  });

  it('ships with the tracking once it is complete', async () => {
    // Arrange
    const wrapper = await mountOpenDialog();
    wrapper
      .findComponent(TrackingFields)
      .vm.$emit('update:modelValue', { carrier: 'UPS', trackingNumber: '1Z999' });
    await flushPromises();
    // Act
    buttonLabelled('Ship').click();
    await flushPromises();
    // Assert
    expect(wrapper.emitted('confirm')).toEqual([[{ carrier: 'UPS', trackingNumber: '1Z999' }]]);
  });

  it('ships without tracking when the administrator has none', async () => {
    // Arrange
    const wrapper = await mountOpenDialog();
    // Act
    buttonLabelled('Ship without tracking').click();
    await flushPromises();
    // Assert
    expect(wrapper.emitted('confirm')).toEqual([[NO_TRACKING]]);
  });
});
