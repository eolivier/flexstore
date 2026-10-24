import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import OrderLinesTable from './OrderLinesTable.vue';
import { twoLinesOrder } from './orderFixture';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({ history: createMemoryHistory(), routes: [{ path: '/:any(.*)', component: { template: '<div />' } }] });

describe('OrderLinesTable.vue', () => {
  it('shows a row per line, with a link to the product, and the order total', () => {
    // Act
    const wrapper = mount(OrderLinesTable, { props: { order: twoLinesOrder }, global: { plugins: [vuetify, i18n, router] } });
    // Assert
    const rows = wrapper.findAll('tbody tr');
    expect(rows).toHaveLength(2);
    expect(rows[0].find('a').attributes('href')).toBe('/products/4');
    expect(rows[0].text()).toContain('798.00 €');
    expect(wrapper.text()).toContain('857.00 €');
  });
});
