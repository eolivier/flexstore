import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import OrderTrackingCard from './OrderTrackingCard.vue';
import type { JsonOrderTracking } from '@/generated';
import { vuetify, i18n } from '@/test/setup';

const mountCard = (tracking: JsonOrderTracking) =>
  mount(OrderTrackingCard, { props: { tracking }, global: { plugins: [vuetify, i18n] } });

describe('OrderTrackingCard.vue', () => {
  it('shows the carrier and the tracking number', () => {
    // Act
    const wrapper = mountCard({ carrier: 'COLISSIMO', trackingNumber: '6A123', trackingUrl: null });
    // Assert
    expect(wrapper.text()).toContain('Colissimo');
    expect(wrapper.text()).toContain('6A123');
  });

  it('links to the tracking page of the carrier when it has one', () => {
    // Act
    const wrapper = mountCard({
      carrier: 'UPS',
      trackingNumber: '1Z999',
      trackingUrl: 'https://www.ups.com/track',
    });
    // Assert
    const link = wrapper.find('a');
    expect(link.attributes('href')).toBe('https://www.ups.com/track');
    expect(link.text()).toContain('Follow the parcel');
  });

  it('offers no link for a carrier without a tracking page', () => {
    // Act
    const wrapper = mountCard({ carrier: 'OTHER', trackingNumber: 'AB-1', trackingUrl: null });
    // Assert
    expect(wrapper.find('a').exists()).toBe(false);
    expect(wrapper.text()).toContain('Other carrier');
  });
});
