import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { defineComponent, h } from 'vue';
import { VLayout } from 'vuetify/components';
import { enableAutoUnmount, flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import MobileDrawer from './MobileDrawer.vue';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

const customer = { modelValue: true, signedIn: true, admin: false };

// The drawer of Vuetify needs the layout of the application around it
function inLayout(props: typeof customer) {
  return defineComponent({ render: () => h(VLayout, () => h(MobileDrawer, props)) });
}

async function mountDrawer(props: typeof customer) {
  const wrapper = mount(inLayout(props), {
    global: { plugins: [vuetify, i18n, router], stubs: { CatalogSearchField: true, Language: true } },
    attachTo: document.body,
  });
  await flushPromises();
  return wrapper;
}

// Each drawer is attached to the page: it is unmounted after its test
enableAutoUnmount(afterEach);

describe('MobileDrawer.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should list the pages of the shop and of the account of a customer', async () => {
    // Act
    await mountDrawer(customer);
    // Assert
    const text = document.body.textContent;
    expect(text).toContain('Home');
    expect(text).toContain('Cart');
    expect(text).toContain('My orders');
    expect(text).toContain('About');
  });

  it('should not list the account links of a visitor', async () => {
    // Act
    await mountDrawer({ ...customer, signedIn: false });
    // Assert
    expect(document.body.textContent).not.toContain('My orders');
  });

  it('should show the administration entry only to an administrator', async () => {
    // Act
    await mountDrawer({ ...customer, admin: true });
    // Assert
    expect(document.body.textContent).toContain('Administration');
  });

  it('should hide the administration entry from a customer', async () => {
    // Act
    await mountDrawer(customer);
    // Assert
    expect(document.body.textContent).not.toContain('Administration');
  });
});
