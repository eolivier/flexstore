import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import type { JsonEnvironmentVariable } from '@/generated';
import EnvironmentVariableRow from './EnvironmentVariableRow.vue';
import { vuetify, i18n } from '@/test/setup';

function mountRow(variable: JsonEnvironmentVariable) {
  return mount(EnvironmentVariableRow, {
    props: { variable },
    global: { plugins: [vuetify, i18n] },
    attachTo: document.createElement('tbody'),
  });
}

describe('EnvironmentVariableRow.vue', () => {
  it('shows the value of a defined plain variable', () => {
    // Arrange
    const frontUrl: JsonEnvironmentVariable = {
      name: 'FLEXSTORE_FRONT_URL',
      sensitivity: 'PLAIN',
      necessity: 'REQUIRED',
      status: 'DEFINED',
      value: 'https://shop.example.com',
    };
    // Act
    const wrapper = mountRow(frontUrl);
    // Assert
    expect(wrapper.text()).toContain('FLEXSTORE_FRONT_URL');
    expect(wrapper.text()).toContain('https://shop.example.com');
  });

  it('says that a secret is defined without showing anything of it', () => {
    // Arrange
    const jwtSecret: JsonEnvironmentVariable = {
      name: 'FLEXSTORE_JWT_SECRET',
      sensitivity: 'SECRET',
      necessity: 'REQUIRED',
      status: 'DEFINED',
    };
    // Act
    const wrapper = mountRow(jwtSecret);
    // Assert
    expect(wrapper.text()).toContain('Defined');
    expect(wrapper.text()).toContain('Hidden');
  });

  it('says that a variable is not defined', () => {
    // Arrange
    const stripeKey: JsonEnvironmentVariable = {
      name: 'FLEXSTORE_STRIPE_SECRET_KEY',
      sensitivity: 'SECRET',
      necessity: 'OPTIONAL',
      status: 'UNDEFINED',
    };
    // Act
    const wrapper = mountRow(stripeKey);
    // Assert
    expect(wrapper.text()).toContain('Not defined');
  });
});
