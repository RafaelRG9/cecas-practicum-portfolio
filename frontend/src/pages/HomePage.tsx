import { Link } from 'react-router-dom'
import capLogo from '../assets/cap.svg'

export default function HomePage() {
  return (
    <section className="space-y-10">
      <div className="overflow-hidden rounded-3xl bg-white shadow-sm ring-1 ring-slate-200">
        <div className="grid gap-8 p-8 lg:grid-cols-[1.1fr_0.9fr] lg:items-center lg:p-10">
          <div className="space-y-6">
            <div className="flex items-center gap-3">
              <img
                src={capLogo}
                alt="CECAS logo"
                className="size-14 rounded-2xl bg-sky-50 p-2 ring-1 ring-sky-100"
              />

              <div>
                <p className="text-sm font-medium uppercase tracking-[0.2em] text-sky-700">
                  CECAS
                </p>
                <p className="text-sm text-slate-500">
                  Canvas Extra Credit Automation System
                </p>
              </div>
            </div>

            <div className="space-y-4">
              <h1 className="max-w-3xl text-4xl font-semibold tracking-tight text-slate-950 sm:text-5xl">
                Extra credit requests, organized in one place.
              </h1>

              <p className="max-w-2xl text-lg leading-8 text-slate-600">
                CECAS helps students register, log in, and submit extra credit requests while giving program chairs a more organized way to review and manage those requests.
              </p>
            </div>

            <div className="flex flex-wrap gap-3">
              <Link
                to="/register"
                className="rounded-md bg-sky-700 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-sky-800"
              >
                Register as Student
              </Link>

              <Link
                to="/login"
                className="rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
              >
                Student Login
              </Link>
            </div>

            <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-4">
              <p className="text-sm font-semibold text-slate-800">
                Program Chair Login
              </p>
              <p className="mt-1 text-sm text-slate-600">
                Program chair access will use the login process once role-based authentication is connected.
              </p>
            </div>
          </div>

          <div className="flex justify-center rounded-2xl bg-slate-100 p-8 ring-1 ring-slate-200">
            <img
              src={capLogo}
              alt="CECAS logo"
              className="max-h-72 w-full max-w-sm object-contain"
            />
          </div>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <article className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
          <h2 className="text-lg font-semibold text-slate-950">Students</h2>
          <p className="mt-2 text-sm leading-6 text-slate-600">
            Register or log in to begin the extra credit request process.
          </p>
        </article>

        <article className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
          <h2 className="text-lg font-semibold text-slate-950">Requests</h2>
          <p className="mt-2 text-sm leading-6 text-slate-600">
            Submit extra credit activity information through a structured workflow.
          </p>
        </article>

        <article className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
          <h2 className="text-lg font-semibold text-slate-950">Review</h2>
          <p className="mt-2 text-sm leading-6 text-slate-600">
            Program chairs can review and manage requests once the chair workflow is connected.
          </p>
        </article>
      </div>
    </section>
  )
}
