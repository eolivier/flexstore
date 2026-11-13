// A failure is logged the same way everywhere: "Failed to <what was attempted>", then its cause when there is one
export function logFailure(attempt: string, cause?: unknown): void {
  if (cause === undefined) {
    console.error(`Failed to ${attempt}`);
  } else {
    console.error(`Failed to ${attempt}`, cause);
  }
}
