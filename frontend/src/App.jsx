import { Navigate, Outlet, Route, Routes } from 'react-router-dom'
import LoginPage from './pages/LoginPage.jsx'
import AuthCallbackPage from './pages/AuthCallbackPage.jsx'
import HomePage from './pages/HomePage.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import MainLayout from './components/MainLayout.jsx'


export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/auth/callback" element={<AuthCallbackPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout><Outlet /></MainLayout>}>
          <Route path="/" element={<HomePage />} />
          // codegen:routes
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
