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
import irrigationEventApi from '../../api/irrigation-event.js'

const headerSx = { display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }
const pageSx = { mt: 4 }
const alertSx = { mb: 2 }

export default function IrrigationEventListPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [rows, setRows] = useState([])
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  const loadRows = async () => {
    setLoading(true)
    setError(null)
    try {
      setRows(await irrigationEventApi.list())
    } catch {
      setError(t('irrigationEvent.list.loadError'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadRows()
  }, [])

  const handleDelete = async (id) => {
    await irrigationEventApi.remove(id)
    await loadRows()
  }

  return (
    <Container maxWidth="lg" sx={pageSx}>
      <Box sx={headerSx}>
        <Typography variant="h5">{t('irrigationEvent.list.title')}</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/irrigation-event/new')}>
          {t('irrigationEvent.list.create')}
        </Button>
      </Box>

      {error && <Alert severity="error" sx={alertSx}>{error}</Alert>}

      <Paper>
        <Table size="small">
          <TableHead>
            <TableRow>

              <TableCell>{t('irrigationEvent.list.fields.id')}</TableCell>

              <TableCell>{t('irrigationEvent.list.fields.startedAt')}</TableCell>

              <TableCell>{t('irrigationEvent.list.fields.endedAt')}</TableCell>

              <TableCell>{t('irrigationEvent.list.fields.waterLiters')}</TableCell>

              <TableCell>{t('irrigationEvent.list.fields.method')}</TableCell>

              <TableCell>{t('irrigationEvent.list.fields.notes')}</TableCell>

              <TableCell align="right">{t('irrigationEvent.list.actions')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((row) => (
              <TableRow key={row.id}>

                <TableCell>{String(row.id ?? '')}</TableCell>

                <TableCell>{String(row.startedAt ?? '')}</TableCell>

                <TableCell>{String(row.endedAt ?? '')}</TableCell>

                <TableCell>{String(row.waterLiters ?? '')}</TableCell>

                <TableCell>{String(row.method ?? '')}</TableCell>

                <TableCell>{String(row.notes ?? '')}</TableCell>

                <TableCell align="right">
                  <IconButton aria-label={t('irrigationEvent.list.edit')} onClick={() => navigate(`/irrigation-event/${row.id}`)}>
                    <EditIcon />
                  </IconButton>
                  <IconButton aria-label={t('irrigationEvent.list.delete')} onClick={() => handleDelete(row.id)}>
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
            {!loading && rows.length === 0 && (
              <TableRow>
                <TableCell colSpan={ 7 }>{t('irrigationEvent.list.empty')}</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Paper>
    </Container>
  )
}
