import { beforeEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount, RouterLinkStub } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import LegalPageForm from './LegalPageForm.vue';
import { vuetify, i18n } from '@/test/setup';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const legalNotice = {
  id: 'legal-notice-1',
  kind: 'LEGAL_NOTICE' as const,
  text: 'Flexstore SAS',
  publishedAt: '2026-01-01T00:00:00Z',
};

function mountForm() {
  return mount(LegalPageForm, {
    props: { kind: 'legal-notice', legalPage: legalNotice },
    global: { plugins: [vuetify, i18n], stubs: { RouterLink: RouterLinkStub } },
  });
}

describe('LegalPageForm.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('should show the current text of the page', () => {
    // Act
    const wrapper = mountForm();
    // Assert
    expect((wrapper.find('textarea').element as HTMLTextAreaElement).value).toBe('Flexstore SAS');
  });

  it('should publish the edited text', async () => {
    // Arrange
    const edited = { ...legalNotice, text: 'Flexstore SAS, 1 rue de la Démo' };
    mockFetch.mockResolvedValueOnce({ ok: true, status: 200, json: () => Promise.resolve(edited) });
    const wrapper = mountForm();
    await wrapper.find('textarea').setValue(edited.text);
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit');
    await flushPromises();
    // Assert
    expect(mockFetch.mock.calls[0][0]).toContain('/api/admin/legal-pages/legal-notice');
    expect(JSON.parse(mockFetch.mock.calls[0][1].body)).toEqual({ text: edited.text });
    expect(wrapper.text()).toContain('Published: the visitors now read this version.');
  });

  it('should not publish an empty text', async () => {
    // Arrange
    const wrapper = mountForm();
    await wrapper.find('textarea').setValue('   ');
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit');
    await flushPromises();
    // Assert
    expect(mockFetch).not.toHaveBeenCalled();
    expect(wrapper.text()).toContain('Mandatory field');
  });

  it('should keep Publish enabled and Cancel disabled while nothing is modified', () => {
    // Act
    const wrapper = mountForm();
    // Assert
    expect(wrapper.find('[data-test="form-save"]').attributes('disabled')).toBeUndefined();
    expect(wrapper.find('[data-test="form-cancel"]').attributes('disabled')).toBeDefined();
  });

  it('should restore the published text when cancelled', async () => {
    // Arrange
    const wrapper = mountForm();
    await wrapper.find('textarea').setValue('Another text');
    await flushPromises();
    expect(wrapper.find('[data-test="form-cancel"]').attributes('disabled')).toBeUndefined();
    // Act
    await wrapper.find('[data-test="form-cancel"]').trigger('click');
    await flushPromises();
    // Assert
    expect((wrapper.find('textarea').element as HTMLTextAreaElement).value).toBe('Flexstore SAS');
    expect(wrapper.find('[data-test="form-cancel"]').attributes('disabled')).toBeDefined();
  });

  it('should take the published text as the new initial one', async () => {
    // Arrange
    const edited = { ...legalNotice, text: 'Flexstore SAS, 1 rue de la Démo' };
    mockFetch.mockResolvedValueOnce({ ok: true, status: 200, json: () => Promise.resolve(edited) });
    const wrapper = mountForm();
    await wrapper.find('textarea').setValue(edited.text);
    await flushPromises();
    // Act
    await wrapper.find('form').trigger('submit');
    await flushPromises();
    // Assert
    expect(wrapper.find('[data-test="form-cancel"]').attributes('disabled')).toBeDefined();
  });
});
