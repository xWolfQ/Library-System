import { API_BASE_URL } from "@/config"

export async function http<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {})
    },
    ...init
  })

  if (!res.ok) {
    const txt = await res.text().catch(() => "")
    throw new Error(`${res.status} ${res.statusText}${txt ? ` - ${txt}` : ""}`)
  }

  if (res.status === 204) return undefined as T

  const contentType = res.headers.get("content-type") ?? ""
  if (!contentType.includes("application/json")) return undefined as T

  return (await res.json()) as T
}
