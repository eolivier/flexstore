// An action that runs once at a time: the callers who arrive while it is pending share its answer, instead of
// starting it again (a refresh token works once, so several requests rejected together refresh the session once)
export function shareWhilePending<T>(action: () => Promise<T>): () => Promise<T> {
  let pending: Promise<T> | undefined;
  return () => {
    pending ??= action().finally(() => {
      pending = undefined;
    });
    return pending;
  };
}
