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
