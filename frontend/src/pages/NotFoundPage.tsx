import { Link } from 'react-router-dom'

export default function NotFoundPage() {
    return (
        <section className="space-y-4">
        <h1 className="text-3xl font-semibold tracking-tight">404 Not Found</h1>
        <p className="text-slate-600">
            The page you are looking for does not exist. Please check the URL and try again.
        </p>
        <p>
            Go back to the <Link to="/" className="text-sky-600 hover:underline">
                home page
            </Link>.
        </p>
        </section>
    )
}
