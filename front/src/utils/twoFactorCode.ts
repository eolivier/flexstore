// The code of the two-factor authentication: 6 digits, displayed in two groups of 3 ("123 456")

export const TWO_FACTOR_CODE_LENGTH = 6;

const GROUP_LENGTH = 3;

// What the API expects: only the digits typed, at most 6 (spaces and other characters dropped)
export function twoFactorCodeDigits(typedCode: string): string {
  return typedCode.replace(/\D/g, '').slice(0, TWO_FACTOR_CODE_LENGTH);
}

// What the user reads: a space between the first 3 digits and the last 3
export function formattedTwoFactorCode(typedCode: string): string {
  const digits = twoFactorCodeDigits(typedCode);
  if (digits.length <= GROUP_LENGTH) return digits;
  return `${digits.slice(0, GROUP_LENGTH)} ${digits.slice(GROUP_LENGTH)}`;
}

export function isCompleteTwoFactorCode(typedCode: string): boolean {
  return twoFactorCodeDigits(typedCode).length === TWO_FACTOR_CODE_LENGTH;
}
