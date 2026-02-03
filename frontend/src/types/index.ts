export type Book = {
  id: number
  title: string
  author: string
  isbn?: string
  publishedYear?: number
  category?: string
  availableCopies?: number
  borrowCount?: number
}

export type Reader = {
  id: number
  firstName: string
  lastName: string
  email: string
  phoneNumber?: string
  registrationDate: string   
}


export type Loan = {
  id: number
  bookId: number
  readerId: number
  loanDate: string
  dueDate: string
}

export type LoanHistory = {
  id: number
  bookTitle: string
  readerName: string
  loanDate: string
  dueDate: string
  returnDate: string
}
\nexport * from './loanView'\n