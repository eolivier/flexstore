// The area of the page that the guided tour brings into focus
export interface FocusArea {
  top: number;
  left: number;
  width: number;
  height: number;
}

// A part that has a size and is inside the window (a closed drawer waits outside of it)
function isDisplayed(area: DOMRect): boolean {
  const isInsideWindow = area.left + area.width > 0 && area.left < window.innerWidth;
  return area.width > 0 && area.height > 0 && isInsideWindow;
}

// The first part of the page named `focus` that is displayed (the mobile and the desktop layouts may each hold one)
export function visibleFocusAreaOf(focus: string): FocusArea | null {
  const parts = Array.from(document.querySelectorAll(`[data-guided-tour="${focus}"]`));
  const areas = parts.map((part) => part.getBoundingClientRect());
  return areas.find(isDisplayed) ?? null;
}
