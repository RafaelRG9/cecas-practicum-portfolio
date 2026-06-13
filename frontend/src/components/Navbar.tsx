import { NavLink } from 'react-router-dom'

const navItems = [
  { to: '/', label: 'Home', end: true },
  { to: '/login', label: 'Login' },
  { to: '/register', label: 'Register' },
  { to: '/student', label: 'Student' },
  { to: '/chair', label: 'Chair' },
  { to: '/debug', label: 'Debug' },
]

export default function Navbar() {
  return (
    <header className="border-b border-slate-200 bg-white shadow-sm">
      <div className="mx-auto flex max-w-5xl flex-col gap-4 px-6 py-6 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-sky-700">
            CECAS
          </p>
          <h1 className="text-2xl font-semibold tracking-tight">
            Canvas Extra Credit Automation System
          </h1>
        </div>

        <nav aria-label="Main navigation" className="flex flex-wrap gap-2">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                `rounded-md px-3 py-2 text-sm font-medium transition ${
                  isActive
                    ? 'bg-sky-100 text-sky-800'
                    : 'text-slate-700 hover:bg-slate-100 hover:text-slate-950'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </div>
    </header>
  )
}
