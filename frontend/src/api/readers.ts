import { http } from "@/api/http"
import type { Reader } from "@/types"

export const readersApi = {
  getSelect: () => http<any[]>("/api/readers/select"),
  getAll: () => http<Reader[]>("/api/readers"),
  create: (data: Omit<Reader, "id">) => http<void>("/api/readers", { method: "POST", body: JSON.stringify(data) }),
  update: (id: number, data: Partial<Omit<Reader, "id">>) =>
    http<void>(`/api/readers/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  remove: (id: number) => http<void>(`/api/readers/${id}`, { method: "DELETE" })
}
