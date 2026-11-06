import type { JsonEnvironmentVariable } from '@/generated';

export function isDefined(variable: JsonEnvironmentVariable) {
  return variable.status === 'DEFINED';
}

export function isRequiredButUndefined(variable: JsonEnvironmentVariable) {
  return variable.necessity === 'REQUIRED' && !isDefined(variable);
}

export function requiredButUndefined(variables: JsonEnvironmentVariable[]) {
  return variables.filter(isRequiredButUndefined);
}

export const ENVIRONMENT_CATEGORIES = ['EMAIL', 'PAYMENT', 'MCP', 'DATABASE', 'OTHER'] as const;
export type EnvironmentCategory = (typeof ENVIRONMENT_CATEGORIES)[number];

const CATEGORY_PREFIXES: Record<Exclude<EnvironmentCategory, 'OTHER'>, string[]> = {
  EMAIL: ['FLEXSTORE_MAIL_'],
  PAYMENT: ['FLEXSTORE_STRIPE_'],
  MCP: ['FLEXSTORE_MCP_'],
  DATABASE: ['FLEXSTORE_DATABASE_', 'FLEXSTORE_DATASOURCE_'],
};

export function categoryOf(variable: JsonEnvironmentVariable): EnvironmentCategory {
  const found = ENVIRONMENT_CATEGORIES.find(
    (category) =>
      category !== 'OTHER' && CATEGORY_PREFIXES[category].some((prefix) => variable.name.startsWith(prefix)),
  );
  return found ?? 'OTHER';
}

// The non-empty categories, in the order of ENVIRONMENT_CATEGORIES
export function groupedByCategory(variables: JsonEnvironmentVariable[]) {
  return ENVIRONMENT_CATEGORIES.map((category) => ({
    category,
    variables: variables.filter((variable) => categoryOf(variable) === category),
  })).filter((group) => group.variables.length > 0);
}
