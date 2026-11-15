import { beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ShippingMethodForm from './ShippingMethodForm.vue';
import type { JsonShippingMethod } from '@/generated';
import { vuetify, i18n } from '@/test/setup';

vi.stubGlobal('fetch', vi.fn());

const express: JsonShippingMethod = {
  id: 'express',
  label: 'Express',
  cost: 9.9,
  freeShippingEligibility: 'NOT_ELIGIBLE',
  status: 'ENABLED',
};

function mountForm(editing: JsonShippingMethod | null) {
  return mount(ShippingMethodForm, { props: { editing }, global: { plugins: [vuetify, i18n] } });
}

describe('ShippingMethodForm.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should offer to add a method, without Cancel, when nothing is edited', () => {
    // Act
    const wrapper = mountForm(null);
    // Assert
    expect(wrapper.find('[data-test="form-save"]').text()).toBe('Add the method');
    expect(wrapper.find('[data-test="form-cancel"]').exists()).toBe(false);
  });

  it('should offer Cancel then Save, with the common labels, when a method is edited', () => {
    // Act
    const wrapper = mountForm(express);
    // Assert
    expect(wrapper.find('[data-test="form-save"]').text()).toBe('Save');
    expect(wrapper.find('[data-test="form-cancel"]').text()).toBe('Cancel');
  });

  it('should tell the page that the edition is over when Cancel is used', async () => {
    // Arrange
    const wrapper = mountForm(express);
    // Act
    await wrapper.find('[data-test="form-cancel"]').trigger('click');
    // Assert
    expect(wrapper.emitted('done')).toHaveLength(1);
  });
});
