import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useSalesDashboardStore } from './salesDashboard.store';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

const dashboard = { firstDay: '2026-10-25', lastDay: '2026-10-31', currency: '€', revenue: 40, orderCount: 1 };

describe('salesDashboard.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mockFetch.mockReset();
  });

  it('reads the sales of the last 30 days by default', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(dashboard) });
    const store = useSalesDashboardStore();
    // Act
    await store.fetchDashboard();
    // Assert
    expect(mockFetch.mock.calls[0][0]).toContain('/api/admin/sales-dashboard?days=30');
    expect(store.dashboard).toEqual(dashboard);
  });

  it('reads the sales again over the period chosen', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(dashboard) });
    const store = useSalesDashboardStore();
    // Act
    await store.fetchDashboard(7);
    // Assert
    expect(mockFetch.mock.calls[0][0]).toContain('days=7');
    expect(store.days).toBe(7);
  });

  it('tells when the sales could not be loaded', async () => {
    // Arrange
    mockFetch.mockResolvedValue({ ok: false, status: 500 });
    const store = useSalesDashboardStore();
    // Act
    await store.fetchDashboard();
    // Assert
    expect(store.loadError).toBe(true);
  });
});
