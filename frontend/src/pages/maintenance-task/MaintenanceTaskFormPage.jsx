import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Container,

  FormControl,

  FormControlLabel,

  InputLabel,

  MenuItem,
  Paper,

  Select,

  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useTranslation } from 'react-i18next'
import maintenanceTaskApi, { listAllGreenhouse } from '../../api/maintenance-task.js'

const pageSx = { mt: 4 }
const paperSx = { p: 3 }
const titleSx = { mb: 3 }
const alertSx = { mb: 2 }
const actionsSx = { display: 'flex', gap: 1, justifyContent: 'flex-end' }
const shrinkInputLabelProps = { shrink: true }

const initialForm = {

  id: '',

  title: '',

  description: '',

  scheduledAt: '',

  completedAt: '',

  status: '',

  greenhouseId: '',

}

export default function MaintenanceTaskFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { t } = useTranslation()
  const editing = id && id !== 'new'
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState(null)

  const [greenhouseOptions, setGreenhouseOptions] = useState([])


  useEffect(() => {

    listAllGreenhouse().then((data) => setGreenhouseOptions(data))

    if (!editing) {
      return
    }
    maintenanceTaskApi.getById(id)
      .then(setForm)
      .catch(() => setError(t('maintenanceTask.form.loadError')))
  }, [editing, id, t])

  const updateField = (name, value) => {
    setForm((current) => ({ ...current, [name]: value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError(null)
    try {
      if (editing) {
        await maintenanceTaskApi.update(id, form)
      } else {
        await maintenanceTaskApi.create(form)
      }
      navigate('/maintenance-task')
    } catch {
      setError(t('maintenanceTask.form.saveError'))
    }
  }

  return (
    <Container maxWidth="md" sx={pageSx}>
      <Paper sx={paperSx}>
        <Typography variant="h5" sx={titleSx}>
          {editing ? t('maintenanceTask.form.editTitle') : t('maintenanceTask.form.createTitle')}
        </Typography>

        {error && <Alert severity="error" sx={alertSx}>{error}</Alert>}

        <Box component="form" onSubmit={handleSubmit}>
          <Stack spacing={2}>




            <TextField
              type="text"
              label={t('maintenanceTask.form.fields.title')}
              value={form.title ?? ''}
              onChange={(event) => updateField('title', event.target.value)}
              
            />





            <TextField
              type="text"
              label={t('maintenanceTask.form.fields.description')}
              value={form.description ?? ''}
              onChange={(event) => updateField('description', event.target.value)}
              
            />





            <TextField
              type="datetime-local"
              label={t('maintenanceTask.form.fields.scheduledAt')}
              value={form.scheduledAt ?? ''}
              onChange={(event) => updateField('scheduledAt', event.target.value)}
              InputLabelProps={shrinkInputLabelProps}
            />





            <TextField
              type="datetime-local"
              label={t('maintenanceTask.form.fields.completedAt')}
              value={form.completedAt ?? ''}
              onChange={(event) => updateField('completedAt', event.target.value)}
              InputLabelProps={shrinkInputLabelProps}
            />



            <TextField
              select
              label={t('maintenanceTask.form.fields.status')}
              value={form.status ?? ''}
              onChange={(event) => updateField('status', event.target.value)}
            >

              <MenuItem value="PENDING">PENDING</MenuItem>

              <MenuItem value="IN_PROGRESS">IN_PROGRESS</MenuItem>

              <MenuItem value="COMPLETED">COMPLETED</MenuItem>

              <MenuItem value="CANCELLED">CANCELLED</MenuItem>

            </TextField>





            <FormControl fullWidth>
              <InputLabel>{t('maintenanceTask.field.greenhouse')}</InputLabel>
              <Select
                value={form.greenhouseId ?? ''}
                label={t('maintenanceTask.field.greenhouse')}
                onChange={(event) => updateField('greenhouseId', event.target.value)}
              >
                <MenuItem value="">—</MenuItem>
                { greenhouseOptions.map((option) => (
                  <MenuItem key={option.id} value={option.id}>{option.name}</MenuItem>
                ))}
              </Select>
            </FormControl>

            <Box sx={actionsSx}>
              <Button onClick={() => navigate('/maintenance-task')}>{t('maintenanceTask.form.cancel')}</Button>
              <Button type="submit" variant="contained">{t('maintenanceTask.form.save')}</Button>
            </Box>
          </Stack>
        </Box>
      </Paper>
    </Container>
  )
}
