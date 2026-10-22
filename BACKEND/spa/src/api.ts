const API_URL = (import.meta.env.VITE_API_BASE_URL ?? "").replace(/\/$/, "");

export type TokenResponse = {
  accessToken: string;
  tokenType: string;
  expiresAt: string;
};

export type Page<T> = {
  content: T[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
};

export type Journey = {
  id: number;
  loteId: number;
  planId: number;
  fecha: string;
  descripcion: string;
  cantidadAnimales: number;
  costoTotal: number | null;
  mensaje: string;
};

export type Lot = {
  id: number;
  nombre: string;
  activo: boolean;
};

export type SanitaryPlan = {
  id: number;
  nombre: string;
  productosIds: number[];
};

export type Product = {
  id: number;
  nombre: string;
  dosisMinima: number | null;
  dosisMaxima: number | null;
};

export type Animal = {
  id: number;
  codigoArete: string;
  estado: string;
  loteId: number;
};

async function request<T>(path: string, token?: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  if (init.body) headers.set("Content-Type", "application/json");
  if (token) headers.set("Authorization", `Bearer ${token}`);

  const response = await fetch(`${API_URL}${path}`, { ...init, headers });
  if (!response.ok) {
    const body = await response.json().catch(() => undefined) as
      | { detail?: string; message?: string; errors?: Record<string, string[]> }
      | undefined;
    const fieldMessages = body?.errors
      ? Object.values(body.errors).flat().join(" ")
      : "";
    throw new Error(fieldMessages || body?.detail || body?.message || `Error HTTP ${response.status}`);
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export const api = {
  login: (username: string, password: string) =>
    request<TokenResponse>("/auth/login", undefined, {
      method: "POST",
      body: JSON.stringify({ username, password }),
    }),
  journeys: (token: string, page: number, size: number) =>
    request<Page<Journey>>(`/api/v1/jornadas?page=${page}&size=${size}&sort=fecha,desc`, token),
  lots: (token: string) =>
    request<Page<Lot>>("/api/v1/lotes?page=0&size=100&sort=nombre,asc", token),
  plans: (token: string) =>
    request<Page<SanitaryPlan>>("/api/v1/planes-sanitarios?page=0&size=100&sort=nombre,asc", token),
  animalsInLot: async (token: string, lotId: number) => {
    const first = await request<Page<Animal>>(
      `/api/v1/animales?estado=Activo&loteId=${lotId}&page=0&size=100&sort=codigoArete,asc`,
      token,
    );
    const content = [...first.content];
    for (let page = 1; page < first.page.totalPages; page += 1) {
      const next = await request<Page<Animal>>(
        `/api/v1/animales?estado=Activo&loteId=${lotId}&page=${page}&size=100&sort=codigoArete,asc`,
        token,
      );
      content.push(...next.content);
    }
    return { ...first, content };
  },
  product: (token: string, id: number) => request<Product>(`/api/v1/productos/${id}`, token),
  plan: (token: string, id: number) => request<SanitaryPlan>(`/api/v1/planes-sanitarios/${id}`, token),
  createJourney: (token: string, payload: {
    loteId: number;
    planId: number;
    fecha: string;
    descripcion: string;
  }) => request<Journey>("/api/v1/jornadas", token, {
    method: "POST",
    body: JSON.stringify(payload),
  }),
  executeJourney: (token: string, journeyId: number, payload: {
    animales: number[];
    productos: Array<{ productoId: number; dosisMlPorKg: number }>;
  }) => request<unknown>(`/api/v1/jornadas/${journeyId}/ejecucion`, token, {
    method: "POST",
    body: JSON.stringify(payload),
  }),
};
