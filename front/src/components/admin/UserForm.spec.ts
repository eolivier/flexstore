import { beforeEach, describe, expect, it } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { flushPromises, mount } from '@vue/test-utils';
import { createMemoryHistory, createRouter } from 'vue-router';
import UserForm from './UserForm.vue';
import AccountStatusField from './AccountStatusField.vue';
import AdminTwoFactorAuthenticationField from './AdminTwoFactorAuthenticationField.vue';
import { vuetify, i18n } from '@/test/setup';

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});
const jane = { firstName: 'Jane', lastName: 'Doe', email: 'jane@example.com', role: 'USER' } as const;
const account = { id: 'u-1', status: 'ACTIVE', twoFactorEnabled: true, ownAccount: false } as const;

function mountForm(props: Record<string, unknown> = {}) {
  return mount(UserForm, {
    props: { initial: jane, saving: false, ...props },
    global: { plugins: [vuetify, i18n, router] },
  });
}

describe('UserForm.vue', () => {
  beforeEach(() => setActivePinia(createPinia()));

  it('asks for a password when the user is created', () => {
    // Act
    const wrapper = mountForm();
    // Assert
    expect(wrapper.find('input[type="password"]').exists()).toBe(true);
  });

  it('does not ask for a password when the account exists', () => {
    // Act
    const wrapper = mountForm({ account });
    // Assert
    expect(wrapper.find('input[type="password"]').exists()).toBe(false);
  });

  it('shows the status and the two-factor authentication of an existing account only', () => {
    // Act
    const created = mountForm();
    const existing = mountForm({ account });
    // Assert
    expect(created.findComponent(AccountStatusField).exists()).toBe(false);
    expect(existing.findComponent(AccountStatusField).exists()).toBe(true);
    expect(existing.findComponent(AdminTwoFactorAuthenticationField).exists()).toBe(true);
  });

  it('shows the initial values', () => {
    // Act
    const wrapper = mountForm();
    // Assert
    expect((wrapper.find('input[type="email"]').element as HTMLInputElement).value).toBe('jane@example.com');
  });

  it('submits the edited values', async () => {
    // Arrange
    const wrapper = mountForm({ account });
    await wrapper.find('input[type="email"]').setValue('jane.doe@example.com');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    // Assert
    expect(wrapper.emitted('submit')![0][0]).toMatchObject({
      firstName: 'Jane',
      lastName: 'Doe',
      email: 'jane.doe@example.com',
      role: 'USER',
      status: 'ACTIVE',
    });
  });

  it('does not submit an invalid email', async () => {
    // Arrange
    const wrapper = mountForm();
    await wrapper.find('input[type="email"]').setValue('jane');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    // Assert
    expect(wrapper.emitted('submit')).toBeUndefined();
  });

  it('shows the label of the submit button and the error message', () => {
    // Act
    const wrapper = mountForm({ submitLabel: 'Create user', errorMessage: 'Email already used' });
    // Assert
    expect(wrapper.find('[data-test="form-save"]').text()).toBe('Create user');
    expect(wrapper.text()).toContain('Email already used');
  });
});
