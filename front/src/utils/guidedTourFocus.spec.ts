import { afterEach, describe, expect, it } from 'vitest';
import { visibleFocusAreaOf } from './guidedTourFocus';

function partNamed(focus: string, width: number) {
  const part = document.createElement('button');
  part.dataset.guidedTour = focus;
  part.getBoundingClientRect = () => ({ top: 5, left: 8, width, height: width > 0 ? 30 : 0 }) as DOMRect;
  document.body.appendChild(part);
}

describe('visibleFocusAreaOf', () => {
  afterEach(() => {
    document.body.innerHTML = '';
  });

  it('should find the area of the part named', () => {
    // Arrange
    partNamed('cart', 40);
    // Act
    const area = visibleFocusAreaOf('cart');
    // Assert
    expect(area).toMatchObject({ top: 5, left: 8, width: 40, height: 30 });
  });

  it('should skip a part that is not displayed', () => {
    // Arrange
    partNamed('shop', 0);
    partNamed('shop', 60);
    // Act
    const area = visibleFocusAreaOf('shop');
    // Assert
    expect(area?.width).toBe(60);
  });

  it('should find nothing when no part has that name', () => {
    // Act / Assert
    expect(visibleFocusAreaOf('account')).toBeNull();
  });
});
