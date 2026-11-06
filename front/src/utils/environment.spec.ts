import { describe, expect, it } from 'vitest';
import type { JsonEnvironmentVariable } from '@/generated';
import { categoryOf, groupedByCategory, isRequiredButUndefined, requiredButUndefined } from './environment';

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

describe('environment categories', () => {
  it.each([
    { name: 'FLEXSTORE_MAIL_HOST', category: 'EMAIL' },
    { name: 'FLEXSTORE_STRIPE_SECRET_KEY', category: 'PAYMENT' },
    { name: 'FLEXSTORE_MCP_ENABLED', category: 'MCP' },
    { name: 'FLEXSTORE_DATABASE_URL', category: 'DATABASE' },
    { name: 'FLEXSTORE_DATASOURCE_PORT', category: 'DATABASE' },
    { name: 'FLEXSTORE_JWT_SECRET', category: 'OTHER' },
  ])('puts $name in the category $category', ({ name, category }) => {
    // Act
    const found = categoryOf({ ...variable('OPTIONAL', 'DEFINED'), name });
    // Assert
    expect(found).toBe(category);
  });

  it('groups the variables in the order of the categories and leaves out the empty ones', () => {
    // Arrange
    const other = { ...variable('REQUIRED', 'DEFINED'), name: 'FLEXSTORE_FRONT_URL' };
    const mail = { ...variable('OPTIONAL', 'DEFINED'), name: 'FLEXSTORE_MAIL_FROM' };
    // Act
    const groups = groupedByCategory([other, mail]);
    // Assert
    expect(groups).toEqual([
      { category: 'EMAIL', variables: [mail] },
      { category: 'OTHER', variables: [other] },
    ]);
  });
});
