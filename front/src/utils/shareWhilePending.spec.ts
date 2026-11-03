import { describe, expect, it, vi } from 'vitest';
import { shareWhilePending } from './shareWhilePending';

// An action whose answer the test decides
function pendingAction() {
  let answer!: (value: string) => void;
  let fail!: (reason: Error) => void;
  const promise = new Promise<string>((resolve, reject) => {
    answer = resolve;
    fail = reject;
  });
  const action = vi.fn(() => promise);
  return { action, answer, fail };
}

describe('shareWhilePending', () => {
  it('should run the action once for the callers who arrive while it is pending', async () => {
    // Arrange
    const { action, answer } = pendingAction();
    const shared = shareWhilePending(action);
    // Act
    const answers = Promise.all([shared(), shared(), shared()]);
    answer('renewed');
    // Assert
    expect(await answers).toEqual(['renewed', 'renewed', 'renewed']);
    expect(action).toHaveBeenCalledTimes(1);
  });

  it('should run the action again once it is over', async () => {
    // Arrange
    const action = vi.fn().mockResolvedValue('renewed');
    const shared = shareWhilePending(action);
    await shared();
    // Act
    await shared();
    // Assert
    expect(action).toHaveBeenCalledTimes(2);
  });

  it('should share a failure, then run the action again', async () => {
    // Arrange
    const { action, fail } = pendingAction();
    const shared = shareWhilePending(action);
    const failures = Promise.allSettled([shared(), shared()]);
    fail(new Error('Network error'));
    const settled = await failures;
    // Act
    const next = shared();
    // Assert
    expect(settled.map((outcome) => outcome.status)).toEqual(['rejected', 'rejected']);
    expect(action).toHaveBeenCalledTimes(2);
    next.catch(() => undefined);
  });
});
