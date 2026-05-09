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
import greenhouseApi from '../../api/greenhouse.js'

const headerSx = { display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }
const pageSx = { mt: 4 }
const alertSx = { mb: 2 }

export default function GreenhouseListPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [rows, setRows] = useState([])
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  const loadRows = async () => {
    setLoading(true)
    setError(null)
    try {
      setRows(await greenhouseApi.list())
    } catch {
      setError(t('greenhouse.list.loadError'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadRows()
  }, [])

  const handleDelete = async (id) => {
    await greenhouseApi.remove(id)
    await loadRows()
  }

  return (
    <Container maxWidth="lg" sx={pageSx}>
      <Box sx={headerSx}>
        <Typography variant="h5">{t('greenhouse.list.title')}</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/greenhouse/new')}>
          {t('greenhouse.list.create')}
        </Button>
      </Box>

      {error && <Alert severity="error" sx={alertSx}>{error}</Alert>}

      <Paper>
        <Table size="small">
          <TableHead>
            <TableRow>

              <TableCell>{t('greenhouse.list.fields.id')}</TableCell>

              <TableCell>{t('greenhouse.list.fields.code')}</TableCell>

              <TableCell>{t('greenhouse.list.fields.name')}</TableCell>

              <TableCell>{t('greenhouse.list.fields.areaSquareMeters')}</TableCell>

              <TableCell>{t('greenhouse.list.fields.status')}</TableCell>

              <TableCell>{t('greenhouse.list.fields.active')}</TableCell>

              <TableCell align="right">{t('greenhouse.list.actions')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((row) => (
              <TableRow key={row.id}>

                <TableCell>{String(row.id ?? '')}</TableCell>

                <TableCell>{String(row.code ?? '')}</TableCell>

                <TableCell>{String(row.name ?? '')}</TableCell>

                <TableCell>{String(row.areaSquareMeters ?? '')}</TableCell>

                <TableCell>{String(row.status ?? '')}</TableCell>

                <TableCell>{String(row.active ?? '')}</TableCell>

                <TableCell align="right">
                  <IconButton aria-label={t('greenhouse.list.edit')} onClick={() => navigate(`/greenhouse/${row.id}`)}>
                    <EditIcon />
                  </IconButton>
                  <IconButton aria-label={t('greenhouse.list.delete')} onClick={() => handleDelete(row.id)}>
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
            {!loading && rows.length === 0 && (
              <TableRow>
                <TableCell colSpan={ 7 }>{t('greenhouse.list.empty')}</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Paper>
    </Container>
  )
}
