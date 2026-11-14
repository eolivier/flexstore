import { afterEach, describe, expect, it } from 'vitest';
import { enableAutoUnmount, flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import AccountMenu from './AccountMenu.vue';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

const visitor = { signedIn: false, admin: false, fullName: '', email: '' };
const customer = { signedIn: true, admin: false, fullName: 'Jane Doe', email: 'jane@example.com' };
const administrator = { ...customer, admin: true };

async function openMenu(props: typeof visitor) {
  const wrapper = mount(AccountMenu, { props, global: { plugins: [vuetify, i18n, router] }, attachTo: document.body });
  await wrapper.find('button').trigger('click');
  await flushPromises();
  return wrapper;
}

// Each menu is attached to the page, for its overlay: it is unmounted after its test
enableAutoUnmount(afterEach);

describe('AccountMenu.vue', () => {
  it('should offer a visitor to sign in or to create an account', async () => {
    // Act
    await openMenu(visitor);
    // Assert
    expect(document.body.textContent).toContain('Sign in');
    expect(document.body.textContent).toContain('Create an account');
    expect(document.body.textContent).not.toContain('Sign out');
  });

  it('should show the name, the email and the account links of a signed-in customer', async () => {
    // Act
    await openMenu(customer);
    // Assert
    expect(document.body.textContent).toContain('Jane Doe');
    expect(document.body.textContent).toContain('jane@example.com');
    expect(document.body.textContent).toContain('My orders');
    expect(document.body.textContent).not.toContain('Administration');
  });

  it('should add the administration links for an administrator', async () => {
    // Act
    await openMenu(administrator);
    // Assert
    expect(document.body.textContent).toContain('Administration');
    expect(document.body.textContent).toContain('Discount codes');
  });

  it('should tell that the customer signs out', async () => {
    // Arrange
    const wrapper = await openMenu(customer);
    const entries = [...document.body.querySelectorAll('.v-list-item')];
    const signOut = entries.find((entry) => entry.textContent?.includes('Sign out'));
    // Act
    signOut?.dispatchEvent(new MouseEvent('click', { bubbles: true }));
    await flushPromises();
    // Assert
    expect(wrapper.emitted('logout')).toHaveLength(1);
  });
});
