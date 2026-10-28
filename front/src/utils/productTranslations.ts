import { DEFAULT_LANGUAGE, SHOP_LANGUAGES, type ProductTexts, type ProductTranslation } from '@/models/product';

// Nothing entered yet, in any language
export function emptyTexts(): ProductTexts {
  return Object.fromEntries(SHOP_LANGUAGES.map((language) => [language, { name: '', description: '' }]));
}

// The texts of the translations of a product, empty in the languages it is not translated into
export function textsOf(translations: ProductTranslation[]): ProductTexts {
  const texts = emptyTexts();
  translations.forEach(({ language, name, description }) => (texts[language] = { name, description }));
  return texts;
}

// The translations entered: always the default one, the others when their name or description is entered (an
// empty language is not translated)
export function translationsOf(texts: ProductTexts): ProductTranslation[] {
  const translatedLanguages = SHOP_LANGUAGES.filter(
    (language) => language === DEFAULT_LANGUAGE || isEntered(texts, language)
  );
  return translatedLanguages.map((language) => {
    const { name, description } = texts[language];
    return { language, name: name.trim(), description: description.trim() };
  });
}

export function isEntered(texts: ProductTexts, language: string): boolean {
  return !!texts[language].name.trim() || !!texts[language].description.trim();
}

// A language where only the name or only the description is entered
export function isHalfEntered(texts: ProductTexts, language: string): boolean {
  return !texts[language].name.trim() !== !texts[language].description.trim();
}
