// Without the characters that look alike (0 and O, 1 and I), for a code that is read and typed by a customer
const CODE_CHARACTERS = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
const GENERATED_CODE_LENGTH = 8;

export function generateDiscountCode(randomValues: (length: number) => Uint32Array = randomValuesOf): string {
  const toCharacter = (value: number) => CODE_CHARACTERS[value % CODE_CHARACTERS.length];
  return Array.from(randomValues(GENERATED_CODE_LENGTH), toCharacter).join('');
}

function randomValuesOf(length: number): Uint32Array {
  return crypto.getRandomValues(new Uint32Array(length));
}
