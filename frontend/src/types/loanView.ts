export type LoanView = {
  id: number
  bookTitle: string
  readerName: string
  loanDate: string
  dueDate: string
  returnDate?: string
  status: "ACTIVE" | "RETURNED"
}
