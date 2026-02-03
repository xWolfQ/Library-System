import { cn } from "@/components/cn"

type Variant = "default" | "success" | "warning" | "danger"

const map: Record<Variant, string> = {
  default: "bg-white/10 text-zinc-200 border-white/10",
  success: "bg-emerald-500/15 text-emerald-200 border-emerald-500/20",
  warning: "bg-amber-500/15 text-amber-200 border-amber-500/20",
  danger: "bg-red-500/15 text-red-200 border-red-500/20"
}

export function Badge({ variant = "default", className, ...props }: { variant?: Variant } & React.HTMLAttributes<HTMLSpanElement>) {
  return (
    <span className={cn("inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-medium", map[variant], className)} {...props} />
  )
}
