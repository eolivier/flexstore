import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import EditUser from './EditUser.vue';
import AccountStatusField from './AccountStatusField.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});
const jane = {
  id: 'u-1',
  firstName: 'Jane',
  lastName: 'Doe',
  email: 'jane@example.com',
  role: 'USER',
  status: 'ACTIVE',
};

async function mountEditUser() {
  const wrapper = mount(EditUser, { props: { id: 'u-1' }, global: { plugins: [vuetify, i18n, router] } });
  await flushPromises();
  return wrapper;
}

describe('EditUser.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    sessionStorage.clear();
    mockFetch.mockImplementation((url: string, init?: RequestInit) =>
      Promise.resolve({
        ok: true,
        status: 200,
        json: () => Promise.resolve(init?.method === 'PUT' || url.endsWith('/u-1') ? jane : [jane]),
      })
    );
  });

  it('shows the current first name, last name, email and role of the user', async () => {
    // Act
    const wrapper = await mountEditUser();
    // Assert
    const [firstName, lastName] = wrapper.findAll('input[type="text"]');
    expect((firstName.element as HTMLInputElement).value).toBe('Jane');
    expect((lastName.element as HTMLInputElement).value).toBe('Doe');
    expect((wrapper.find('input[type="email"]').element as HTMLInputElement).value).toBe('jane@example.com');
    expect((wrapper.find('input[type="radio"][value="USER"]').element as HTMLInputElement).checked).toBe(true);
  });

  it('saves the edited last name, email and role', async () => {
    // Arrange
    const wrapper = await mountEditUser();
    await wrapper.findAll('input[type="text"]')[1].setValue('Smith');
    await wrapper.find('input[type="radio"][value="ADMIN"]').setValue(true);
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();
    // Assert
    const [, init] = mockFetch.mock.calls.find(([, init]) => init?.method === 'PUT')!;
    expect(JSON.parse(init.body)).toEqual({
      firstName: 'Jane',
      lastName: 'Smith',
      email: 'jane@example.com',
      role: 'ADMIN',
    });
  });

  it('the cancel button goes back to the list of users', async () => {
    // Act
    const wrapper = await mountEditUser();
    // Assert
    const cancel = wrapper.findAll('a').find((link) => link.text() === 'Cancel')!;
    expect(cancel.attributes('href')).toBe('/admin/users');
  });

  const statusRequests = () =>
    mockFetch.mock.calls.filter(
      ([url, init]) => url.endsWith('/api/users/u-1/status') && init?.method === 'PUT'
    );

  it('saves the status chosen by the administrator, with its own request', async () => {
    // Arrange
    const wrapper = await mountEditUser();
    wrapper.findComponent(AccountStatusField).vm.$emit('update:modelValue', 'SUSPENDED');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();
    // Assert
    expect(statusRequests()).toHaveLength(1);
    expect(JSON.parse(statusRequests()[0][1].body)).toEqual({ status: 'SUSPENDED' });
  });

  it('does not send the status when it did not change', async () => {
    // Arrange
    const wrapper = await mountEditUser();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();
    // Assert
    expect(statusRequests()).toHaveLength(0);
  });

  it('an administrator cannot change the status of their own account', async () => {
    // Arrange
    sessionStorage.setItem(
      'flexstore.session',
      JSON.stringify({ token: 'jwt-token', expiresAt: '2030-01-01T00:00:00Z', user: { ...jane, role: 'ADMIN' } })
    );
    setActivePinia(createPinia());
    // Act
    const wrapper = await mountEditUser();
    // Assert
    expect(wrapper.findComponent(AccountStatusField).props('disabled')).toBe(true);
    expect(wrapper.text()).toContain('You cannot change the status of your own account.');
  });

  const saveButton = (wrapper: Awaited<ReturnType<typeof mountEditUser>>) =>
    wrapper.findAll('button').find((button) => button.text() === 'Save')!;

  it('the save button is enabled when only the status changes', async () => {
    // Arrange
    const wrapper = await mountEditUser();
    // Act
    wrapper.findComponent(AccountStatusField).vm.$emit('update:modelValue', 'SUSPENDED');
    await flushPromises();
    // Assert
    expect(saveButton(wrapper).attributes('disabled')).toBeUndefined();
  });

  // A one-word name was split by the migration into a first name and an empty last name
  const userWithoutLastName = () => {
    const withoutLastName = { ...jane, lastName: '' };
    mockFetch.mockImplementation((url: string, init?: RequestInit) =>
      Promise.resolve({
        ok: true,
        status: 200,
        json: () =>
          Promise.resolve(init?.method === 'PUT' || url.endsWith('/u-1') ? withoutLastName : [withoutLastName]),
      })
    );
  };

  it('a user without last name shows at once the field to fill, which keeps the save button disabled', async () => {
    // Arrange
    userWithoutLastName();
    // Act
    const wrapper = await mountEditUser();
    // Assert
    expect(wrapper.text()).toContain('Mandatory field');
    expect(saveButton(wrapper).attributes('disabled')).toBeDefined();
  });

  it('a user without last name can be saved once the last name is filled in', async () => {
    // Arrange
    userWithoutLastName();
    const wrapper = await mountEditUser();
    // Act
    await wrapper.findAll('input[type="text"]')[1].setValue('Doe');
    await flushPromises();
    // Assert
    expect(saveButton(wrapper).attributes('disabled')).toBeUndefined();
  });
});
