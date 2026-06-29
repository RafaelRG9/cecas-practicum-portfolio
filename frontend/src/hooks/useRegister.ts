import { useState, type FormEvent } from 'react'
import authService from '../services/AuthService'
import { useNavigate } from 'react-router-dom';

export function useRegister() {
  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [program, setProgram] = useState('')
  const [studentId, setStudentId] = useState('')

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()


  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()

    setError('')

    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    setLoading(true);

    try {
      await authService.register({
        fullName,
        email,
        password,
        program,
        studentId: Number(studentId),
      })

      navigate('/login', {
        state: { email },
      })

    } catch (err) {
      setError(err instanceof Error ? err.message : 'Not able to register.')
    } finally {
      setLoading(false)
    }
  }

  return {
    fullName,
    email,
    password,
    confirmPassword,
    program,
    studentId,
    loading,
    error,
    setFullName,
    setEmail,
    setPassword,
    setConfirmPassword,
    setProgram,
    setStudentId,
    handleSubmit,
  }
}