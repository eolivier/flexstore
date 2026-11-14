import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import GuidedTourConfetti from './GuidedTourConfetti.vue';
import { confettiColors } from '@/utils/guidedTour';

describe('GuidedTourConfetti.vue', () => {
  it('should show pieces of every color', () => {
    // Act
    const wrapper = mount(GuidedTourConfetti);
    // Assert
    const pieces = wrapper.findAll<HTMLElement>('.piece');
    const colors = new Set(pieces.map((piece) => piece.element.style.background));
    expect(pieces.length).toBeGreaterThan(confettiColors.length);
    expect(colors.size).toBe(confettiColors.length);
  });
});
