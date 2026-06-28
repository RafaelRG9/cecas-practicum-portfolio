import { Navigate, Outlet } from 'react-router-dom'
import { useCurrentUser } from '../hooks/useCurrentUser'

type RequireRoleProps = {
  allowedRoles: Array<'STUDENT' | 'CHAIR'>
}

export default function RequireRole({ allowedRoles }: RequireRoleProps) {
  const { user, loading } = useCurrentUser()

  if (loading) {
    return <p className="text-sm text-slate-600">Loading...</p>
  }

  if (!user.authenticated) {
    return <Navigate to="/login" replace />
  }

  if (!user.role || !allowedRoles.includes(user.role as 'STUDENT' | 'CHAIR')) {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}