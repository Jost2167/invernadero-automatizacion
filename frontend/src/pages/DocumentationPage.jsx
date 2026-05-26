import { Box, Button, Card, CardContent, Divider, Tooltip, Typography } from '@mui/material'
import DownloadIcon from '@mui/icons-material/Download'
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf'
import AccountTreeIcon from '@mui/icons-material/AccountTree'
import OpenInNewIcon from '@mui/icons-material/OpenInNew'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import { useEffect, useState } from 'react'
import schemas from 'virtual:er-schemas'

export default function DocumentationPage() {
  const { t } = useTranslation()
  const [pdfAvailable, setPdfAvailable] = useState(false)

  useEffect(() => {
    fetch('/docs/examples-export.pdf', { method: 'HEAD' })
      .then(r => {
        const ct = r.headers.get('content-type') ?? ''
        setPdfAvailable(r.ok && ct.includes('pdf'))
      })
      .catch(() => setPdfAvailable(false))
  }, [])

  const erAvailable = schemas.length > 0

  return (
    <Box sx={{ maxWidth: 700, mx: 'auto', py: 4, px: 2 }}>
      <Typography variant="h5" fontWeight={700} gutterBottom>
        {t('docs.title')}
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 4 }}>
        {t('docs.subtitle')}
      </Typography>

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        <Card variant="outlined">
          <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: { xs: 'wrap', sm: 'nowrap' } }}>
            <PictureAsPdfIcon sx={{ fontSize: 40, color: 'error.main', flexShrink: 0 }} />
            <Box sx={{ flex: '1 1 auto', minWidth: 0 }}>
              <Typography variant="subtitle1" fontWeight={600}>
                {t('docs.pdf.title')}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {t('docs.pdf.description')}
              </Typography>
            </Box>
            <Tooltip title={!pdfAvailable ? t('docs.pdf.notAvailable', 'PDF no disponible') : ''}>
              <span>
                <Button
                  variant="contained"
                  startIcon={<DownloadIcon />}
                  {...(pdfAvailable
                    ? { component: 'a', href: '/docs/examples-export.pdf', download: 'examples-export.pdf' }
                    : { disabled: true }
                  )}
                  sx={{ flexShrink: 0, ml: { sm: 'auto' } }}
                >
                  {t('docs.download')}
                </Button>
              </span>
            </Tooltip>
          </CardContent>
        </Card>

        <Divider />

        <Card variant="outlined">
          <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: { xs: 'wrap', sm: 'nowrap' } }}>
            <AccountTreeIcon sx={{ fontSize: 40, color: 'primary.main', flexShrink: 0 }} />
            <Box sx={{ flex: '1 1 auto', minWidth: 0 }}>
              <Typography variant="subtitle1" fontWeight={600}>
                {t('docs.er.title')}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {t('docs.er.description')}
              </Typography>
            </Box>
            <Tooltip title={!erAvailable ? t('docs.er.notAvailable', 'No hay entidades definidas') : ''}>
              <span>
                <Button
                  variant="contained"
                  startIcon={<OpenInNewIcon />}
                  component={erAvailable ? Link : 'button'}
                  to={erAvailable ? '/er-diagram' : undefined}
                  disabled={!erAvailable}
                  sx={{ flexShrink: 0, ml: { sm: 'auto' } }}
                >
                  {t('docs.er.action')}
                </Button>
              </span>
            </Tooltip>
          </CardContent>
        </Card>
      </Box>
    </Box>
  )
}
