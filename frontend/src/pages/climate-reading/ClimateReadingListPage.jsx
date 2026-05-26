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
import climateReadingApi from '../../api/climate-reading.js'

const headerSx = { display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }
const pageSx = { mt: 4 }
const alertSx = { mb: 2 }

export default function ClimateReadingListPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [rows, setRows] = useState([])
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  const loadRows = async () => {
    setLoading(true)
    setError(null)
    try {
      setRows(await climateReadingApi.list())
    } catch {
      setError(t('climateReading.list.loadError'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadRows()
  }, [])

  const handleDelete = async (id) => {
    await climateReadingApi.remove(id)
    await loadRows()
  }

  return (
    <Container maxWidth="lg" sx={pageSx}>
      <Box sx={headerSx}>
        <Typography variant="h5">{t('climateReading.list.title')}</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/climate-reading/new')}>
          {t('climateReading.list.create')}
        </Button>
      </Box>

      {error && <Alert severity="error" sx={alertSx}>{error}</Alert>}

      <Paper>
        <Table size="small">
          <TableHead>
            <TableRow>

              <TableCell>{t('climateReading.list.fields.id')}</TableCell>

              <TableCell>{t('climateReading.list.fields.recordedAt')}</TableCell>

              <TableCell>{t('climateReading.list.fields.temperatureCelsius')}</TableCell>

              <TableCell>{t('climateReading.list.fields.humidityPercent')}</TableCell>

              <TableCell>{t('climateReading.list.fields.co2Ppm')}</TableCell>

              <TableCell>{t('climateReading.list.fields.lightLux')}</TableCell>

              <TableCell align="right">{t('climateReading.list.actions')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((row) => (
              <TableRow key={row.id}>

                <TableCell>{String(row.id ?? '')}</TableCell>

                <TableCell>{String(row.recordedAt ?? '')}</TableCell>

                <TableCell>{String(row.temperatureCelsius ?? '')}</TableCell>

                <TableCell>{String(row.humidityPercent ?? '')}</TableCell>

                <TableCell>{String(row.co2Ppm ?? '')}</TableCell>

                <TableCell>{String(row.lightLux ?? '')}</TableCell>

                <TableCell align="right">
                  <IconButton aria-label={t('climateReading.list.edit')} onClick={() => navigate(`/climate-reading/${row.id}`)}>
                    <EditIcon />
                  </IconButton>
                  <IconButton aria-label={t('climateReading.list.delete')} onClick={() => handleDelete(row.id)}>
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
            {!loading && rows.length === 0 && (
              <TableRow>
                <TableCell colSpan={ 7 }>{t('climateReading.list.empty')}</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Paper>
    </Container>
  )
}
