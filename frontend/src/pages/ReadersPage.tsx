import { useEffect, useMemo, useState } from "react"
import { Pencil, Trash2, Plus } from "lucide-react"
import { readersApi } from "@/api/readers"
import type { Reader } from "@/types"
import { PageHeader } from "@/pages/_pageHeader"
import { Card } from "@/components/ui/card"
import { Table, TBody, TD, TH, THead, TR } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Dialog, DialogContent, DialogTrigger } from "@/components/ui/dialog"

type ReaderForm = Omit<Reader, "id">

const empty: ReaderForm = {
  firstName: "",
  lastName: "",
  email: "",
  phoneNumber: ""
}

export default function ReadersPage() {
  const [items, setItems] = useState<Reader[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [query, setQuery] = useState("")
  const [createOpen, setCreateOpen] = useState(false)
  const [editOpen, setEditOpen] = useState(false)
  const [createForm, setCreateForm] = useState<ReaderForm>(empty)
  const [editId, setEditId] = useState<number | null>(null)
  const [editForm, setEditForm] = useState<ReaderForm>(empty)

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      setItems(await readersApi.getAll())
    } catch (e: any) {
      setError(String(e?.message ?? e))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase()
    if (!q) return items
    return items.filter((r) =>
      [r.firstName, r.lastName, r.email, r.phoneNumber ?? ""].some((x) => x.toLowerCase().includes(q))
    )
  }, [items, query])

  const onCreate = async () => {
    if (!createForm.firstName.trim() || !createForm.lastName.trim() || !createForm.email.trim()) {
      setError("Imię, nazwisko i email są wymagane.")
      return
    }
    setError(null)
    try {
      await readersApi.create(createForm)
      setCreateForm(empty)
      setCreateOpen(false)
      await load()
    } catch (e: any) {
      setError(String(e?.message ?? e))
    }
  }

  const openEdit = (r: Reader) => {
    setEditId(r.id)
    setEditForm({
      firstName: r.firstName ?? "",
      lastName: r.lastName ?? "",
      email: r.email ?? "",
      phoneNumber: r.phoneNumber ?? ""
    })
    setEditOpen(true)
  }

  const onSave = async () => {
    if (editId == null) return
    if (!editForm.firstName.trim() || !editForm.lastName.trim() || !editForm.email.trim()) {
      setError("Imię, nazwisko i email są wymagane.")
      return
    }
    setError(null)
    try {
      await readersApi.update(editId, editForm)
      setEditOpen(false)
      setEditId(null)
      await load()
    } catch (e: any) {
      setError(String(e?.message ?? e))
    }
  }

  const onDelete = async (id: number) => {
    if (!confirm("Usunąć czytelnika?")) return
    setError(null)
    try {
      await readersApi.remove(id)
      await load()
    } catch (e: any) {
      setError(String(e?.message ?? e))
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Czytelnicy"
        subtitle="Zarządzaj czytelnikami"
        right={
          <Dialog open={createOpen} onOpenChange={setCreateOpen}>
            <DialogTrigger>
              <Button>
                <Plus size={16} /> Dodaj czytelnika
              </Button>
            </DialogTrigger>
            <DialogContent title="Dodaj czytelnika" description="Wprowadź dane i zapisz.">
              <div className="grid gap-3 md:grid-cols-2">
                <Input placeholder="Imię" value={createForm.firstName} onChange={(e) => setCreateForm(f => ({ ...f, firstName: e.target.value }))} />
                <Input placeholder="Nazwisko" value={createForm.lastName} onChange={(e) => setCreateForm(f => ({ ...f, lastName: e.target.value }))} />
                <Input placeholder="Email" value={createForm.email} onChange={(e) => setCreateForm(f => ({ ...f, email: e.target.value }))} />
                <Input placeholder="Telefon" value={createForm.phoneNumber ?? ""} onChange={(e) => setCreateForm(f => ({ ...f, phoneNumber: e.target.value }))} />
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
        <div className="rounded-2xl border border-red-500/20 bg-red-500/10 p-4 text-sm text-red-200">
          {error}
        </div>
      ) : null}

      <Card className="p-4">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="text-sm text-zinc-400">{loading ? "Ładowanie..." : `Pozycje: ${filtered.length}`}</div>
          <div className="w-full sm:w-80">
            <Input placeholder="Szukaj (imię, nazwisko, email...)" value={query} onChange={(e) => setQuery(e.target.value)} />
          </div>
        </div>

        <div className="mt-4 overflow-x-auto rounded-2xl border border-white/10">
          <Table>
            <THead>
              <TR>                <TH>Imię</TH>
                <TH>Nazwisko</TH>
                <TH>Email</TH>
                <TH>Telefon</TH>
                <TH>Data rejestracji</TH>
                <TH className="text-right">Akcje</TH>
              </TR>
            </THead>
            <TBody>
              {filtered.map((r) => (
                <TR key={r.id}>                  <TD className="font-medium">{r.firstName}</TD>
                  <TD className="text-zinc-300">{r.lastName}</TD>
                  <TD className="text-zinc-300">{r.email}</TD>
                  <TD className="text-zinc-400">{r.phoneNumber ?? "-"}</TD>
                  <TD>
                      {r.registrationDate
                        ? new Date(r.registrationDate).toLocaleDateString("pl-PL")
                        : "-"}
                  </TD>
                  <TD className="text-right">
                    <div className="inline-flex gap-2">
                      <Button variant="secondary" onClick={() => openEdit(r)} title="Edytuj">
                        <Pencil size={16} />
                      </Button>
                      <Button variant="danger" onClick={() => onDelete(r.id)} title="Usuń">
                        <Trash2 size={16} />
                      </Button>
                    </div>
                  </TD>
                </TR>
              ))}
              {!loading && filtered.length === 0 ? (
                <TR>
                  <TD colSpan={5} className="p-8 text-center text-zinc-400">
                    Brak danych
                  </TD>
                </TR>
              ) : null}
            </TBody>
          </Table>
        </div>
      </Card>

      <Dialog open={editOpen} onOpenChange={setEditOpen}>
        <DialogContent title="Edytuj czytelnika" description="Zmień dane i zapisz.">
          <div className="grid gap-3 md:grid-cols-2">
            <Input placeholder="Imię" value={editForm.firstName} onChange={(e) => setEditForm(f => ({ ...f, firstName: e.target.value }))} />
            <Input placeholder="Nazwisko" value={editForm.lastName} onChange={(e) => setEditForm(f => ({ ...f, lastName: e.target.value }))} />
            <Input placeholder="Email" value={editForm.email} onChange={(e) => setEditForm(f => ({ ...f, email: e.target.value }))} />
            <Input placeholder="Telefon" value={editForm.phoneNumber ?? ""} onChange={(e) => setEditForm(f => ({ ...f, phoneNumber: e.target.value }))} />
          </div>
          <div className="mt-5 flex justify-end gap-2">
            <Button variant="ghost" onClick={() => setEditOpen(false)}>Anuluj</Button>
            <Button onClick={onSave}>Zapisz</Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  )
}
