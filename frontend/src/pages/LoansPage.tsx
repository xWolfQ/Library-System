import { useEffect, useMemo, useState } from "react"
import { loansApi } from "@/api/loans"
import { booksApi } from "@/api/books"
import { readersApi } from "@/api/readers"
import { LoanView } from "@/types/loanView"
import { PageHeader } from "@/pages/_pageHeader"
import { Card } from "@/components/ui/card"
import { Table, THead, TR, TH, TBody, TD } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"


const formatDatePL = (value?: string) => {
  if (!value) return "-"

  try {
    // usuń ewentualną strefę i milisekundy
    const cleaned = value.replace("T", " ").split(".")[0]

    const [datePart, timePart] = cleaned.split(" ")

    if (!datePart) return "-"

    const [year, month, day] = datePart.split("-")
    if (!year || !month || !day) return value

    if (timePart) {
      const [hour, minute] = timePart.split(":")
      return `${day}-${month}-${year} ${hour}:${minute}`
    }

    return `${day}-${month}-${year}`
  } catch {
    // ABSOLUTNIE NIC NIE WYWRACA STRONY
    return value
  }
}

export default function LoansPage() {
  const [items, setItems] = useState<LoanView[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [query, setQuery] = useState("")

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const active = await loansApi.getAll()

      let history: any[] = []
      try {
        history = await loansApi.getHistory()
      } catch {
        history = []
      }

      const books = await booksApi.getAll()
      const readers = await readersApi.getAll()

      const bookMap = new Map(books.map((b: any) => [b.id, b.title]))
      const readerMap = new Map(
        readers.map((r: any) => [r.id, `${r.firstName} ${r.lastName}`])
      )

      const activeMapped: LoanView[] = (active || []).map((l: any) => ({
        id: l.id,
        bookTitle: bookMap.get(l.bookId) ?? "",
        readerName: readerMap.get(l.readerId) ?? "",
        loanDate: l.loanDate,
        dueDate: l.dueDate,
        status: "ACTIVE",
      }))

      const historyMapped: LoanView[] = (history || []).map((h: any) => ({
        id: h.id,
        bookTitle: h.bookTitle,
        readerName: h.readerName,
        loanDate: h.loanDate,
        dueDate: h.dueDate,
        returnDate: h.returnDate,
        status: "RETURNED",
      }))

      setItems(
        [...activeMapped, ...historyMapped].sort(
          (a, b) =>
            new Date(b.loanDate).getTime() -
            new Date(a.loanDate).getTime()
        )
      )
    } catch (e: any) {
      setError("Nie udało się załadować wypożyczeń")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const onReturn = async (id: number) => {
    try {
      setLoading(true)
      await loansApi.returnLoan(id)
      await load()
    } catch {
      setError("Nie udało się zwrócić wypożyczenia")
    } finally {
      setLoading(false)
    }
  }

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase()
    if (!q) return items
    return items.filter((l) =>
      [
        l.bookTitle,
        l.readerName,
        formatDateTimePL(l.loanDate),
        formatDateTimePL(l.dueDate),
        l.status === "RETURNED" ? "zwrócone" : "aktywne",
      ].some((x) => x.toLowerCase().includes(q))
    )
  }, [items, query])

  const statusBadge = (r: LoanView) => {
    if (r.status === "RETURNED") {
      return <Badge variant="success">Zwrócone</Badge>
    }
    const overdue = new Date(r.dueDate) < new Date()
    return overdue ? (
      <Badge variant="destructive">Zaległe</Badge>
    ) : (
      <Badge variant="secondary">Aktywne</Badge>
    )
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Wypożyczenia"
        subtitle="Aktywne oraz zwrócone wypożyczenia"
      />

      {error && (
        <div className="rounded-2xl border border-red-500/20 bg-red-500/10 p-4 text-sm text-red-200">
          {error}
        </div>
      )}

      <Card className="p-4">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="text-sm text-zinc-400">
            {loading ? "Ładowanie..." : `Pozycje: ${filtered.length}`}
          </div>
          <div className="w-full sm:w-80">
            <Input
              placeholder="Szukaj (książka, czytelnik, data...)"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
          </div>
        </div>

        <div className="mt-4 overflow-x-auto rounded-2xl border border-white/10">
          <Table>
            <THead>
              <TR>
                <TH>Książka</TH>
                <TH>Czytelnik</TH>
                <TH>Data wypożyczenia</TH>
                <TH>Termin zwrotu</TH>
                <TH>Status</TH>
                <TH className="text-right">Akcja</TH>
              </TR>
            </THead>

            <TBody>
              {filtered.map((r) => (
                <TR key={`${r.status}-${r.id}`}>
                  <TD className="font-medium">{r.bookTitle}</TD>
                  <TD className="text-zinc-300">{r.readerName}</TD>
                  <TD className="text-zinc-400">
                    {formatDatePL(r.loanDate)}
                  </TD>
                  <TD className="text-zinc-400">
                    {formatDatePL(r.dueDate)}
                  </TD>
                  <TD>{statusBadge(r)}</TD>
                  <TD className="text-right">
                    {r.status === "ACTIVE" ? (
                      <Button
                        size="sm"
                        disabled={loading}
                        onClick={() => onReturn(r.id)}
                      >
                        Zwróć
                      </Button>
                    ) : (
                      <span className="text-zinc-500">—</span>
                    )}
                  </TD>
                </TR>
              ))}

              {!loading && filtered.length === 0 && (
                <TR>
                  <TD colSpan={6} className="p-8 text-center text-zinc-400">
                    Brak wypożyczeń
                  </TD>
                </TR>
              )}
            </TBody>
          </Table>
        </div>
      </Card>
    </div>
  )
}
