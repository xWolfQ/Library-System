import * as React from "react"
import { cn } from "@/components/cn"

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(({ className, ...props }, ref) => (
  <input
    ref={ref}
    className={cn(
      "h-10 w-full rounded-xl border border-white/10 bg-zinc-950/40 px-3 text-sm text-zinc-100 placeholder:text-zinc-500 shadow-soft outline-none focus:border-white/20 focus:ring-2 focus:ring-white/10",
      className
    )}
    {...props}
  />
))
Input.displayName = "Input"
