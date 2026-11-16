import { describe, expect, it } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createVuetify } from 'vuetify';
import TwoFactorCodeField from './TwoFactorCodeField.vue';

function mountField(digits = '') {
  return mount(TwoFactorCodeField, {
    props: { modelValue: digits, 'onUpdate:modelValue': (value: string) => wrapper.setProps({ modelValue: value }) },
    global: { plugins: [createVuetify()] },
  });
}

let wrapper: ReturnType<typeof mountField>;

async function type(typedCode: string) {
  const input = wrapper.find('input');
  await input.setValue(typedCode);
  await flushPromises();
}

describe('TwoFactorCodeField.vue', () => {
  it('should show a space between the first 3 digits and the last 3', async () => {
    // Arrange
    wrapper = mountField();
    // Act
    await type('123456');
    // Assert
    expect(wrapper.find('input').element.value).toBe('123 456');
  });

  it('should keep the digits only in its model', async () => {
    // Arrange
    wrapper = mountField();
    // Act
    await type('123 456');
    // Assert
    expect(wrapper.props('modelValue')).toBe('123456');
  });

  it('should erase a letter typed', async () => {
    // Arrange
    wrapper = mountField();
    // Act
    await type('12a');
    // Assert
    expect(wrapper.find('input').element.value).toBe('12');
  });

  it('should show the digits it is given in two groups', () => {
    // Act
    wrapper = mountField('654321');
    // Assert
    expect(wrapper.find('input').element.value).toBe('654 321');
  });
});
