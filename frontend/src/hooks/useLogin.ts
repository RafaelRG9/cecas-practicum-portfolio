import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import authService from '../services/AuthService'
import { useLocation } from 'react-router-dom'

export function useLogin() {
  const navigate = useNavigate()
  const location = useLocation();

  const [email, setEmail] = useState(
    () => location.state?.email ?? ''
  );
  
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')

    const normalizedEmail = email.trim()

    if (!normalizedEmail) {
      setError('Email is required.')
      return
    }

    if (!password) {
      setError('Password is required.')
      return
    }

    if (!confirmPassword) {
      setError('Confirm password is required.')
      return
    }

    if (password !== confirmPassword) {
      setError('Password and confirm password must match.')
      return
    }

    setLoading(true)

    try {
      await authService.login({
        email: normalizedEmail,
        password,
      })

      navigate('/')
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message)
      } else {
        setError('Unable to sign in.')
      }
    } finally {
      setLoading(false)
    }
  }

  return {
    email,
    password,
    confirmPassword,
    loading,
    error,
    setEmail,
    setPassword,
    setConfirmPassword,
    handleSubmit,
  }
}