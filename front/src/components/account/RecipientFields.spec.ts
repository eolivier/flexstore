import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import RecipientFields from './RecipientFields.vue';
import type { Recipient } from '@/models/account';
import { vuetify, i18n } from '@/test/setup';

const janeDoe: Recipient = {
  recipientType: 'PRIVATE_INDIVIDUAL',
  firstName: 'Jane',
  lastName: 'Doe',
  companyName: '',
  vatNumber: '',
};
const acme: Recipient = { ...janeDoe, recipientType: 'LEGAL_ENTITY', companyName: 'Acme' };

function mountFields(recipient: Recipient, purpose: 'delivery' | 'billing') {
  return mount(RecipientFields, {
    props: { modelValue: recipient, 'onUpdate:modelValue': () => {}, purpose },
    global: { plugins: [vuetify, i18n] },
  });
}

describe('RecipientFields.vue', () => {
  it('a person is entered by their first name and last name', () => {
    // Act
    const wrapper = mountFields({ ...janeDoe }, 'delivery');
    // Assert
    expect(wrapper.text()).toContain('First name');
    expect(wrapper.text()).not.toContain('Company name');
  });

  it('ticking "I am a company" makes the recipient a company', async () => {
    // Arrange
    const recipient = { ...janeDoe };
    const wrapper = mountFields(recipient, 'delivery');
    // Act
    await wrapper.find('input[type="checkbox"]').setValue(true);
    // Assert
    expect(recipient.recipientType).toBe('LEGAL_ENTITY');
    expect(wrapper.text()).toContain('Company name');
    expect(wrapper.text()).toContain('VAT number (optional)');
  });

  it.each([
    { purpose: 'delivery' as const, namesAsked: true },
    { purpose: 'billing' as const, namesAsked: false },
  ])(
    'the names of the contact of a company are asked for a $purpose: $namesAsked',
    ({ purpose, namesAsked }) => {
      // Act
      const wrapper = mountFields({ ...acme }, purpose);
      // Assert
      expect(wrapper.text().includes('First name')).toBe(namesAsked);
    }
  );
});
