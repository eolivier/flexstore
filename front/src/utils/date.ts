// The dates of the shop are shown the same way everywhere, in the current locale

// A day, written out: "13 November 2026"
export function formatDate(moment: string, locale: string): string {
  return new Date(moment).toLocaleDateString(locale, { day: 'numeric', month: 'long', year: 'numeric' });
}

// A moment, with its time: "13 November 2026 at 10:15"
export function formatDateTime(moment: string, locale: string): string {
  return new Date(moment).toLocaleString(locale, {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

// A day of the shop ("2026-11-13") as a short label for a chart: "Nov 13"
export function formatShortDay(day: string, locale: string): string {
  return new Date(`${day}T12:00:00`).toLocaleDateString(locale, { day: 'numeric', month: 'short' });
}
