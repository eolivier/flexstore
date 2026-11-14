import { describe, expect, it } from 'vitest';
import { useEditedForm } from './useEditedForm';

const address = { street: '1 rue de la Démo', socialNetworks: { website: 'https://flexstore.test' } };

describe('useEditedForm', () => {
  it('should not be modified at first', () => {
    // Act
    const { isModified } = useEditedForm(address);
    // Assert
    expect(isModified.value).toBe(false);
  });

  it('should be modified once a value differs, nested values included', () => {
    // Arrange
    const { editedValues, isModified } = useEditedForm(address);
    // Act
    editedValues.socialNetworks.website = 'https://flexstore.example';
    // Assert
    expect(isModified.value).toBe(true);
  });

  it('should not be modified again when the initial value is typed back', () => {
    // Arrange
    const { editedValues, isModified } = useEditedForm(address);
    editedValues.street = '2 rue de la Démo';
    // Act
    editedValues.street = address.street;
    // Assert
    expect(isModified.value).toBe(false);
  });

  it('should restore the initial values, nested values included', () => {
    // Arrange
    const { editedValues, isModified, restore } = useEditedForm(address);
    editedValues.street = '2 rue de la Démo';
    editedValues.socialNetworks.website = 'https://flexstore.example';
    // Act
    restore();
    // Assert
    expect(editedValues).toEqual(address);
    expect(isModified.value).toBe(false);
  });

  it('should take the saved values as the new initial ones', () => {
    // Arrange
    const { editedValues, isModified, markSaved, restore } = useEditedForm(address);
    editedValues.street = '2 rue de la Démo';
    // Act
    markSaved();
    // Assert
    expect(isModified.value).toBe(false);
    editedValues.street = '3 rue de la Démo';
    restore();
    expect(editedValues.street).toBe('2 rue de la Démo');
  });

  it('should give a copy of the edited values', () => {
    // Arrange
    const { editedValues, snapshot } = useEditedForm(address);
    // Act
    const copy = snapshot();
    editedValues.street = '2 rue de la Démo';
    // Assert
    expect(copy.street).toBe(address.street);
  });

  it('should not change the values it was built from', () => {
    // Arrange
    const { editedValues } = useEditedForm(address);
    // Act
    editedValues.socialNetworks.website = 'https://flexstore.example';
    // Assert
    expect(address.socialNetworks.website).toBe('https://flexstore.test');
  });
});
