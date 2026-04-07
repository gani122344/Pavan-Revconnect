/**
 * Parses a date value from the backend which may be:
 * - ISO string: "2024-04-05T13:30:45"
 * - ISO string with Z: "2024-04-05T13:30:45Z"
 * - Array from Jackson LocalDateTime: [2024, 4, 5, 13, 30, 45, 123456789]
 * - Number (epoch ms)
 */
export function parseBackendDate(value: any): Date | null {
  if (!value) return null;

  // Array format from Jackson LocalDateTime serialization
  if (Array.isArray(value)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = value;
    // Jackson months are 1-indexed, JS Date months are 0-indexed
    return new Date(year, month - 1, day, hour, minute, second);
  }

  // Number (epoch ms)
  if (typeof value === 'number') {
    return new Date(value);
  }

  // String
  if (typeof value === 'string') {
    // If no timezone info, treat as local time by replacing T with space
    // to avoid browser inconsistencies
    const date = new Date(value);
    if (!isNaN(date.getTime())) return date;
    return null;
  }

  return null;
}

/**
 * Returns a human-readable relative time string from a backend date value.
 */
export function getRelativeTime(value: any): string {
  const date = parseBackendDate(value);
  if (!date) return '';

  const now = new Date();
  const seconds = Math.max(0, Math.floor((now.getTime() - date.getTime()) / 1000));

  if (seconds < 60) return 'just now';
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return minutes + 'm ago';
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return hours + 'h ago';
  const days = Math.floor(hours / 24);
  if (days < 7) return days + 'd ago';
  if (days < 30) return Math.floor(days / 7) + 'w ago';
  if (days < 365) return Math.floor(days / 30) + 'mo ago';
  return Math.floor(days / 365) + 'y ago';
}
