import { useEffect, useMemo, useState } from "react"
import { Pencil, Trash2, Plus, Handshake } from "lucide-react"
import { booksApi } from "@/api/books"
import { readersApi } from "@/api/readers"
import { loansApi } from "@/api/loans"
import type { Book } from "@/types"
import { PageHeader } from "@/pages/_pageHeader"
import { Card } from "@/components/ui/card"
import { Table, TBody, TD, TH, THead, TR } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Dialog, DialogContent, DialogTrigger } from "@/components/ui/dialog"
import { Select } from "@/components/ui/select"

type BookForm = Omit<Book, "id">

const empty: BookForm = {
  title: "",
  author: "",
  ISBN: "",
  publishedYear: undefined,
  category: "",
  availableCopies: 0
}

type ReaderSelect = { id: number; label: string }

function toReaderSelect(x: any) {
  return {
    id: Number(x.id),
    label: x.fullName ?? String(x.id),
  }
}

function numberOrUndefined(v: string) {
  const t = v.trim()
  if (!t) return undefined
  const n = Number(t)
  return Number.isFinite(n) ? n : undefined
}

function defaultDueDateISO() {
  const d = new Date()
  d.setDate(d.getDate() + 14)
  return d.toISOString().slice(0, 10)
}

export default function BooksPage() {
  const [items, setItems] = useState<Book[]>([])
  const [readers, setReaders] = useState<ReaderSelect[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [query, setQuery] = useState("")
  const [createOpen, setCreateOpen] = useState(false)
  const [editOpen, setEditOpen] = useState(false)

  const [createForm, setCreateForm] = useState<BookForm>(empty)
  const [editId, setEditId] = useState<number | null>(null)
  const [editForm, setEditForm] = useState<BookForm>(empty)

  
  const [loanOpen, setLoanOpen] = useState(false)
  const [loanBook, setLoanBook] = useState<Book | null>(null)
  const [loanReaderId, setLoanReaderId] = useState<number | "">("")
  const [loanDueDate, setLoanDueDate] = useState<string>(defaultDueDateISO())

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const [books, rSel] = await Promise.all([booksApi.getAll(), readersApi.getSelect()])
      setItems(books)
      setReaders((rSel ?? []).map(toReaderSelect))
    } catch (e: any) {
      setError(String(e?.message ?? e))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase()
    if (!q) return items
    return items.filter((b) =>
      [b.title, b.author, b.ISBN ?? "", b.category ?? ""].some((x) => x.toLowerCase().includes(q))
    )
  }, [items, query])

  const onCreate = async () => {
    if (!createForm.title.trim() || !createForm.author.trim()) {
      setError("Tytuł i autor są wymagane.")
      return
    }
    setError(null)
    try {
      await booksApi.create(createForm)
      setCreateForm(empty)
      setCreateOpen(false)
      await load()
    } catch (e: any) {
      setError(String(e?.message ?? e))
    }
  }

  const openEdit = (b: Book) => {
    setEditId(b.id)
    setEditForm({
      title: b.title ?? "",
      author: b.author ?? "",
      ISBN: b.ISBN ?? "",
      publishedYear: b.publishedYear,
      category: b.category ?? "",
      availableCopies: b.availableCopies ?? 0
    })
    setEditOpen(true)
  }

  const onSave = async () => {
    if (editId == null) return
    if (!editForm.title.trim() || !editForm.author.trim()) {
      setError("Tytuł i autor są wymagane.")
      return
    }
    setError(null)
    try {
      await booksApi.update(editId, editForm)
      setEditOpen(false)
      setEditId(null)
      await load()
    } catch (e: any) {
      setError(String(e?.message ?? e))
    }
  }

  const onDelete = async (id: number) => {
    if (!confirm("Usunąć książkę?")) return
    setError(null)
    try {
      await booksApi.remove(id)
      await load()
    } catch (e: any) {
      setError(String(e?.message ?? e))
    }
  }

  const openLoan = (b: Book) => {
    setLoanBook(b)
    setLoanReaderId("")
    setLoanDueDate(defaultDueDateISO())
    setLoanOpen(true)
  }

  const submitLoan = async () => {
    if (!loanBook) return
    if (loanReaderId === "" || !loanDueDate) {
      setError("Wybierz czytelnika i termin zwrotu.")
      return
    }
    setError(null)
    try {
      await loansApi.create({ bookId: loanBook.id, readerId: Number(loanReaderId), dueDate: loanDueDate })
      setLoanOpen(false)
      setLoanBook(null)
      await load()
    } catch (e: any) {
      setError(String(e?.message ?? e))
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Książki"
        subtitle="Zarządzaj katalogiem książek"
        right={
          <Dialog open={createOpen} onOpenChange={setCreateOpen}>
            <DialogTrigger>
              <Button>
                <Plus size={16} /> Dodaj książkę
              </Button>
            </DialogTrigger>
            <DialogContent title="Dodaj książkę" description="Wprowadź dane książki i zapisz.">
              <div className="grid gap-3 md:grid-cols-2">
                <Input placeholder="Tytuł" value={createForm.title} onChange={(e) => setCreateForm((f) => ({ ...f, title: e.target.value }))} />
                <Input placeholder="Autor" value={createForm.author} onChange={(e) => setCreateForm((f) => ({ ...f, author: e.target.value }))} />
                <Input placeholder="ISBN" value={createForm.ISBN ?? ""} onChange={(e) => setCreateForm((f) => ({ ...f, ISBN: e.target.value }))} />
                <Input placeholder="Rok wydania" inputMode="numeric" value={createForm.publishedYear ?? ""} onChange={(e) => setCreateForm((f) => ({ ...f, publishedYear: numberOrUndefined(e.target.value) }))} />
                <Input placeholder="Kategoria" value={createForm.category ?? ""} onChange={(e) => setCreateForm((f) => ({ ...f, category: e.target.value }))} />
                <Input placeholder="Dostępne sztuki" inputMode="numeric" value={createForm.availableCopies ?? 0} onChange={(e) => setCreateForm((f) => ({ ...f, availableCopies: Number(e.target.value || 0) }))} />
              </div>

              <div className="mt-5 flex justify-end gap-2">
                <Button variant="ghost" onClick={() => setCreateOpen(false)}>Anuluj</Button>
                <Button onClick={onCreate}>Zapisz</Button>
              </div>
            </DialogContent>
          </Dialog>
        }
      />

      {error ? (
        <div className="rounded-2xl border border-red-500/20 bg-red-500/10 p-4 text-sm text-red-200">{error}</div>
      ) : null}

      <Card className="p-4">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="text-sm text-zinc-400">{loading ? "Ładowanie..." : `Pozycje: ${filtered.length}`}</div>
          <div className="w-full sm:w-80">
            <Input placeholder="Szukaj (tytuł, autor, ISBN...)" value={query} onChange={(e) => setQuery(e.target.value)} />
          </div>
        </div>

        <div className="mt-4 overflow-x-auto rounded-2xl border border-white/10">
          <Table>
            <THead>
              <TR>
                <TH>Tytuł</TH>
                <TH>Autor</TH>
                <TH>ISBN</TH>
                <TH>Rok</TH>
                <TH>Kategoria</TH>
                <TH>Sztuki</TH>
                <TH className="text-right">Akcje</TH>
              </TR>
            </THead>
            <TBody>
              {filtered.map((b) => (
                <TR key={b.id}>
                  <TD className="font-medium">{b.title}</TD>
                  <TD className="text-zinc-300">{b.author}</TD>
                  <TD className="text-zinc-400">{b.ISBN ?? "-"}</TD>
                  <TD className="text-zinc-400">{b.publishedYear ?? "-"}</TD>
                  <TD className="text-zinc-400">{b.category ?? "-"}</TD>
                  <TD className="text-zinc-300">{b.availableCopies ?? 0}</TD>
                  <TD className="text-right">
                    <div className="inline-flex gap-2">
                      <Button
                        variant="secondary"
                        onClick={() => openLoan(b)}
                        disabled={(b.availableCopies ?? 0) <= 0}
                        title={(b.availableCopies ?? 0) <= 0 ? "Brak dostępnych egzemplarzy" : "Wypożycz"}
                      >
                        <Handshake size={16} /> Wypożycz
                      </Button>
                      <Button variant="secondary" onClick={() => openEdit(b)} title="Edytuj">
                        <Pencil size={16} />
                      </Button>
                      <Button variant="danger" onClick={() => onDelete(b.id)} title="Usuń">
                        <Trash2 size={16} />
                      </Button>
                    </div>
                  </TD>
                </TR>
              ))}
              {!loading && filtered.length === 0 ? (
                <TR>
                  <TD colSpan={7} className="p-8 text-center text-zinc-400">Brak danych</TD>
                </TR>
              ) : null}
            </TBody>
          </Table>
        </div>
      </Card>

      <Dialog open={editOpen} onOpenChange={setEditOpen}>
        <DialogContent title="Edytuj książkę" description="Zmień dane i zapisz.">
          <div className="grid gap-3 md:grid-cols-2">
            <Input placeholder="Tytuł" value={editForm.title} onChange={(e) => setEditForm((f) => ({ ...f, title: e.target.value }))} />
            <Input placeholder="Autor" value={editForm.author} onChange={(e) => setEditForm((f) => ({ ...f, author: e.target.value }))} />
            <Input placeholder="ISBN" value={editForm.ISBN ?? ""} onChange={(e) => setEditForm((f) => ({ ...f, ISBN: e.target.value }))} />
            <Input placeholder="Rok wydania" inputMode="numeric" value={editForm.publishedYear ?? ""} onChange={(e) => setEditForm((f) => ({ ...f, publishedYear: numberOrUndefined(e.target.value) }))} />
            <Input placeholder="Kategoria" value={editForm.category ?? ""} onChange={(e) => setEditForm((f) => ({ ...f, category: e.target.value }))} />
            <Input placeholder="Dostępne sztuki" inputMode="numeric" value={editForm.availableCopies ?? 0} onChange={(e) => setEditForm((f) => ({ ...f, availableCopies: Number(e.target.value || 0) }))} />
          </div>

          <div className="mt-5 flex justify-end gap-2">
            <Button variant="ghost" onClick={() => setEditOpen(false)}>Anuluj</Button>
            <Button onClick={onSave}>Zapisz</Button>
          </div>
        </DialogContent>
      </Dialog>

      <Dialog open={loanOpen} onOpenChange={setLoanOpen}>
        <DialogContent title="Wypożycz książkę" description="Wybierz czytelnika i zatwierdź wypożyczenie.">
          <div className="rounded-xl border border-white/10 bg-white/[0.03] p-3">
            <div className="text-sm font-medium">{loanBook?.title ?? ""}</div>
            <div className="text-xs text-zinc-400">{loanBook?.author ?? ""}</div>
          </div>

          <div className="mt-3 grid gap-3">
            <div className="grid gap-1">
              <div className="text-xs text-zinc-400">Czytelnik</div>
              <Select value={loanReaderId === "" ? "" : String(loanReaderId)} onChange={(e) => setLoanReaderId(e.target.value ? Number(e.target.value) : "")}>
                <option value="">Wybierz czytelnika…</option>
                {readers.map((r) => (
                  <option key={r.id} value={r.id}>{r.label}</option>
                ))}
              </Select>
            </div>

            <div className="grid gap-1">
              <div className="text-xs text-zinc-400">Termin zwrotu</div>
              <Input type="date" value={loanDueDate} onChange={(e) => setLoanDueDate(e.target.value)} />
            </div>
          </div>

          <div className="mt-5 flex justify-end gap-2">
            <Button variant="ghost" onClick={() => setLoanOpen(false)}>Anuluj</Button>
            <Button onClick={submitLoan} disabled={loanReaderId === ""}>Wypożycz</Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  )
}
