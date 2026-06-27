import type {
  ChangePasswordRequest,
  CurrentUserResponse,
  LoginRequest,
  RegisterRequest,
} from '../types/auth.types';
import csrfService from './CsrfService';

class AuthService {
  private readonly AUTH_BASE = '/api/auth';
  private readonly USER_BASE = '/api/users';

  /**
   * Register new user
   */
  async register(payload: RegisterRequest): Promise<CurrentUserResponse> {
    await csrfService.init();

    const res = await csrfService.fetch(`${this.AUTH_BASE}/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      throw new Error(`Register failed (${res.status})`);
    }

    return (await res.json()) as CurrentUserResponse;
  }

  /**
   * Login user
   */
  async login(payload: LoginRequest): Promise<CurrentUserResponse> {
    await csrfService.init();

    const res = await csrfService.fetch(`${this.AUTH_BASE}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      throw new Error(`Login failed (${res.status})`);
    }

    return (await res.json()) as CurrentUserResponse;
  }

  /**
   * Get current session user
   */
  async fetchCurrentUser(): Promise<CurrentUserResponse> {
    const res = await csrfService.fetch(`${this.AUTH_BASE}/me`);

    if (!res.ok) {
      throw new Error(`Fetch current user failed (${res.status})`);
    }

    return (await res.json()) as CurrentUserResponse;
  }

  /**
   * Logout user
   */
  async logout(): Promise<void> {
    await csrfService.init();

    const res = await csrfService.fetch(`${this.AUTH_BASE}/logout`, {
      method: 'POST',
    });

    if (!res.ok) {
      throw new Error(`Logout failed (${res.status})`);
    }
  }

  /**
   * Change password
   */
async changePassword(payload: ChangePasswordRequest): Promise<string> {
  await csrfService.init();

  const res = await csrfService.fetch(`${this.USER_BASE}/change-password`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    const msg = await res.text();
    throw new Error(`Change password failed (${res.status}): ${msg}`);
  }

  return await res.text(); 
}

/**
 * Force change password (for chair)
 */
async forceChangePassword(payload: ChangePasswordRequest): Promise<string> {
  await csrfService.init();

  const res = await csrfService.fetch(`${this.USER_BASE}/force-change-password`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    const msg = await res.text();
    throw new Error(`Force change password failed (${res.status}): ${msg}`);
  }

  return await res.text(); 
}
}

const authService = new AuthService();
export default authService;