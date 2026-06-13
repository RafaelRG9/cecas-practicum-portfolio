import { Outlet } from 'react-router-dom'
import Navbar from '../components/Navbar'

export default function AppLayout() {
  return (
    <div className="min-h-dvh bg-slate-100 text-slate-900">
      <Navbar />

      <main className="mx-auto max-w-5xl px-6 py-10">
        <Outlet />
      </main>
    </div>
  )
}
