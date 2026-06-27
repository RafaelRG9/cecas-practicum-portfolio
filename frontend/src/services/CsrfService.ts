class CsrfService {
  getCookie(name: string): string | null {
    const match = document.cookie
      .split('; ')
      .find((row) => row.startsWith(`${name}=`));

    return match ? decodeURIComponent(match.split('=')[1]) : null;
  }

  async init(): Promise<void> {
    const res = await fetch('/api/auth/csrf', {
      credentials: 'include',
    });

    if (!res.ok) {
      throw new Error(`Failed to initialize CSRF (${res.status})`);
    }
  }

  async fetch(input: RequestInfo, init: RequestInit = {}): Promise<Response> {
    const method = (init.method ?? 'GET').toUpperCase();
    const headers = new Headers(init.headers ?? {});

    if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
      const csrfToken = this.getCookie('XSRF-TOKEN');

      if (csrfToken) {
        headers.set('X-XSRF-TOKEN', csrfToken);
      }
    }

    return fetch(input, {
      ...init,
      credentials: 'include',
      headers,
    });
  }
}

const csrfService = new CsrfService();

export default csrfService;