import { useMediaQuery, useTheme } from '@mui/material'
import { Link, useLocation } from 'react-router-dom'
import {
  Box,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Typography,
} from '@mui/material'
import SpaIcon from '@mui/icons-material/Spa'
import CloseIcon from '@mui/icons-material/Close'
import { useTranslation } from 'react-i18next'

export const NAV_MODULES = [
  { key: 'dashboard', path: '/', exact: true },
  { key: 'docs', path: '/docs' },
  { key: 'climate-reading', path: '/climate-reading' },
  { key: 'crop-cycle', path: '/crop-cycle' },
  { key: 'fertilization-event', path: '/fertilization-event' },
  { key: 'greenhouse', path: '/greenhouse' },
  { key: 'greenhouse-alert', path: '/greenhouse-alert' },
  { key: 'irrigation-event', path: '/irrigation-event' },
  { key: 'location', path: '/location' },
  { key: 'maintenance-task', path: '/maintenance-task' },
  { key: 'pest-inspection', path: '/pest-inspection' },
  { key: 'sensor', path: '/sensor' },
  // codegen:nav
]

export default function Sidebar({ onClose }) {
  const theme = useTheme()
  const isDesktop = useMediaQuery(theme.breakpoints.up('md'))
  const location = useLocation()
  const { t } = useTranslation()

  const isActive = (path, exact) =>
    exact ? location.pathname === path : location.pathname === path || location.pathname.startsWith(`${path}/`)

  const handleClick = () => {
    if (!isDesktop && onClose) onClose()
  }

  return (
    <Box sx={{ width: '100%', height: '100%', display: 'flex', flexDirection: 'column', bgcolor: 'common.white' }}>
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1.5,
          px: 2.5,
          py: 2.5,
          bgcolor: 'common.white',
          color: 'text.primary',
          flexShrink: 0,
        }}
      >
        <Box
          sx={{
            width: 38,
            height: 38,
            borderRadius: '10px',
            bgcolor: 'rgba(107,125,44,0.12)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0,
            color: 'primary.main',
          }}
        >
          <SpaIcon sx={{ fontSize: 22 }} />
        </Box>
        <Typography variant="h6" sx={{ fontWeight: 700, flexGrow: 1, letterSpacing: '-0.3px' }}>
          {t('app.title')}
        </Typography>
        {!isDesktop && (
          <IconButton
            size="small"
            onClick={onClose}
            sx={{ color: 'text.secondary', '&:hover': { color: 'text.primary', bgcolor: 'rgba(0,0,0,0.04)' } }}
            aria-label={t('sidebar.closeMenu')}
          >
            <CloseIcon fontSize="small" />
          </IconButton>
        )}
      </Box>

      <Box sx={{ flexGrow: 1, overflowY: 'auto', py: 1.5 }}>
        <List disablePadding>
          {NAV_MODULES.map((module) => {
            const selected = isActive(module.path, module.exact)
            return (
              <ListItemButton
                key={module.path}
                component={Link}
                to={module.path}
                selected={selected}
                onClick={handleClick}
                sx={{
                  mx: 1,
                  mb: 0.5,
                  borderRadius: 2,
                  '&.Mui-selected': {
                    bgcolor: 'rgba(107, 125, 44, 0.12)',
                    '&:hover': { bgcolor: 'rgba(107, 125, 44, 0.18)' },
                  },
                  '&:not(.Mui-selected):hover': { bgcolor: 'rgba(0,0,0,0.04)' },
                }}
              >
                <ListItemText
                  primary={t(`sidebar.${module.key}`)}
                  primaryTypographyProps={{
                    fontWeight: selected ? 600 : 500,
                    fontSize: '0.9rem',
                    color: selected ? 'primary.main' : 'text.primary',
                  }}
                />
              </ListItemButton>
            )
          })}
        </List>
      </Box>

      <Box
        sx={{
          px: 2.5,
          py: 2,
          borderTop: '1px solid',
          borderColor: 'divider',
          flexShrink: 0,
        }}
      >
        <Typography variant="caption" color="text.secondary" sx={{ opacity: 0.7 }}>
          © {new Date().getFullYear()} {t('app.title')}
        </Typography>
      </Box>
    </Box>
  )
}
