import { useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { Alert, Box, Button, Divider, Paper, Typography } from '@mui/material'
import GoogleIcon from '@mui/icons-material/Google'
import SpaIcon from '@mui/icons-material/Spa'
import LockOutlinedIcon from '@mui/icons-material/LockOutlined'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../auth/AuthContext.jsx'
import { API_BASE_URL } from '../auth/constants.js'
import LanguageSelector from '../components/LanguageSelector.jsx'

export default function LoginPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { t } = useTranslation()
  const { token, loading } = useAuth()

  const errorCode = searchParams.get('error')
  const errorMessage = errorCode
    ? t(`auth.errors.${errorCode}`, { defaultValue: t('auth.errors.GENERIC_ERROR') })
    : null

  useEffect(() => {
    if (!loading && token) {
      navigate('/', { replace: true })
    }
  }, [token, loading, navigate])

  const handleGoogleLogin = () => {
    window.location.href = `${API_BASE_URL}/oauth2/authorization/google`
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        bgcolor: 'background.default',
        position: 'relative',
      }}
    >
      <Box sx={{ position: 'absolute', top: 16, right: 16, zIndex: 2 }}>
        <LanguageSelector color="default" />
      </Box>

      {/* Panel izquierdo - branding */}
      <Box
        sx={{
          flex: 1,
          display: { xs: 'none', md: 'flex' },
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          px: 6,
          background: 'linear-gradient(160deg, #3D4A19 0%, #6B7D2C 50%, #9AAE5B 100%)',
          color: 'common.white',
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        {/* Decorative shapes */}
        <Box sx={{ position: 'absolute', top: -80, left: -80, width: 300, height: 300, borderRadius: '50%', bgcolor: 'rgba(255,255,255,0.05)' }} />
        <Box sx={{ position: 'absolute', bottom: -60, right: -60, width: 240, height: 240, borderRadius: '50%', bgcolor: 'rgba(255,255,255,0.04)' }} />
        <Box sx={{ position: 'absolute', top: '40%', right: -30, width: 120, height: 120, borderRadius: '50%', bgcolor: 'rgba(255,255,255,0.06)' }} />

        <Box sx={{ maxWidth: 400, position: 'relative' }}>
          <Box
            sx={{
              width: 72,
              height: 72,
              borderRadius: '20px',
              bgcolor: 'rgba(255,255,255,0.15)',
              border: '1px solid rgba(255,255,255,0.2)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              mb: 4,
            }}
          >
            <SpaIcon sx={{ fontSize: 40 }} />
          </Box>
          <Typography variant="h3" sx={{ fontWeight: 800, mb: 2, lineHeight: 1.1 }}>
            {t('app.title')}
          </Typography>
        </Box>
      </Box>

      {/* Panel derecho - formulario */}
      <Box
        sx={{
          flex: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          px: { xs: 2, sm: 4, md: 6 },
          py: { xs: 8, md: 4 },
        }}
      >
        <Box sx={{ width: '100%', maxWidth: 420 }}>
          {/* Logo visible solo en mobile */}
          <Box
            sx={{
              display: { xs: 'flex', md: 'none' },
              alignItems: 'center',
              gap: 1.5,
              mb: 4,
              justifyContent: 'center',
            }}
          >
            <Box
              sx={{
                width: 44,
                height: 44,
                borderRadius: '12px',
                background: 'linear-gradient(135deg, #4B5A1F 0%, #6B7D2C 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <SpaIcon sx={{ color: 'white', fontSize: 24 }} />
            </Box>
            <Typography variant="h5" sx={{ fontWeight: 700, color: 'primary.dark' }}>
              {t('app.title')}
            </Typography>
          </Box>

          <Paper
            elevation={2}
            sx={{ p: { xs: 3, sm: 4 }, borderRadius: 3 }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 0.5 }}>
              <Box
                sx={{
                  width: 36,
                  height: 36,
                  borderRadius: '10px',
                  bgcolor: 'rgba(107,125,44,0.1)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <LockOutlinedIcon fontSize="small" color="primary" />
              </Box>
              <Typography variant="h5" sx={{ fontWeight: 700 }}>
                {t('auth.login.title')}
              </Typography>
            </Box>
            {errorMessage && (
              <Alert severity="error" sx={{ mb: 2.5 }}>
                {errorMessage}
              </Alert>
            )}

            <Button
              fullWidth
              variant="contained"
              size="large"
              startIcon={<GoogleIcon />}
              onClick={handleGoogleLogin}
              sx={{ py: 1.3 }}
            >
              {t('auth.login.googleButton')}
            </Button>

            <Divider sx={{ my: 3 }}>
              <Typography variant="caption" color="text.secondary">
                {t('auth.login.secureLogin')}
              </Typography>
            </Divider>

            <Typography variant="caption" color="text.secondary" align="center" display="block">
              {t('auth.login.termsNote')}
            </Typography>
          </Paper>
        </Box>
      </Box>
    </Box>
  )
}
