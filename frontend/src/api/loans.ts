import { http } from "@/api/http"

export const loansApi = {
  getAll: () => http<any[]>("/api/loans"),
  getHistory: () => http<any[]>("/api/loan-history"),
  create: (data: { bookId: number; readerId: number; dueDate: string }) =>
    http<void>("/api/loans", { method: "POST", body: JSON.stringify(data) }),
  returnLoan: (loanId: number) =>
    http<void>("/api/loans/return", { method: "PUT", body: JSON.stringify({ loanId }) }),
}
