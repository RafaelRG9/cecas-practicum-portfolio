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
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setSuccess(false)

    const normalizedEmail = email.trim()

    if (!normalizedEmail) {
      setError('Email is required.')
      return
    }

    if (!password) {
      setError('Password is required.')
      return
    }

    setLoading(true)

    try {
      await authService.login({
        email: normalizedEmail,
        password,
      })

      setSuccess(true)
      await new Promise((resolve) => setTimeout(resolve, 500))

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
    loading,
    error,
    success,
    setEmail,
    setPassword,
    handleSubmit,
  }
}