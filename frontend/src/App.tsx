import { Routes, Route } from "react-router-dom"
import AppLayout from "@/layout/AppLayout"
import DashboardPage from "@/pages/DashboardPage"
import BooksPage from "@/pages/BooksPage"
import ReadersPage from "@/pages/ReadersPage"
import LoansPage from "@/pages/LoansPage"

export default function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/books" element={<BooksPage />} />
        <Route path="/readers" element={<ReadersPage />} />
        <Route path="/loans" element={<LoansPage />} />
      </Route>
    </Routes>
  )
}
