import { useEffect } from 'react'
import CsrfService from '../../services/CsrfService'

  
export default function CsrfInitializer() {
  useEffect(() => {
    void CsrfService.init().catch((err) => {
      console.warn('CSRF init failed', err)
    })
  }, [])

  return null
}

