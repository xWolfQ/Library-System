import * as React from "react"
import { createPortal } from "react-dom"
import { cn } from "@/components/cn"
import { X } from "lucide-react"

type DialogContextValue = {
  open: boolean
  setOpen: (v: boolean) => void
}

const DialogContext = React.createContext<DialogContextValue | null>(null)

export function Dialog({ open, onOpenChange, children }: { open: boolean; onOpenChange: (v: boolean) => void; children: React.ReactNode }) {
  return <DialogContext.Provider value={{ open, setOpen: onOpenChange }}>{children}</DialogContext.Provider>
}

export function DialogTrigger({ children }: { children: React.ReactElement }) {
  const ctx = React.useContext(DialogContext)
  if (!ctx) throw new Error("DialogTrigger must be used within Dialog")
  return React.cloneElement(children, { onClick: () => ctx.setOpen(true) })
}

export function DialogContent({ title, description, children }: { title: string; description?: string; children: React.ReactNode }) {
  const ctx = React.useContext(DialogContext)
  if (!ctx) throw new Error("DialogContent must be used within Dialog")
  if (!ctx.open) return null

  return createPortal(
    <div className="fixed inset-0 z-50">
      <div className="absolute inset-0 bg-black/60" onClick={() => ctx.setOpen(false)} />
      <div className="absolute left-1/2 top-1/2 w-[min(560px,92vw)] -translate-x-1/2 -translate-y-1/2 rounded-2xl border border-white/10 bg-zinc-950 p-5 shadow-soft">
        <div className="flex items-start justify-between gap-4">
          <div>
            <div className="text-base font-semibold">{title}</div>
            {description ? <div className="mt-1 text-sm text-zinc-400">{description}</div> : null}
          </div>
          <button
            className={cn("rounded-lg p-2 hover:bg-white/10 transition")}
            onClick={() => ctx.setOpen(false)}
            aria-label="Zamknij"
          >
            <X size={18} />
          </button>
        </div>
        <div className="mt-4">{children}</div>
      </div>
    </div>,
    document.body
  )
}
