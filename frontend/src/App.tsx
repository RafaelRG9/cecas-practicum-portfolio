import { Route, Routes } from 'react-router-dom'
import AppLayout from './app/AppLayout'
import ChairPage from './pages/ChairPage'
import DebugPage from './pages/DebugPage'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import NotFoundPage from './pages/NotFoundPage'
import RegisterPage from './pages/RegisterPage'
import StudentPage from './pages/StudentPage'
import CsrfInitializer from './components/app/CsrfInitializer'
export default function App() {
  return (
    <>
      <CsrfInitializer />
        <Routes>
          <Route path="/" element={<AppLayout />}>
          <Route index element={<HomePage />} />
          <Route path="login" element={<LoginPage />} />
          <Route path="register" element={<RegisterPage />} />
          <Route path="student" element={<StudentPage />} />
          <Route path="chair" element={<ChairPage />} />
          <Route path="debug" element={<DebugPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </>
  )
}
