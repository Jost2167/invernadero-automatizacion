import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Alert,
  Box,
  Button,
  Container,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import DeleteIcon from '@mui/icons-material/Delete'
import EditIcon from '@mui/icons-material/Edit'
import { useTranslation } from 'react-i18next'
import greenhouseAlertApi from '../../api/greenhouse-alert.js'

const headerSx = { display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }
const pageSx = { mt: 4 }
const alertSx = { mb: 2 }

export default function GreenhouseAlertListPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [rows, setRows] = useState([])
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  const loadRows = async () => {
    setLoading(true)
    setError(null)
    try {
      setRows(await greenhouseAlertApi.list())
    } catch {
      setError(t('greenhouseAlert.list.loadError'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadRows()
  }, [])

  const handleDelete = async (id) => {
    await greenhouseAlertApi.remove(id)
    await loadRows()
  }

  return (
    <Container maxWidth="lg" sx={pageSx}>
      <Box sx={headerSx}>
        <Typography variant="h5">{t('greenhouseAlert.list.title')}</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/greenhouse-alert/new')}>
          {t('greenhouseAlert.list.create')}
        </Button>
      </Box>

      {error && <Alert severity="error" sx={alertSx}>{error}</Alert>}

      <Paper>
        <Table size="small">
          <TableHead>
            <TableRow>

              <TableCell>{t('greenhouseAlert.list.fields.id')}</TableCell>

              <TableCell>{t('greenhouseAlert.list.fields.title')}</TableCell>

              <TableCell>{t('greenhouseAlert.list.fields.severity')}</TableCell>

              <TableCell>{t('greenhouseAlert.list.fields.message')}</TableCell>

              <TableCell>{t('greenhouseAlert.list.fields.detectedAt')}</TableCell>

              <TableCell>{t('greenhouseAlert.list.fields.resolved')}</TableCell>

              <TableCell align="right">{t('greenhouseAlert.list.actions')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((row) => (
              <TableRow key={row.id}>

                <TableCell>{String(row.id ?? '')}</TableCell>

                <TableCell>{String(row.title ?? '')}</TableCell>

                <TableCell>{String(row.severity ?? '')}</TableCell>

                <TableCell>{String(row.message ?? '')}</TableCell>

                <TableCell>{String(row.detectedAt ?? '')}</TableCell>

                <TableCell>{String(row.resolved ?? '')}</TableCell>

                <TableCell align="right">
                  <IconButton aria-label={t('greenhouseAlert.list.edit')} onClick={() => navigate(`/greenhouse-alert/${row.id}`)}>
                    <EditIcon />
                  </IconButton>
                  <IconButton aria-label={t('greenhouseAlert.list.delete')} onClick={() => handleDelete(row.id)}>
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
            {!loading && rows.length === 0 && (
              <TableRow>
                <TableCell colSpan={ 7 }>{t('greenhouseAlert.list.empty')}</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Paper>
    </Container>
  )
}
