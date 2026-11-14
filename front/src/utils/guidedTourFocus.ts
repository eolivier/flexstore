// The area of the page that the guided tour brings into focus
export interface FocusArea {
  top: number;
  left: number;
  width: number;
  height: number;
}

// The first part of the page named `focus` that is displayed (the mobile and the desktop layouts may each hold one)
export function visibleFocusAreaOf(focus: string): FocusArea | null {
  const parts = Array.from(document.querySelectorAll(`[data-guided-tour="${focus}"]`));
  const areas = parts.map((part) => part.getBoundingClientRect());
  return areas.find((area) => area.width > 0 && area.height > 0) ?? null;
}
