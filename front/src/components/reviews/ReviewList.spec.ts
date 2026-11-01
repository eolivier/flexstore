import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import ReviewList from './ReviewList.vue';
import { vuetify, i18n } from '@/test/setup';

const review = {
  id: 'r-1',
  productId: 'lamp',
  reviewerName: 'Marie D.',
  rating: 5,
  comment: 'Sturdy',
  reviewedAt: '2026-10-31T10:00:00Z',
};

function mountList(deletable: boolean) {
  return mount(ReviewList, { props: { reviews: [review], deletable }, global: { plugins: [vuetify, i18n] } });
}

describe('ReviewList.vue', () => {
  it('offers no deletion to a customer', () => {
    // Act
    const wrapper = mountList(false);
    // Assert
    expect(wrapper.find('[data-test="delete-review"]').exists()).toBe(false);
  });

  it('lets an administrator ask for the deletion of a review', async () => {
    // Arrange
    const wrapper = mountList(true);
    // Act
    await wrapper.get('[data-test="delete-review"]').trigger('click');
    // Assert
    expect(wrapper.emitted('delete')).toEqual([[review]]);
  });
});
