import { useEffect, useMemo, useState } from "react"
import { BookOpen, TrendingUp, AlertCircle, Users } from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import type { Book, Reader, Loan } from "@/types"
import { booksApi } from "@/api/books"
import { readersApi } from "@/api/readers"
import { loansApi } from "@/api/loans"

type TopBook = {
  title: string
  author: string
  borrowedCopies: number
}

function Stat({ title, value, icon }: { title: string; value: number; icon: React.ReactNode }) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle>{title}</CardTitle>
        <div className="text-zinc-300">{icon}</div>
      </CardHeader>
      <CardContent>
        <div className="text-4xl font-semibold tracking-tight">{value}</div>
      </CardContent>
    </Card>
  )
}

export default function DashboardPage() {
  const [books, setBooks] = useState<Book[]>([])
  const [readers, setReaders] = useState<Reader[]>([])
  const [loans, setLoans] = useState<Loan[]>([])
  const [topBooks, setTopBooks] = useState<TopBook[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    ;(async () => {
      setError(null)
      const [b, r, l, top] = await Promise.all([
        booksApi.getAll(),
        readersApi.getAll(),
        loansApi.getAll(),
        booksApi.getTopBorrowed(5),
      ])

      setBooks(b)
      setReaders(r)
      setLoans(l)
      setTopBooks(top)
    })().catch((e) => setError(String(e?.message ?? e)))
  }, [])

  const stats = useMemo(() => {
    const activeLoans = loans.filter((x) => !x.returnDate).length
    const overdue = loans.filter((x) => !x.returnDate && new Date(x.dueDate) < new Date()).length
    return {
      books: books.length,
      readers: readers.length,
      activeLoans,
      overdue,
    }
  }, [books, readers, loans])

  return (
    <div>
      <h1 className="text-4xl font-semibold tracking-tight">Dashboard</h1>
      <p className="mt-2 text-sm text-zinc-400">Przegląd systemu bibliotecznego</p>

      {error && (
        <div className="mt-6 rounded-2xl border border-red-500/20 bg-red-500/10 p-4 text-sm text-red-200">
          {error}
        </div>
      )}

      
      <div className="mt-8 grid gap-5 md:grid-cols-2 xl:grid-cols-4">
        <Stat title="Książki w zbiorach" value={stats.books} icon={<BookOpen size={18} />} />
        <Stat title="Aktywne wypożyczenia" value={stats.activeLoans} icon={<TrendingUp size={18} />} />
        <Stat title="Zaległości" value={stats.overdue} icon={<AlertCircle size={18} />} />
        <Stat title="Zarejestrowani czytelnicy" value={stats.readers} icon={<Users size={18} />} />
      </div>

      
      <div className="mt-6 grid gap-5 xl:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>🔥 Najczęściej wypożyczane książki</CardTitle>
          </CardHeader>
          <CardContent>
            <ul className="space-y-2">
              {topBooks.map((b, i) => (
                <li
                  key={`${b.title}-${i}`}
                  className="flex items-center justify-between gap-4 rounded-xl border border-white/10 bg-white/[0.03] px-3 py-2"
                >
                  <div className="min-w-0">
                    <div className="truncate text-sm font-medium">
                      {i + 1}. {b.title}
                    </div>
                    <div className="truncate text-xs text-zinc-400">{b.author}</div>
                  </div>
                  <div className="shrink-0 rounded-full border border-white/10 bg-white/5 px-2.5 py-1 text-xs text-zinc-200">
                    {b.borrowedCopies}×
                  </div>
                </li>
              ))}
              {topBooks.length === 0 && (
                <li className="text-sm text-zinc-400">Brak danych</li>
              )}
            </ul>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
