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
import cropCycleApi from '../../api/crop-cycle.js'

const headerSx = { display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }
const pageSx = { mt: 4 }
const alertSx = { mb: 2 }

export default function CropCycleListPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [rows, setRows] = useState([])
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  const loadRows = async () => {
    setLoading(true)
    setError(null)
    try {
      setRows(await cropCycleApi.list())
    } catch {
      setError(t('cropCycle.list.loadError'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadRows()
  }, [])

  const handleDelete = async (id) => {
    await cropCycleApi.remove(id)
    await loadRows()
  }

  return (
    <Container maxWidth="lg" sx={pageSx}>
      <Box sx={headerSx}>
        <Typography variant="h5">{t('cropCycle.list.title')}</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/crop-cycle/new')}>
          {t('cropCycle.list.create')}
        </Button>
      </Box>

      {error && <Alert severity="error" sx={alertSx}>{error}</Alert>}

      <Paper>
        <Table size="small">
          <TableHead>
            <TableRow>

              <TableCell>{t('cropCycle.list.fields.id')}</TableCell>

              <TableCell>{t('cropCycle.list.fields.cropName')}</TableCell>

              <TableCell>{t('cropCycle.list.fields.variety')}</TableCell>

              <TableCell>{t('cropCycle.list.fields.startedAt')}</TableCell>

              <TableCell>{t('cropCycle.list.fields.expectedHarvestAt')}</TableCell>

              <TableCell>{t('cropCycle.list.fields.status')}</TableCell>

              <TableCell align="right">{t('cropCycle.list.actions')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((row) => (
              <TableRow key={row.id}>

                <TableCell>{String(row.id ?? '')}</TableCell>

                <TableCell>{String(row.cropName ?? '')}</TableCell>

                <TableCell>{String(row.variety ?? '')}</TableCell>

                <TableCell>{String(row.startedAt ?? '')}</TableCell>

                <TableCell>{String(row.expectedHarvestAt ?? '')}</TableCell>

                <TableCell>{String(row.status ?? '')}</TableCell>

                <TableCell align="right">
                  <IconButton aria-label={t('cropCycle.list.edit')} onClick={() => navigate(`/crop-cycle/${row.id}`)}>
                    <EditIcon />
                  </IconButton>
                  <IconButton aria-label={t('cropCycle.list.delete')} onClick={() => handleDelete(row.id)}>
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
            {!loading && rows.length === 0 && (
              <TableRow>
                <TableCell colSpan={ 7 }>{t('cropCycle.list.empty')}</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Paper>
    </Container>
  )
}
