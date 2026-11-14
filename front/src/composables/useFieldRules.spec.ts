import { describe, expect, it } from 'vitest';
import { defineComponent } from 'vue';
import { mount } from '@vue/test-utils';
import { i18n } from '@/test/setup';
import { useFieldRules } from './useFieldRules';

function fieldRules() {
  let rules!: ReturnType<typeof useFieldRules>;
  const wrapper = defineComponent({
    setup() {
      rules = useFieldRules();
      return () => null;
    },
  });
  mount(wrapper, { global: { plugins: [i18n] } });
  return rules;
}

describe('useFieldRules', () => {
  it.each([
    { value: 'Jane', expected: true },
    { value: '   ', expected: 'Mandatory field' },
    { value: '', expected: 'Mandatory field' },
    { value: null, expected: 'Mandatory field' },
  ])('required("$value") should be $expected', ({ value, expected }) => {
    // Arrange
    const { required } = fieldRules();
    // Act
    const verdict = required(value);
    // Assert
    expect(verdict).toBe(expected);
  });

  it.each([
    { value: 'jane@flexstore.fr', expected: true },
    { value: 'jane.flexstore.fr', expected: 'Invalid email format' },
  ])('email("$value") should be $expected', ({ value, expected }) => {
    // Arrange
    const { email } = fieldRules();
    // Act
    const verdict = email(value);
    // Assert
    expect(verdict).toBe(expected);
  });

  it.each([
    { value: '12345678', expected: true },
    { value: '1234567', expected: 'Password must be at least 8 characters' },
  ])('passwordLength("$value") should be $expected', ({ value, expected }) => {
    // Arrange
    const { passwordLength } = fieldRules();
    // Act
    const verdict = passwordLength(value);
    // Assert
    expect(verdict).toBe(expected);
  });
});
