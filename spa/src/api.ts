const API_URL = (import.meta.env.VITE_API_BASE_URL ?? "").replace(/\/$/, "");
export const AUTH_UNAUTHORIZED_EVENT = "sisganado:unauthorized";
export const AUTH_TOKEN_KEY = "sisganado.accessToken";

export type TokenResponse = {
  accessToken: string;
  tokenType: string;
  expiresAt: string;
};

export type RegistrationResponse = {
  username: string;
  mensaje: string;
  requiereAprobacion: boolean;
};

export type UserAccount = {
  id: number;
  username: string;
  nombre: string;
  rol: string;
  activo: boolean;
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
  tipo?: string;
  unidadMedida?: string;
  dosisMinima: number | null;
  dosisMaxima: number | null;
  precioUnitario?: number;
  contenidoMlPorUnidad?: number;
  retiroCarneDias?: number | null;
  retiroLecheDias?: number | null;
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
    if (response.status === 401 && token) {
      sessionStorage.removeItem(AUTH_TOKEN_KEY);
      window.dispatchEvent(new Event(AUTH_UNAUTHORIZED_EVENT));
    }
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
  register: (payload: {
    username: string;
    nombreCompleto: string;
    password: string;
    tipoCuenta: "VETERINARIO" | "USUARIO";
  }) => request<RegistrationResponse>("/auth/register", undefined, {
    method: "POST",
    body: JSON.stringify(payload),
  }),
  journeys: (token: string, page: number, size: number) =>
    request<Page<Journey>>(`/api/v1/jornadas?page=${page}&size=${size}&sort=fecha,desc`, token),
  lots: (token: string) =>
    request<Page<Lot>>("/api/v1/lotes?page=0&size=100&sort=nombre,asc", token),
  createLot: (token: string, payload: {
    nombre: string;
    ubicacion: string;
    proposito: string;
    activo: boolean;
  }) => request<Lot & { ubicacion: string; proposito: string }>("/api/v1/lotes", token, {
    method: "POST",
    body: JSON.stringify(payload),
  }),
  plans: (token: string) =>
    request<Page<SanitaryPlan>>("/api/v1/planes-sanitarios?page=0&size=100&sort=nombre,asc", token),
  createPlan: (token: string, payload: {
    nombre: string;
    descripcion: string;
    edadObjetivoMeses: number | null;
    intervaloMinimoDias: number;
    productosIds: number[];
  }) => request<SanitaryPlan>("/api/v1/planes-sanitarios", token, {
    method: "POST",
    body: JSON.stringify(payload),
  }),
  products: (token: string) =>
    request<Page<Product>>("/api/v1/productos?page=0&size=100&sort=nombre,asc", token),
  createProduct: (token: string, payload: {
    nombre: string;
    tipo: string;
    unidadMedida: string;
    dosisMinima: number;
    dosisMaxima: number;
    precioUnitario: number;
    contenidoMlPorUnidad: number;
    retiroCarneDias: number;
    retiroLecheDias: number;
  }) => request<Product>("/api/v1/productos", token, {
    method: "POST",
    body: JSON.stringify(payload),
  }),
  createAnimal: (token: string, payload: {
    codigoArete: string;
    raza: string;
    sexo: string;
    pesoKg: number;
    fechaNacimiento: string;
    estado: string;
    loteId: number;
  }) => request<Animal>("/api/v1/animales", token, {
    method: "POST",
    body: JSON.stringify(payload),
  }),
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
  users: (token: string) => request<UserAccount[]>("/api/v1/admin/usuarios", token),
  activateVeterinarian: (token: string, id: number) =>
    request<UserAccount>(`/api/v1/admin/usuarios/${id}/activar`, token, { method: "PUT" }),
};
