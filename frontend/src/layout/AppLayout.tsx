import { NavLink, Outlet } from "react-router-dom"
import { BookOpen, ClipboardList, LayoutDashboard, Users } from "lucide-react"
import { cn } from "@/components/cn"

const navItem = "flex items-center gap-3 rounded-xl px-3 py-2 text-sm text-zinc-200 transition hover:bg-white/5"

export default function AppLayout() {
  return (
    <div className="min-h-screen bg-zinc-950 text-zinc-50">
      <div className="flex">
        <aside className="sticky top-0 h-screen w-72 border-r border-white/10 bg-zinc-950/60 backdrop-blur">
          <div className="p-6">
            <div className="text-lg font-semibold tracking-tight">System Biblioteczny</div>
          </div>

          <nav className="px-4 space-y-1">
            <NavLink to="/" end className={({ isActive }) => cn(navItem, isActive && "bg-white/10")}>
              <LayoutDashboard size={18} /> Dashboard
            </NavLink>
            <NavLink to="/books" className={({ isActive }) => cn(navItem, isActive && "bg-white/10")}>
              <BookOpen size={18} /> Książki
            </NavLink>
            <NavLink to="/readers" className={({ isActive }) => cn(navItem, isActive && "bg-white/10")}>
              <Users size={18} /> Czytelnicy
            </NavLink>
            <NavLink to="/loans" className={({ isActive }) => cn(navItem, isActive && "bg-white/10")}>
              <ClipboardList size={18} /> Wypożyczenia
            </NavLink>
          </nav>

          <div className="absolute bottom-0 w-full border-t border-white/10 p-4 text-xs text-zinc-500">
            © 2025 Biblioteka
            <div className="mt-1 text-xs text-zinc-500">Wersja 1.1.0</div>
          </div>
          
        </aside>

        <main className="flex-1 p-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
