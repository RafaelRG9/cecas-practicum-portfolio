import type {
    ButtonHTMLAttributes,
    PropsWithChildren,
} from "react";

type ButtonProps =
    PropsWithChildren<ButtonHTMLAttributes<HTMLButtonElement>>;

export default function Button({
    className = "",
    children,
    ...props
}: ButtonProps) {
    return (
        <button
            {...props}
            className={`rounded-md bg-sky-700 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-sky-800 disabled:cursor-not-allowed disabled:bg-slate-400 ${className}`}
        >
            {children}
        </button>
    );
}