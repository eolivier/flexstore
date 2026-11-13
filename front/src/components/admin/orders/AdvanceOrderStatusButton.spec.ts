import { afterEach, describe, expect, it } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import AdvanceOrderStatusButton from './AdvanceOrderStatusButton.vue';
import type { JsonOrder } from '@/generated';
import { twoLinesOrder } from '@/components/orders/orderFixture';
import { NO_TRACKING } from '@/utils/tracking';
import { vuetify, i18n } from '@/test/setup';

function mountButton(status: JsonOrder['status']) {
  return mount(AdvanceOrderStatusButton, {
    props: { order: { ...twoLinesOrder, status }, advancing: false },
    global: { plugins: [vuetify, i18n] },
    attachTo: document.body,
  });
}

describe('AdvanceOrderStatusButton.vue', () => {
  afterEach(() => {
    document.body.innerHTML = '';
  });

  it('should move a paid order at once, without asking for a tracking', async () => {
    // Arrange
    const wrapper = mountButton('PAID');
    // Act
    await wrapper.find('button').trigger('click');
    // Assert
    expect(wrapper.emitted('advance')).toEqual([[{ ...twoLinesOrder, status: 'PAID' }, NO_TRACKING]]);
  });

  it('should ask for the tracking before it ships a processed order', async () => {
    // Arrange
    const wrapper = mountButton('PROCESSING');
    // Act
    await wrapper.find('button').trigger('click');
    await flushPromises();
    // Assert
    expect(wrapper.emitted('advance')).toBeUndefined();
    expect(document.body.textContent).toContain('Ship this order');
  });
});
