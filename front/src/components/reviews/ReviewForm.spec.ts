import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { VRating } from 'vuetify/components';
import ReviewForm from './ReviewForm.vue';
import { vuetify, i18n } from '@/test/setup';

function mountForm(props: Record<string, unknown> = {}) {
  return mount(ReviewForm, { props, global: { plugins: [vuetify, i18n] } });
}

describe('ReviewForm.vue', () => {
  it('cannot be published without a rating and a comment', () => {
    // Act
    const wrapper = mountForm();
    // Assert
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined();
  });

  it('sends the rating and the trimmed comment', async () => {
    // Arrange
    const wrapper = mountForm();
    wrapper.findComponent(VRating).vm.$emit('update:modelValue', 4);
    await wrapper.find('textarea').setValue('  Sturdy  ');
    // Act
    await wrapper.find('form').trigger('submit');
    // Assert
    expect(wrapper.emitted('submit')).toEqual([[{ rating: 4, comment: 'Sturdy' }]]);
  });

  it.each([
    { outcome: 'notBought', expected: 'Only a customer who bought and paid for this product can review it.' },
    { outcome: 'alreadyReviewed', expected: 'You already reviewed this product.' },
  ])('tells why a review is refused ($outcome)', ({ outcome, expected }) => {
    // Act
    const wrapper = mountForm({ outcome });
    // Assert
    expect(wrapper.text()).toContain(expected);
  });
});
