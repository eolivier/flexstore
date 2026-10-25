import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import CreateUser from './CreateUser.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:any(.*)', component: { template: '<div />' } }],
});

async function fillAndSubmit(chosenRole?: 'USER' | 'ADMIN') {
  const wrapper = mount(CreateUser, { global: { plugins: [vuetify, i18n, router] } });
  const [firstName, lastName] = wrapper.findAll('input[type="text"]');
  await firstName.setValue('Jane');
  await lastName.setValue('Doe');
  await wrapper.find('input[type="email"]').setValue('jane@example.com');
  await wrapper.find('input[type="password"]').setValue('password123');
  if (chosenRole) {
    await wrapper.find(`input[type="radio"][value="${chosenRole}"]`).setValue(true);
  }
  await flushPromises();
  await wrapper.find('form').trigger('submit.prevent');
  await flushPromises();
  return wrapper;
}

function sentUser() {
  const [, init] = mockFetch.mock.calls.find(
    ([url, init]) => url.endsWith('/api/users') && init?.method === 'POST'
  )!;
  return JSON.parse(init.body);
}

describe('CreateUser.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    mockFetch.mockResolvedValue({
      ok: true,
      status: 201,
      json: () => Promise.resolve([]),
      text: () => Promise.resolve(''),
    });
  });

  it.each([
    { chosenRole: undefined, expectedRole: 'USER' },
    { chosenRole: 'USER' as const, expectedRole: 'USER' },
    { chosenRole: 'ADMIN' as const, expectedRole: 'ADMIN' },
  ])(
    'creates the user with the role $expectedRole (role chosen: $chosenRole)',
    async ({ chosenRole, expectedRole }) => {
      // Act
      await fillAndSubmit(chosenRole);
      // Assert
      expect(sentUser()).toEqual({
        firstName: 'Jane',
        lastName: 'Doe',
        email: 'jane@example.com',
        password: 'password123',
        role: expectedRole,
      });
    }
  );

  it('the cancel button goes back to the list of users', () => {
    // Act
    const wrapper = mount(CreateUser, { global: { plugins: [vuetify, i18n, router] } });
    // Assert
    const cancel = wrapper.findAll('a').find((link) => link.text() === 'Cancel')!;
    expect(cancel.attributes('href')).toBe('/admin/users');
  });
});
