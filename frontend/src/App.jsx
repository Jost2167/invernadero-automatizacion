import { Navigate, Outlet, Route, Routes } from 'react-router-dom'
import LoginPage from './pages/LoginPage.jsx'
import AuthCallbackPage from './pages/AuthCallbackPage.jsx'
import HomePage from './pages/HomePage.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import MainLayout from './components/MainLayout.jsx'
import DocumentationPage from './pages/DocumentationPage.jsx'
import ErDiagramPage from './pages/ErDiagramPage.jsx'
import ClimateReadingListPage from './pages/climate-reading/ClimateReadingListPage.jsx'
import ClimateReadingFormPage from './pages/climate-reading/ClimateReadingFormPage.jsx'
import CropCycleListPage from './pages/crop-cycle/CropCycleListPage.jsx'
import CropCycleFormPage from './pages/crop-cycle/CropCycleFormPage.jsx'
import FertilizationEventListPage from './pages/fertilization-event/FertilizationEventListPage.jsx'
import FertilizationEventFormPage from './pages/fertilization-event/FertilizationEventFormPage.jsx'
import GreenhouseListPage from './pages/greenhouse/GreenhouseListPage.jsx'
import GreenhouseFormPage from './pages/greenhouse/GreenhouseFormPage.jsx'
import GreenhouseAlertListPage from './pages/greenhouse-alert/GreenhouseAlertListPage.jsx'
import GreenhouseAlertFormPage from './pages/greenhouse-alert/GreenhouseAlertFormPage.jsx'
import IrrigationEventListPage from './pages/irrigation-event/IrrigationEventListPage.jsx'
import IrrigationEventFormPage from './pages/irrigation-event/IrrigationEventFormPage.jsx'
import LocationListPage from './pages/location/LocationListPage.jsx'
import LocationFormPage from './pages/location/LocationFormPage.jsx'
import MaintenanceTaskListPage from './pages/maintenance-task/MaintenanceTaskListPage.jsx'
import MaintenanceTaskFormPage from './pages/maintenance-task/MaintenanceTaskFormPage.jsx'
import PestInspectionListPage from './pages/pest-inspection/PestInspectionListPage.jsx'
import PestInspectionFormPage from './pages/pest-inspection/PestInspectionFormPage.jsx'
import SensorListPage from './pages/sensor/SensorListPage.jsx'
import SensorFormPage from './pages/sensor/SensorFormPage.jsx'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/auth/callback" element={<AuthCallbackPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout><Outlet /></MainLayout>}>
          <Route path="/" element={<HomePage />} />
          <Route path="/docs" element={<DocumentationPage />} />
          <Route path="/er-diagram" element={<ErDiagramPage />} />
          <Route path="/climate-reading" element={<ClimateReadingListPage />} />
          <Route path="/climate-reading/:id" element={<ClimateReadingFormPage />} />
          <Route path="/crop-cycle" element={<CropCycleListPage />} />
          <Route path="/crop-cycle/:id" element={<CropCycleFormPage />} />
          <Route path="/fertilization-event" element={<FertilizationEventListPage />} />
          <Route path="/fertilization-event/:id" element={<FertilizationEventFormPage />} />
          <Route path="/greenhouse" element={<GreenhouseListPage />} />
          <Route path="/greenhouse/:id" element={<GreenhouseFormPage />} />
          <Route path="/greenhouse-alert" element={<GreenhouseAlertListPage />} />
          <Route path="/greenhouse-alert/:id" element={<GreenhouseAlertFormPage />} />
          <Route path="/irrigation-event" element={<IrrigationEventListPage />} />
          <Route path="/irrigation-event/:id" element={<IrrigationEventFormPage />} />
          <Route path="/location" element={<LocationListPage />} />
          <Route path="/location/:id" element={<LocationFormPage />} />
          <Route path="/maintenance-task" element={<MaintenanceTaskListPage />} />
          <Route path="/maintenance-task/:id" element={<MaintenanceTaskFormPage />} />
          <Route path="/pest-inspection" element={<PestInspectionListPage />} />
          <Route path="/pest-inspection/:id" element={<PestInspectionFormPage />} />
          <Route path="/sensor" element={<SensorListPage />} />
          <Route path="/sensor/:id" element={<SensorFormPage />} />
          // codegen:routes
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
