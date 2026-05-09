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
import pestInspectionApi from '../../api/pest-inspection.js'

const headerSx = { display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }
const pageSx = { mt: 4 }
const alertSx = { mb: 2 }

export default function PestInspectionListPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [rows, setRows] = useState([])
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  const loadRows = async () => {
    setLoading(true)
    setError(null)
    try {
      setRows(await pestInspectionApi.list())
    } catch {
      setError(t('pestInspection.list.loadError'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadRows()
  }, [])

  const handleDelete = async (id) => {
    await pestInspectionApi.remove(id)
    await loadRows()
  }

  return (
    <Container maxWidth="lg" sx={pageSx}>
      <Box sx={headerSx}>
        <Typography variant="h5">{t('pestInspection.list.title')}</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/pest-inspection/new')}>
          {t('pestInspection.list.create')}
        </Button>
      </Box>

      {error && <Alert severity="error" sx={alertSx}>{error}</Alert>}

      <Paper>
        <Table size="small">
          <TableHead>
            <TableRow>

              <TableCell>{t('pestInspection.list.fields.id')}</TableCell>

              <TableCell>{t('pestInspection.list.fields.inspectedAt')}</TableCell>

              <TableCell>{t('pestInspection.list.fields.pestType')}</TableCell>

              <TableCell>{t('pestInspection.list.fields.severity')}</TableCell>

              <TableCell>{t('pestInspection.list.fields.affectedAreaSquareMeters')}</TableCell>

              <TableCell>{t('pestInspection.list.fields.treatmentApplied')}</TableCell>

              <TableCell align="right">{t('pestInspection.list.actions')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((row) => (
              <TableRow key={row.id}>

                <TableCell>{String(row.id ?? '')}</TableCell>

                <TableCell>{String(row.inspectedAt ?? '')}</TableCell>

                <TableCell>{String(row.pestType ?? '')}</TableCell>

                <TableCell>{String(row.severity ?? '')}</TableCell>

                <TableCell>{String(row.affectedAreaSquareMeters ?? '')}</TableCell>

                <TableCell>{String(row.treatmentApplied ?? '')}</TableCell>

                <TableCell align="right">
                  <IconButton aria-label={t('pestInspection.list.edit')} onClick={() => navigate(`/pest-inspection/${row.id}`)}>
                    <EditIcon />
                  </IconButton>
                  <IconButton aria-label={t('pestInspection.list.delete')} onClick={() => handleDelete(row.id)}>
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
            {!loading && rows.length === 0 && (
              <TableRow>
                <TableCell colSpan={ 7 }>{t('pestInspection.list.empty')}</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Paper>
    </Container>
  )
}
