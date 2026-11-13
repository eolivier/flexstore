import { beforeEach, describe, expect, it } from 'vitest';
import { useSnackbar } from './useSnackbar';

describe('useSnackbar', () => {
  beforeEach(() => {
    useSnackbar().close();
  });

  it('shows a success in the primary color for 3 seconds', () => {
    // Arrange
    const snackbar = useSnackbar();
    // Act
    snackbar.success('Saved');
    // Assert
    expect(snackbar.open.value).toBe(true);
    expect(snackbar.message.value).toMatchObject({ text: 'Saved', color: 'primary', timeout: 3000 });
  });

  it('shows an error in the error color for 3 seconds', () => {
    // Arrange
    const snackbar = useSnackbar();
    // Act
    snackbar.error('Not saved');
    // Assert
    expect(snackbar.message.value).toMatchObject({ text: 'Not saved', color: 'error', timeout: 3000 });
  });

  it('keeps a persistent message open until it is closed', () => {
    // Arrange
    const snackbar = useSnackbar();
    // Act
    snackbar.warning('To finish', { persistent: true });
    // Assert
    expect(snackbar.message.value).toMatchObject({ color: 'warning', timeout: -1 });
  });

  it('carries the action and the icon of the message', () => {
    // Arrange
    const snackbar = useSnackbar();
    const action = { text: 'View', to: '/cart' };
    // Act
    snackbar.success('Added', { action, icon: 'mdi-cart-check' });
    // Assert
    expect(snackbar.message.value).toMatchObject({ action, icon: 'mdi-cart-check' });
  });

  it('replaces the previous message with a new one', () => {
    // Arrange
    const snackbar = useSnackbar();
    snackbar.success('First');
    const firstId = snackbar.message.value?.id;
    // Act
    snackbar.success('Second');
    // Assert
    expect(snackbar.message.value?.text).toBe('Second');
    expect(snackbar.message.value?.id).not.toBe(firstId);
  });

  it('closes the message', () => {
    // Arrange
    const snackbar = useSnackbar();
    snackbar.success('Saved');
    // Act
    snackbar.close();
    // Assert
    expect(snackbar.open.value).toBe(false);
  });
});
