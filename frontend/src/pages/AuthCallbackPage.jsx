import { useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { Box, CircularProgress } from '@mui/material'
import { useAuth } from '../auth/AuthContext.jsx'

export default function AuthCallbackPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { login } = useAuth()

  useEffect(() => {
    const token = searchParams.get('token')
    const errorCode = searchParams.get('error')

    if (token) {
      login(token)
      navigate('/', { replace: true })
    } else if (errorCode) {
      navigate(`/login?error=${encodeURIComponent(errorCode)}`, { replace: true })
    } else {
      navigate('/login', { replace: true })
    }
  }, [searchParams, login, navigate])

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}>
      <CircularProgress />
    </Box>
  )
}
