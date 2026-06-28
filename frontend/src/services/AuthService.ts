import type {
  ChangePasswordRequest,
  CurrentUserResponse,
  LoginRequest,
  RegisterRequest,
} from '../types/auth.types'
import csrfService from './CsrfService'

async function readErrorMessage(res: Response, fallback: string) {
  const contentType = res.headers.get('content-type') ?? ''

  if (contentType.includes('application/json')) {
    const body = await res.json().catch(() => null)
    if (body?.detail) return body.detail
    if (body?.message) return body.message
  }

  const text = await res.text().catch(() => '')
  return text || fallback
}

class AuthService {
  private readonly AUTH_BASE = '/api/auth'
  private readonly USER_BASE = '/api/users'

  async register(payload: RegisterRequest): Promise<CurrentUserResponse> {
    await csrfService.init()

    const res = await csrfService.fetch(`${this.AUTH_BASE}/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) {
      throw new Error(`Register failed (${res.status})`)
    }

    return (await res.json()) as CurrentUserResponse
  }

  async login(payload: LoginRequest): Promise<CurrentUserResponse> {
    await csrfService.init()

    const res = await csrfService.fetch(`${this.AUTH_BASE}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) {
      const fallback =
        res.status === 401
          ? 'Invalid email or password.'
          : 'Unable to sign in right now.'

      throw new Error(await readErrorMessage(res, fallback))
    }

    return (await res.json()) as CurrentUserResponse
  }

  async fetchCurrentUser(): Promise<CurrentUserResponse> {
    const res = await csrfService.fetch(`${this.AUTH_BASE}/me`)

    if (!res.ok) {
      throw new Error(`Fetch current user failed (${res.status})`)
    }

    return (await res.json()) as CurrentUserResponse
  }

  async logout(): Promise<void> {
    await csrfService.init()

    const res = await csrfService.fetch(`${this.AUTH_BASE}/logout`, {
      method: 'POST',
    })

    if (!res.ok) {
      throw new Error(`Logout failed (${res.status})`)
    }
  }

  async changePassword(payload: ChangePasswordRequest): Promise<string> {
    await csrfService.init()

    const res = await csrfService.fetch(`${this.USER_BASE}/change-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) {
      const msg = await res.text()
      throw new Error(`Change password failed (${res.status}): ${msg}`)
    }

    return await res.text()
  }

  async forceChangePassword(payload: ChangePasswordRequest): Promise<string> {
    await csrfService.init()

    const res = await csrfService.fetch(`${this.USER_BASE}/force-change-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) {
      const msg = await res.text()
      throw new Error(`Force change password failed (${res.status}): ${msg}`)
    }

    return await res.text()
  }
}

const authService = new AuthService()
export default authService