import { readFileSync } from 'fs';
import { resolve } from 'path';
import { describe, expect, it } from 'vitest';

const VUETIFY_LAYERS = 'vuetify-core, vuetify-components, vuetify-overrides, vuetify-utilities, vuetify-final';

describe('index.html', () => {
  const indexHtml = readFileSync(resolve(__dirname, '../index.html'), 'utf-8');

  it('should declare the order of the layers of Vuetify', () => {
    // Act
    const declaration = `@layer ${VUETIFY_LAYERS};`;
    // Assert
    expect(indexHtml).toContain(declaration);
  });

  it('should declare the order of the layers before any link to a style sheet', () => {
    // Act
    const declarationPosition = indexHtml.indexOf('@layer');
    const firstStyleSheetPosition = indexHtml.indexOf('rel="stylesheet"');
    // Assert
    expect(declarationPosition).toBeGreaterThan(-1);
    expect(declarationPosition).toBeLessThan(firstStyleSheetPosition);
  });
});
