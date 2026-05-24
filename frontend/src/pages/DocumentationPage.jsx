import { Box, Button, Card, CardContent, Divider, Typography } from '@mui/material'
import DownloadIcon from '@mui/icons-material/Download'
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf'
import AccountTreeIcon from '@mui/icons-material/AccountTree'
import OpenInNewIcon from '@mui/icons-material/OpenInNew'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'

export default function DocumentationPage() {
  const { t } = useTranslation()

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
          <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
            <PictureAsPdfIcon sx={{ fontSize: 40, color: 'error.main', flexShrink: 0 }} />
            <Box sx={{ flexGrow: 1 }}>
              <Typography variant="subtitle1" fontWeight={600}>
                {t('docs.pdf.title')}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {t('docs.pdf.description')}
              </Typography>
            </Box>
            <Button
              variant="contained"
              startIcon={<DownloadIcon />}
              component="a"
              href="/docs/examples-export.pdf"
              download="examples-export.pdf"
              sx={{ flexShrink: 0 }}
            >
              {t('docs.download')}
            </Button>
          </CardContent>
        </Card>

        <Divider />

        <Card variant="outlined">
          <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
            <AccountTreeIcon sx={{ fontSize: 40, color: 'primary.main', flexShrink: 0 }} />
            <Box sx={{ flexGrow: 1 }}>
              <Typography variant="subtitle1" fontWeight={600}>
                {t('docs.er.title')}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {t('docs.er.description')}
              </Typography>
            </Box>
            <Button
              variant="contained"
              startIcon={<OpenInNewIcon />}
              component={Link}
              to="/docs/er-diagram"
              sx={{ flexShrink: 0 }}
            >
              {t('docs.er.action')}
            </Button>
          </CardContent>
        </Card>
      </Box>
    </Box>
  )
}
