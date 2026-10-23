import { useCallback, useEffect, useMemo, useState } from "react";
import type { FormEvent } from "react";
import { api } from "./api";
import type { Animal, Journey, Lot, Product, SanitaryPlan, TokenResponse } from "./api";

const PAGE_SIZE = 8;
const TOKEN_KEY = "sisganado.accessToken";

function localDateString() {
  const today = new Date();
  today.setMinutes(today.getMinutes() - today.getTimezoneOffset());
  return today.toISOString().slice(0, 10);
}

type ExecutionData = {
  animals: Animal[];
  products: Product[];
};

function readSavedToken(): string {
  const saved = sessionStorage.getItem(TOKEN_KEY);
  if (!saved) return "";
  try {
    const payload = saved.split(".")[1];
    const claims = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/"))) as { exp?: number };
    if (!claims.exp || claims.exp * 1000 <= Date.now()) {
      sessionStorage.removeItem(TOKEN_KEY);
      return "";
    }
    return saved;
  } catch {
    sessionStorage.removeItem(TOKEN_KEY);
    return "";
  }
}

function App() {
  const [token, setToken] = useState(readSavedToken);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loginError, setLoginError] = useState("");
  const [loginBusy, setLoginBusy] = useState(false);
  const [page, setPage] = useState(0);
  const [journeys, setJourneys] = useState<Journey[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [listLoading, setListLoading] = useState(false);
  const [listError, setListError] = useState("");
  const [lots, setLots] = useState<Lot[]>([]);
  const [plans, setPlans] = useState<SanitaryPlan[]>([]);
  const [catalogError, setCatalogError] = useState("");
  const [formError, setFormError] = useState("");
  const [notice, setNotice] = useState("");
  const [busy, setBusy] = useState(false);
  const [selectedJourney, setSelectedJourney] = useState<Journey | null>(null);
  const [executionData, setExecutionData] = useState<ExecutionData | null>(null);
  const [executionLoading, setExecutionLoading] = useState(false);
  const [selectedAnimals, setSelectedAnimals] = useState<number[]>([]);
  const [doses, setDoses] = useState<Record<number, string>>({});
  const [lotId, setLotId] = useState("");
  const [planId, setPlanId] = useState("");
  const [date, setDate] = useState(localDateString);
  const [description, setDescription] = useState("");

  const authorizationName = useMemo(() => {
    if (!token) return "";
    try {
      const payload = token.split(".")[1];
      const claims = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/"))) as { sub?: string };
      return claims.sub ?? "";
    } catch {
      return "";
    }
  }, [token]);

  const signOut = useCallback(() => {
    sessionStorage.removeItem(TOKEN_KEY);
    setToken("");
    setJourneys([]);
    setSelectedJourney(null);
    setExecutionData(null);
  }, []);

  const loadJourneys = useCallback(async () => {
    if (!token) return;
    setListLoading(true);
    setListError("");
    try {
      const result = await api.journeys(token, page, PAGE_SIZE);
      setJourneys(result.content);
      setTotalPages(result.page.totalPages);
      setTotalElements(result.page.totalElements);
    } catch (error) {
      const message = error instanceof Error ? error.message : "No se pudo cargar la lista.";
      setListError(message);
      if (message.includes("autenticación") || message.includes("JWT") || message.includes("no-autenticado")) {
        signOut();
      }
    } finally {
      setListLoading(false);
    }
  }, [page, signOut, token]);

  useEffect(() => {
    void loadJourneys();
  }, [loadJourneys]);

  useEffect(() => {
    if (!token) return;
    let cancelled = false;
    Promise.all([api.lots(token), api.plans(token)])
      .then(([lotPage, planPage]) => {
        if (cancelled) return;
        setLots(lotPage.content.filter((lot) => lot.activo));
        setPlans(planPage.content);
        setCatalogError("");
      })
      .catch((error: unknown) => {
        if (!cancelled) {
          setCatalogError(error instanceof Error ? error.message : "No se pudieron cargar los catálogos.");
        }
      });
    return () => { cancelled = true; };
  }, [token]);

  useEffect(() => {
    if (!token || !selectedJourney) {
      setExecutionData(null);
      return;
    }
    let cancelled = false;
    setExecutionLoading(true);
    setSelectedAnimals([]);
    setDoses({});
    Promise.all([
      api.animalsInLot(token, selectedJourney.loteId),
      api.plan(token, selectedJourney.planId),
    ])
      .then(async ([animalPage, plan]) => {
        const products = await Promise.all(plan.productosIds.map((id) => api.product(token, id)));
        if (!cancelled) setExecutionData({ animals: animalPage.content, products });
      })
      .catch((error: unknown) => {
        if (!cancelled) setFormError(error instanceof Error ? error.message : "No se pudo preparar la ejecución.");
      })
      .finally(() => {
        if (!cancelled) setExecutionLoading(false);
      });
    return () => { cancelled = true; };
  }, [selectedJourney, token]);

  async function submitLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoginBusy(true);
    setLoginError("");
    try {
      const response: TokenResponse = await api.login(username.trim(), password);
      sessionStorage.setItem(TOKEN_KEY, response.accessToken);
      setToken(response.accessToken);
      setPassword("");
      setPage(0);
    } catch (error) {
      setLoginError(error instanceof Error ? error.message : "No fue posible iniciar sesión.");
    } finally {
      setLoginBusy(false);
    }
  }

  async function submitPlanning(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!lotId || !planId) {
      setFormError("Seleccione el lote y el plan sanitario.");
      return;
    }
    setBusy(true);
    setFormError("");
    setNotice("");
    try {
      await api.createJourney(token, {
        loteId: Number(lotId),
        planId: Number(planId),
        fecha: date,
        descripcion: description.trim(),
      });
      setDescription("");
      setNotice("Jornada planificada. Ya aparece en la lista.");
      if (page === 0) await loadJourneys();
      else setPage(0);
    } catch (error) {
      setFormError(error instanceof Error ? error.message : "No se pudo planificar la jornada.");
    } finally {
      setBusy(false);
    }
  }

  async function submitExecution(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedJourney || !executionData) return;
    if (selectedAnimals.length === 0) {
      setFormError("Seleccione al menos un animal activo.");
      return;
    }
    const products = executionData.products.map((product) => ({
      productoId: product.id,
      dosisMlPorKg: Number(doses[product.id]),
    }));
    if (products.some((product) => !Number.isFinite(product.dosisMlPorKg) || product.dosisMlPorKg <= 0)) {
      setFormError("Ingrese una dosis mayor que cero para cada producto.");
      return;
    }
    setBusy(true);
    setFormError("");
    setNotice("");
    try {
      await api.executeJourney(token, selectedJourney.id, { animales: selectedAnimals, productos: products });
      setNotice(`Jornada #${selectedJourney.id} ejecutada y registrada.`);
      setSelectedJourney(null);
      await loadJourneys();
    } catch (error) {
      setFormError(error instanceof Error ? error.message : "No se pudo ejecutar la jornada.");
    } finally {
      setBusy(false);
    }
  }

  if (!token) {
    return (
      <main className="login-shell">
        <section className="login-card">
          <div className="brand-mark" aria-hidden="true">SG</div>
          <p className="eyebrow">SISTEMA DE CONTROL SANITARIO</p>
          <h1>Bienvenido a SisGanado</h1>
          <p className="muted">Inicie sesión para consultar y ejecutar jornadas sanitarias.</p>
          <form onSubmit={submitLogin} className="stack">
            <label>Usuario
              <input autoComplete="username" value={username} onChange={(event) => setUsername(event.target.value)} required />
            </label>
            <label>Contraseña
              <input type="password" autoComplete="current-password" value={password}
                onChange={(event) => setPassword(event.target.value)} required />
            </label>
            {loginError && <p className="error-message" role="alert">{loginError}</p>}
            <button className="button primary full" disabled={loginBusy}>
              {loginBusy ? "Validando…" : "Iniciar sesión"}
            </button>
          </form>
        </section>
      </main>
    );
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <a className="brand" href="/" aria-label="SisGanado, inicio">
          <span className="brand-mark small" aria-hidden="true">SG</span>
          <span>SisGanado <small>CONTROL SANITARIO</small></span>
        </a>
        <div className="account">
          <span className="account-name">{authorizationName || "Usuario"}</span>
          <button className="button quiet" onClick={signOut}>Cerrar sesión</button>
        </div>
      </header>

      <main className="content">
        <section className="hero">
          <div>
            <p className="eyebrow">OPERACIÓN DE FINCA</p>
            <h1>Jornadas sanitarias</h1>
            <p className="muted">Planifique, consulte y registre tratamientos para sus lotes.</p>
          </div>
          <div className="hero-stat"><strong>{totalElements}</strong><span>jornadas registradas</span></div>
        </section>

        {notice && <p className="notice" role="status">{notice}</p>}
        {catalogError && <p className="error-message" role="alert">{catalogError}</p>}

        <div className="dashboard-grid">
          <section className="panel plan-panel">
            <div className="section-heading">
              <div><p className="eyebrow">NUEVO PROCESO</p><h2>Planificar jornada</h2></div>
              <span className="step-badge">01</span>
            </div>
            <form onSubmit={submitPlanning} className="stack">
              <label>Lote
                <select value={lotId} onChange={(event) => setLotId(event.target.value)} required>
                  <option value="">Seleccione un lote</option>
                  {lots.map((lot) => <option value={lot.id} key={lot.id}>{lot.nombre}</option>)}
                </select>
              </label>
              <label>Plan sanitario
                <select value={planId} onChange={(event) => setPlanId(event.target.value)} required>
                  <option value="">Seleccione un plan</option>
                  {plans.map((plan) => <option value={plan.id} key={plan.id}>{plan.nombre}</option>)}
                </select>
              </label>
              <label>Fecha
                <input type="date" min={localDateString()} value={date}
                  onChange={(event) => setDate(event.target.value)} required />
              </label>
              <label>Descripción
                <textarea maxLength={200} rows={3} value={description}
                  onChange={(event) => setDescription(event.target.value)} required />
              </label>
              {formError && !selectedJourney && <p className="error-message" role="alert">{formError}</p>}
              <button className="button primary" disabled={busy || !!catalogError}>
                {busy ? "Guardando…" : "Planificar jornada"}
              </button>
            </form>
          </section>

          <section className="panel list-panel">
            <div className="section-heading">
              <div><p className="eyebrow">SEGUIMIENTO</p><h2>Jornadas recientes</h2></div>
              <button className="button quiet" onClick={() => void loadJourneys()} disabled={listLoading}>Actualizar</button>
            </div>
            {listLoading && <div className="state-box" role="status"><span className="spinner" /> Cargando jornadas…</div>}
            {!listLoading && listError && <div className="state-box error-message" role="alert">{listError}</div>}
            {!listLoading && !listError && journeys.length === 0 && (
              <div className="state-box empty-state">No hay jornadas en esta página. Planifique la primera jornada.</div>
            )}
            {!listLoading && !listError && journeys.length > 0 && (
              <>
                <div className="journey-list">
                  {journeys.map((journey) => (
                    <article className="journey-card" key={journey.id}>
                      <div className="journey-date">{new Date(`${journey.fecha}T00:00:00`).toLocaleDateString("es-CR", {
                        day: "2-digit", month: "short", year: "numeric",
                      })}</div>
                      <div className="journey-main">
                        <strong>{journey.descripcion}</strong>
                        <span>Lote #{journey.loteId} · {journey.cantidadAnimales} animales · Plan #{journey.planId}</span>
                      </div>
                      <button className="button outline" onClick={() => {
                        setFormError("");
                        setSelectedJourney(journey);
                      }}>Ejecutar</button>
                    </article>
                  ))}
                </div>
                <nav className="pagination" aria-label="Paginación de jornadas">
                  <button className="button quiet" disabled={page === 0 || listLoading}
                    onClick={() => setPage((current) => Math.max(0, current - 1))}>← Anterior</button>
                  <span>Página {page + 1} de {Math.max(totalPages, 1)}</span>
                  <button className="button quiet" disabled={page + 1 >= totalPages || listLoading}
                    onClick={() => setPage((current) => current + 1)}>Siguiente →</button>
                </nav>
              </>
            )}
          </section>
        </div>

        {selectedJourney && (
          <section className="panel execution-panel">
            <div className="section-heading">
              <div><p className="eyebrow">EJECUCIÓN</p><h2>Registrar jornada #{selectedJourney.id}</h2></div>
              <button className="button quiet" onClick={() => setSelectedJourney(null)}>Cerrar</button>
            </div>
            {executionLoading && <div className="state-box" role="status"><span className="spinner" /> Cargando animales y productos…</div>}
            {!executionLoading && !executionData && formError && (
              <p className="error-message" role="alert">{formError}</p>
            )}
            {!executionLoading && executionData && (
              <form onSubmit={submitExecution} className="stack">
                <fieldset>
                  <legend>Animales activos del lote</legend>
                  {executionData.animals.length === 0 && <p className="muted">No hay animales activos disponibles en este lote.</p>}
                  <div className="animal-grid">
                    {executionData.animals.map((animal) => (
                      <label className="check-option" key={animal.id}>
                        <input type="checkbox" checked={selectedAnimals.includes(animal.id)}
                          onChange={(event) => setSelectedAnimals((current) => event.target.checked
                            ? [...current, animal.id]
                            : current.filter((id) => id !== animal.id))} />
                        <span><strong>{animal.codigoArete}</strong><small>ID {animal.id}</small></span>
                      </label>
                    ))}
                  </div>
                </fieldset>
                <fieldset>
                  <legend>Dosis por producto (ml/kg)</legend>
                  <div className="product-dose-grid">
                    {executionData.products.map((product) => (
                      <label key={product.id}>{product.nombre}
                        <input type="number" min="0.001" step="0.001" required
                          placeholder={product.dosisMinima != null && product.dosisMaxima != null
                            ? `${product.dosisMinima} – ${product.dosisMaxima}`
                            : "Dosis"}
                          value={doses[product.id] ?? ""}
                          onChange={(event) => setDoses((current) => ({ ...current, [product.id]: event.target.value }))} />
                        {(product.dosisMinima != null || product.dosisMaxima != null) && (
                          <small>Rango: {product.dosisMinima ?? "—"} – {product.dosisMaxima ?? "—"} ml/kg</small>
                        )}
                      </label>
                    ))}
                  </div>
                </fieldset>
                {formError && <p className="error-message" role="alert">{formError}</p>}
                <div className="form-actions">
                  <button className="button primary" disabled={busy || executionData.animals.length === 0}>
                    {busy ? "Registrando…" : "Confirmar ejecución"}
                  </button>
                  <button type="button" className="button quiet" onClick={() => setSelectedJourney(null)}>Cancelar</button>
                </div>
              </form>
            )}
          </section>
        )}
      </main>
    </div>
  );
}

export default App;
