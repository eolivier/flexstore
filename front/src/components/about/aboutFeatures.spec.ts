import { describe, expect, it } from 'vitest';
import messages from '@/i18n/messages';
import { aboutFeatureGroups } from '@/components/about/aboutFeatures';

describe('the features presented on the About page', () => {
  for (const language of ['en', 'fr'] as const) {
    it(`have a title and a text for every group and feature in ${language}`, () => {
      const features = messages[language].about.features as Record<string, Record<string, unknown>>;

      for (const group of aboutFeatureGroups) {
        expect(features[group.key].title).toBeTruthy();
        expect(features[group.key].subtitle).toBeTruthy();
        for (const feature of group.features) {
          expect(features[group.key][feature.key]).toEqual({
            title: expect.any(String),
            text: expect.any(String),
          });
        }
      }
    });
  }
});
