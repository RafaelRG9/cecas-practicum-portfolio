import { useEffect, useState } from 'react'

type HelloResponse = {
    message: string
}

export default function DebugPage() {
    const [message, setMessage] = useState('Loading backend status...')
    const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading')

    useEffect(() => {
        async function loadHello() {
            try {
                const response = await fetch('/api/hello')

                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}`)
                }

                const data: HelloResponse = await response.json()
                setMessage(data.message)
                setStatus('success')
            } catch (error) {
                console.error(error)
                setMessage('Could not reach backend')
                setStatus('error')
            }
        }

        loadHello()
    }, [])

    return (
        <section className="space-y-6">
            <div className="mx-auto max-w-3xl rounded-2xl bg-white p-8 shadow-sm ring-1 ring-slate-200">
                <p className="mb-3 text-sm font-medium uppercase tracking-[0.2em] text-sky-700">
                    CECAS Frontend Status
                </p>

                <h1 className="text-4xl font-semibold tracking-tight">
                    React + Vite + TailwindCSS running
                </h1>

                <p className="mt-4 text-lg text-slate-600">
                    This screen shows the frontend container is working and can reach the backend through the Vite dev proxy.
                </p>

                <div
                    className={`mt-8 rounded-xl border p-4 ${status === 'success'
                        ? 'border-emerald-200 bg-emerald-50 text-emerald-800'
                        : status === 'error'
                            ? 'border-rose-200 bg-rose-50 text-rose-800'
                            : 'border-slate-200 bg-slate-50 text-slate-700'
                        }`}
                >
                    <p className="text-sm font-medium uppercase tracking-wide">Backend Status</p>
                    <p className="mt-2 text-lg">{message}</p>
                </div>
            </div>
        </section>
    )
}
