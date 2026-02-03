import { http } from "@/api/http"
import type { Book } from "@/types"

type TopBook = {
  title: string
  author: string
  borrowedCopies: number
}
export const booksApi = {
  getSelect: () => http<any[]>("/api/books/select"),
  getTopBorrowed: (limit = 5) => http<TopBook[]>(`/api/books/top?limit=${limit}`),
  getAll: () => http<Book[]>("/api/books"),
  create: (data: Omit<Book, "id">) => http<void>("/api/books", { method: "POST", body: JSON.stringify(data) }),
  update: (id: number, data: Partial<Omit<Book, "id">>) =>
    http<void>(`/api/books/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  remove: (id: number) => http<void>(`/api/books/${id}`, { method: "DELETE" })
}
