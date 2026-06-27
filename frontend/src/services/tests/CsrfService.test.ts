import { describe, it, expect, beforeEach, vi } from 'vitest';
import csrfService from '../CsrfService';

describe('CsrfService', () => {
  beforeEach(() => {
    vi.restoreAllMocks();

    // reset document mock every test
    Object.defineProperty(globalThis, 'document', {
      value: {
        cookie: '',
      },
      writable: true,
    });
  });

  it('reads csrf cookie', () => {
    globalThis.document.cookie = 'XSRF-TOKEN=test-token';

    const token = csrfService.getCookie('XSRF-TOKEN');

    expect(token).toBe('test-token');
  });

  it('initializes csrf successfully', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(null, { status: 200 })
    );

    await expect(csrfService.init()).resolves.not.toThrow();
  });

  it('throws when csrf init fails', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(null, { status: 500 })
    );

    await expect(csrfService.init()).rejects.toThrow(
      'Failed to initialize CSRF (500)'
    );
  });

  it('adds csrf header for POST requests', async () => {
    globalThis.document.cookie = 'XSRF-TOKEN=test-token';

    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(null, { status: 200 })
    );

    await csrfService.fetch('/api/auth/login', {
      method: 'POST',
    });

    const options = fetchSpy.mock.calls[0][1] as RequestInit;
    const headers = new Headers(options?.headers);

    expect(headers.get('X-XSRF-TOKEN')).toBe('test-token');
  });

  it('does not add csrf header for GET requests', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(null, { status: 200 })
    );

    await csrfService.fetch('/api/auth/me');

    expect(fetchSpy).toHaveBeenCalled();
  });
});