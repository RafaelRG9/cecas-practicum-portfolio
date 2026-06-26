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
            className={`rounded-md bg-blue-600 px-4 py-2 text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-slate-400 ${className}`}
        >
            {children}
        </button>
    );
}