import { createTheme } from '@mui/material'

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#6B7D2C',
      light: '#9AAE5B',
      dark: '#4B5A1F',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#C97B5A',
      light: '#E3B199',
      dark: '#9A5A3E',
      contrastText: '#ffffff',
    },
    background: {
      default: '#F2F5EC',
      paper: '#FFFFFF',
    },
    text: {
      primary: '#2F2A24',
      secondary: '#5C554D',
    },
    divider: '#E5DDD3',
    success: { main: '#4CAF50', light: '#81C784', dark: '#388E3C' },
    warning: { main: '#FF9800', light: '#FFB74D' },
    info: { main: '#0288D1', light: '#4FC3F7' },
    error: { main: '#D32F2F', light: '#EF9A9A' },
  },
  shape: { borderRadius: 12 },
  typography: {
    fontFamily: '"Inter", "Segoe UI", "Roboto", "Arial", sans-serif',
    h3: { fontWeight: 700, letterSpacing: '-0.5px' },
    h4: { fontWeight: 700, letterSpacing: '-0.3px' },
    h5: { fontWeight: 700 },
    h6: { fontWeight: 600 },
    subtitle1: { fontWeight: 500 },
    subtitle2: { fontWeight: 600 },
    button: { textTransform: 'none', fontWeight: 600 },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: { backgroundColor: '#F2F5EC' },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          boxShadow: 'none',
          '&:hover': { boxShadow: 'none' },
        },
        sizeLarge: { padding: '11px 28px', fontSize: '1rem' },
        containedPrimary: {
          background: 'linear-gradient(135deg, #6B7D2C 0%, #4B5A1F 100%)',
          '&:hover': {
            background: 'linear-gradient(135deg, #7D9232 0%, #5A6C24 100%)',
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: { backgroundImage: 'none' },
        rounded: { borderRadius: 16 },
        elevation1: { boxShadow: '0 1px 4px rgba(0,0,0,0.06), 0 0 0 1px rgba(0,0,0,0.04)' },
        elevation2: { boxShadow: '0 2px 8px rgba(0,0,0,0.08), 0 0 0 1px rgba(0,0,0,0.04)' },
        elevation6: { boxShadow: '0 6px 24px rgba(0,0,0,0.10), 0 1px 4px rgba(0,0,0,0.06)' },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          boxShadow: '0 1px 4px rgba(0,0,0,0.06), 0 0 0 1px rgba(0,0,0,0.04)',
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          boxShadow: '0 1px 0 rgba(0,0,0,0.08)',
          backdropFilter: 'blur(8px)',
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          borderRight: '1px solid #E5DDD3',
          boxShadow: 'none',
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { borderRadius: 8, fontWeight: 500 },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          transition: 'background 0.15s, color 0.15s',
        },
      },
    },
    MuiAlert: {
      styleOverrides: {
        root: { borderRadius: 10 },
      },
    },
    MuiAvatar: {
      styleOverrides: {
        root: { fontWeight: 600 },
      },
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: { borderRadius: 8, fontSize: '0.8rem' },
      },
    },
  },
})

export default theme
