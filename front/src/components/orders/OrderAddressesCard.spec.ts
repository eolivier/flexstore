import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import OrderAddressesCard from './OrderAddressesCard.vue';
import { twoLinesOrder } from './orderFixture';
import { vuetify, i18n } from '@/test/setup';

const home = {
  recipientType: 'PRIVATE_INDIVIDUAL' as const,
  firstName: 'Jane',
  lastName: 'Doe',
  street: '12 rue des Lilas',
  complement: null,
  postalCode: '75011',
  city: 'Paris',
  country: 'France',
};
const office = {
  recipientType: 'PRIVATE_INDIVIDUAL' as const,
  firstName: 'Jane',
  lastName: 'Doe',
  street: '3 place du Marché',
  complement: null,
  postalCode: '69001',
  city: 'Lyon',
  country: 'France',
};

function mountCard(order = twoLinesOrder) {
  return mount(OrderAddressesCard, { props: { order }, global: { plugins: [vuetify, i18n] } });
}

describe('OrderAddressesCard.vue', () => {
  it('shows the delivery address, and a billing address that is the same', () => {
    // Act
    const wrapper = mountCard({ ...twoLinesOrder, deliveryAddress: home, billingAddress: home });
    // Assert
    expect(wrapper.text()).toContain('Jane Doe, 12 rue des Lilas, 75011 Paris, France');
    expect(wrapper.text()).toContain('Same as the delivery address');
  });

  it('shows a billing address different from the delivery address', () => {
    // Act
    const wrapper = mountCard({ ...twoLinesOrder, deliveryAddress: home, billingAddress: office });
    // Assert
    expect(wrapper.text()).toContain('Jane Doe, 3 place du Marché, 69001 Lyon, France');
  });
});
