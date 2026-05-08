import {
  Avatar,
  Box,
  Chip,
  Divider,
  Paper,
  Stack,
  Typography,
} from '@mui/material'
import SpaIcon from '@mui/icons-material/Spa'
import EmailIcon from '@mui/icons-material/Email'
import BadgeIcon from '@mui/icons-material/Badge'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../auth/AuthContext.jsx'

function stringAvatar(name = '') {
  const parts = name.trim().split(' ')
  return parts.length >= 2
    ? `${parts[0][0]}${parts[1][0]}`.toUpperCase()
    : (parts[0]?.[0] ?? '?').toUpperCase()
}

export default function HomePage() {
  const { t } = useTranslation()
  const { user } = useAuth()
  const displayName = user?.name || user?.email || ''

  return (
    <Box sx={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 3 }}>
      {/* Header de bienvenida */}
      <Paper
        elevation={0}
        sx={{
          borderRadius: 3,
          background: 'linear-gradient(135deg, #4B5A1F 0%, #6B7D2C 60%, #9AAE5B 100%)',
          color: 'common.white',
          p: { xs: 3, md: 4 },
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        {/* Decorative circles */}
        <Box
          sx={{
            position: 'absolute',
            right: -40,
            top: -40,
            width: 200,
            height: 200,
            borderRadius: '50%',
            bgcolor: 'rgba(255,255,255,0.06)',
            pointerEvents: 'none',
          }}
        />
        <Box
          sx={{
            position: 'absolute',
            right: 60,
            bottom: -60,
            width: 160,
            height: 160,
            borderRadius: '50%',
            bgcolor: 'rgba(255,255,255,0.04)',
            pointerEvents: 'none',
          }}
        />

        <Box sx={{ maxWidth: 1100, mx: 'auto', width: '100%' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, position: 'relative' }}>
            <Avatar
              sx={{
                width: 56,
                height: 56,
                bgcolor: 'rgba(255,255,255,0.2)',
                fontSize: '1.25rem',
                fontWeight: 700,
                border: '2px solid rgba(255,255,255,0.3)',
              }}
            >
              {stringAvatar(displayName)}
            </Avatar>
            <Box>
              <Typography variant="h5" sx={{ fontWeight: 700, lineHeight: 1.2 }}>
                {t('home.welcome', { name: displayName })}
              </Typography>
            </Box>
          </Box>
        </Box>
      </Paper>

      <Box sx={{ maxWidth: 1100, mx: 'auto', width: '100%', display: 'flex', flexDirection: 'column', gap: 3 }}>
        {/* Perfil de usuario */}
        <Paper
          elevation={0}
          sx={{ borderRadius: 3, border: '1px solid', borderColor: 'divider', p: { xs: 2, md: 3 } }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
            <SpaIcon fontSize="small" color="primary" />
            <Typography variant="subtitle1" sx={{ fontWeight: 600, color: 'primary.main' }}>
              {t('home.profileTitle')}
            </Typography>
          </Box>

          <Divider sx={{ mb: 2 }} />

          <Stack spacing={2}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box
                sx={{
                  width: 36,
                  height: 36,
                  borderRadius: '10px',
                  bgcolor: 'rgba(107,125,44,0.08)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                }}
              >
                <EmailIcon fontSize="small" color="primary" />
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600 }}>
                  {t('home.email')}
                </Typography>
                <Typography variant="body2" sx={{ mt: 0.2 }}>
                  {user?.email}
                </Typography>
              </Box>
            </Box>

            <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
              <Box
                sx={{
                  width: 36,
                  height: 36,
                  borderRadius: '10px',
                  bgcolor: 'rgba(107,125,44,0.08)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                }}
              >
                <BadgeIcon fontSize="small" color="primary" />
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600 }}>
                  {t('home.roles')}
                </Typography>
                <Stack direction="row" spacing={0.75} sx={{ mt: 0.5, flexWrap: 'wrap', gap: 0.75 }}>
                  {user?.roles?.map((role) => (
                    <Chip
                      key={role}
                      label={role}
                      size="small"
                      sx={{
                        bgcolor: 'rgba(107,125,44,0.1)',
                        color: 'primary.dark',
                        fontWeight: 600,
                        fontSize: '0.75rem',
                      }}
                    />
                  ))}
                </Stack>
              </Box>
            </Box>
          </Stack>
        </Paper>
      </Box>
    </Box>
  )
}
