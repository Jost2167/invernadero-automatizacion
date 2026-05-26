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
import maintenanceTaskApi from '../../api/maintenance-task.js'

const headerSx = { display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }
const pageSx = { mt: 4 }
const alertSx = { mb: 2 }

export default function MaintenanceTaskListPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [rows, setRows] = useState([])
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  const loadRows = async () => {
    setLoading(true)
    setError(null)
    try {
      setRows(await maintenanceTaskApi.list())
    } catch {
      setError(t('maintenanceTask.list.loadError'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadRows()
  }, [])

  const handleDelete = async (id) => {
    await maintenanceTaskApi.remove(id)
    await loadRows()
  }

  return (
    <Container maxWidth="lg" sx={pageSx}>
      <Box sx={headerSx}>
        <Typography variant="h5">{t('maintenanceTask.list.title')}</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/maintenance-task/new')}>
          {t('maintenanceTask.list.create')}
        </Button>
      </Box>

      {error && <Alert severity="error" sx={alertSx}>{error}</Alert>}

      <Paper>
        <Table size="small">
          <TableHead>
            <TableRow>

              <TableCell>{t('maintenanceTask.list.fields.id')}</TableCell>

              <TableCell>{t('maintenanceTask.list.fields.title')}</TableCell>

              <TableCell>{t('maintenanceTask.list.fields.description')}</TableCell>

              <TableCell>{t('maintenanceTask.list.fields.scheduledAt')}</TableCell>

              <TableCell>{t('maintenanceTask.list.fields.completedAt')}</TableCell>

              <TableCell>{t('maintenanceTask.list.fields.status')}</TableCell>

              <TableCell align="right">{t('maintenanceTask.list.actions')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((row) => (
              <TableRow key={row.id}>

                <TableCell>{String(row.id ?? '')}</TableCell>

                <TableCell>{String(row.title ?? '')}</TableCell>

                <TableCell>{String(row.description ?? '')}</TableCell>

                <TableCell>{String(row.scheduledAt ?? '')}</TableCell>

                <TableCell>{String(row.completedAt ?? '')}</TableCell>

                <TableCell>{String(row.status ?? '')}</TableCell>

                <TableCell align="right">
                  <IconButton aria-label={t('maintenanceTask.list.edit')} onClick={() => navigate(`/maintenance-task/${row.id}`)}>
                    <EditIcon />
                  </IconButton>
                  <IconButton aria-label={t('maintenanceTask.list.delete')} onClick={() => handleDelete(row.id)}>
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
            {!loading && rows.length === 0 && (
              <TableRow>
                <TableCell colSpan={ 7 }>{t('maintenanceTask.list.empty')}</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Paper>
    </Container>
  )
}
