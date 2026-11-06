import { describe, expect, it } from 'vitest';
import type { JsonEnvironmentVariable } from '@/generated';
import { isRequiredButUndefined, requiredButUndefined } from './environment';

function variable(necessity: 'REQUIRED' | 'OPTIONAL', status: 'DEFINED' | 'UNDEFINED'): JsonEnvironmentVariable {
  return { name: 'FLEXSTORE_X', sensitivity: 'PLAIN', necessity, status };
}

describe('environment', () => {
  it.each([
    { necessity: 'REQUIRED' as const, status: 'UNDEFINED' as const, missing: true },
    { necessity: 'REQUIRED' as const, status: 'DEFINED' as const, missing: false },
    { necessity: 'OPTIONAL' as const, status: 'UNDEFINED' as const, missing: false },
    { necessity: 'OPTIONAL' as const, status: 'DEFINED' as const, missing: false },
  ])('a $necessity variable that is $status is missing: $missing', ({ necessity, status, missing }) => {
    // Act
    const required = isRequiredButUndefined(variable(necessity, status));
    // Assert
    expect(required).toBe(missing);
  });

  it('keeps the required variables that are undefined', () => {
    // Arrange
    const missing = variable('REQUIRED', 'UNDEFINED');
    const variables = [variable('REQUIRED', 'DEFINED'), missing, variable('OPTIONAL', 'UNDEFINED')];
    // Act
    const found = requiredButUndefined(variables);
    // Assert
    expect(found).toEqual([missing]);
  });
});
