// Without case nor accents: "vetement" finds "Vêtement"
function normalized(text: string): string {
  return text
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase();
}

// Every word searched is in the text, in any order; an empty search matches any text
export function containsEveryWord(text: string, search: string): boolean {
  const words = normalized(search).split(/\s+/).filter(Boolean);
  const normalizedText = normalized(text);
  return words.every((word) => normalizedText.includes(word));
}
