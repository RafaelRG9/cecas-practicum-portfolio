import { useEffect, useState } from 'react'
import authService from '../services/AuthService'
import type { CurrentUserResponse } from '../types/auth.types'

const anonymousUser: CurrentUserResponse = {
  authenticated: false,
  email: null,
  role: null,
}

export function useCurrentUser() {
  const [user, setUser] = useState<CurrentUserResponse>(anonymousUser)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true

    void authService.fetchCurrentUser()
      .then((response) => {
        if (active) setUser(response)
      })
      .catch(() => {
        if (active) setUser(anonymousUser)
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => {
      active = false
    }
  }, [])

  return { user, loading }
}