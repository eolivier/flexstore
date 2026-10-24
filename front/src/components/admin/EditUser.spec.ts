import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createMemoryHistory, createRouter } from 'vue-router';
import EditUser from './EditUser.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const router = createRouter({ history: createMemoryHistory(), routes: [{ path: '/:any(.*)', component: { template: '<div />' } }] });
const jane = { id: 'u-1', name: 'Jane Doe', email: 'jane@example.com', role: 'USER' };

async function mountEditUser() {
  const wrapper = mount(EditUser, { props: { id: 'u-1' }, global: { plugins: [vuetify, i18n, router] } });
  await flushPromises();
  return wrapper;
}

describe('EditUser.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
    mockFetch.mockImplementation((url: string, init?: RequestInit) =>
      Promise.resolve({ ok: true, status: 200, json: () => Promise.resolve(init?.method === 'PUT' || url.endsWith('/u-1') ? jane : [jane]) })
    );
  });

  it('shows the current name, email and role of the user', async () => {
    // Act
    const wrapper = await mountEditUser();
    // Assert
    expect((wrapper.find('input[type="text"]').element as HTMLInputElement).value).toBe('Jane Doe');
    expect((wrapper.find('input[type="email"]').element as HTMLInputElement).value).toBe('jane@example.com');
    expect((wrapper.find('input[type="radio"][value="USER"]').element as HTMLInputElement).checked).toBe(true);
  });

  it('saves the edited name, email and role', async () => {
    // Arrange
    const wrapper = await mountEditUser();
    await wrapper.find('input[type="text"]').setValue('Jane Smith');
    await wrapper.find('input[type="radio"][value="ADMIN"]').setValue(true);
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit.prevent');
    await flushPromises();
    // Assert
    const [, init] = mockFetch.mock.calls.find(([, init]) => init?.method === 'PUT')!;
    expect(JSON.parse(init.body)).toEqual({ name: 'Jane Smith', email: 'jane@example.com', role: 'ADMIN' });
  });

  it('the cancel button goes back to the list of users', async () => {
    // Act
    const wrapper = await mountEditUser();
    // Assert
    const cancel = wrapper.findAll('a').find(link => link.text() === 'Cancel')!;
    expect(cancel.attributes('href')).toBe('/admin/users');
  });
});
