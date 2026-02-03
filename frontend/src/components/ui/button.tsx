import * as React from "react"
import { cn } from "@/components/cn"

type Variant = "default" | "secondary" | "ghost" | "danger"

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
}

const variants: Record<Variant, string> = {
  default: "bg-white/10 hover:bg-white/15 border border-white/10",
  secondary: "bg-zinc-900/60 hover:bg-zinc-900 border border-white/10",
  ghost: "hover:bg-white/10",
  danger: "bg-red-500/15 hover:bg-red-500/25 border border-red-500/20"
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = "default", ...props }, ref) => (
    <button
      ref={ref}
      className={cn(
        "inline-flex items-center justify-center gap-2 rounded-xl px-3 py-2 text-sm font-medium text-zinc-50 transition focus:outline-none focus:ring-2 focus:ring-white/20 disabled:opacity-50 disabled:pointer-events-none",
        variants[variant],
        className
      )}
      {...props}
    />
  )
)
Button.displayName = "Button"
