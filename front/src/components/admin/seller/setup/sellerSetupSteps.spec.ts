import { describe, expect, it } from 'vitest';
import { isStepComplete, type SellerSetupStep } from './sellerSetupSteps';
import type { Seller } from '@/stores/seller.store';
import { noSocialNetworkUrls } from '@/utils/socialNetworks';

const flexstore: Seller = {
  name: 'Flexstore SAS',
  address: '1 rue de la Démo, 75001 Paris, France',
  siret: '123 456 789 00012',
  vatNumber: 'FR12345678901',
  phone: '01 23 45 67 89',
  email: 'contact@flexstore.fr',
  website: 'www.flexstore.fr',
  legalForm: 'SAS',
  shareCapital: '10 000',
  registryCity: 'Paris',
  socialNetworks: {
    facebook: 'https://www.facebook.com/flexstore',
    instagram: '',
    x: '',
    tiktok: '',
    youtube: '',
    twitch: 'twitch.tv/flexstore',
  },
};

describe('isStepComplete', () => {
  it.each(['identity', 'registration', 'contact', 'socialNetworks', 'summary'] as SellerSetupStep[])(
    'the %s step should be complete for a well written seller',
    (step) => {
      // Act
      const complete = isStepComplete(step, flexstore);
      // Assert
      expect(complete).toBe(true);
    }
  );

  it.each([
    { step: 'identity', field: 'name', value: '  ' },
    { step: 'identity', field: 'address', value: '' },
    { step: 'identity', field: 'legalForm', value: '' },
    { step: 'identity', field: 'shareCapital', value: 'ten thousand' },
    { step: 'registration', field: 'siret', value: '123' },
    { step: 'registration', field: 'registryCity', value: '' },
    { step: 'registration', field: 'vatNumber', value: '12345' },
    { step: 'contact', field: 'phone', value: '' },
    { step: 'contact', field: 'email', value: 'contact' },
    { step: 'contact', field: 'website', value: 'flexstore' },
  ] as { step: SellerSetupStep; field: keyof Seller; value: string }[])(
    'the $step step should be incomplete with a $field of "$value"',
    ({ step, field, value }) => {
      // Arrange
      const seller = { ...flexstore, [field]: value };
      // Act
      const complete = isStepComplete(step, seller);
      // Assert
      expect(complete).toBe(false);
    }
  );

  it('the social networks step should be complete when none is given', () => {
    // Arrange
    const seller = { ...flexstore, socialNetworks: noSocialNetworkUrls() };
    // Act
    const complete = isStepComplete('socialNetworks', seller);
    // Assert
    expect(complete).toBe(true);
  });

  it.each(['facebook', 'https://', 'face book.com'])(
    'the social networks step should be incomplete with the address "%s"',
    (address) => {
      // Arrange
      const seller = { ...flexstore, socialNetworks: { ...flexstore.socialNetworks, tiktok: address } };
      // Act
      const complete = isStepComplete('socialNetworks', seller);
      // Assert
      expect(complete).toBe(false);
    }
  );

  it('the summary should be incomplete when a field of any step is wrong', () => {
    // Arrange
    const seller = { ...flexstore, phone: '' };
    // Act
    const complete = isStepComplete('summary', seller);
    // Assert
    expect(complete).toBe(false);
  });
});
