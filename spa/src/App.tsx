import { useCallback, useEffect, useMemo, useState } from "react";
import type { FormEvent } from "react";
import { api, AUTH_TOKEN_KEY, AUTH_UNAUTHORIZED_EVENT } from "./api";
import type {
  Animal,
  Journey,
  JourneyCostResult,
  Lot,
  Product,
  SanitaryPlan,
  TokenResponse,
  UserAccount,
} from "./api";

const PAGE_SIZE = 8;
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
  const saved = sessionStorage.getItem(AUTH_TOKEN_KEY);
  if (!saved) return "";
  try {
    const payload = saved.split(".")[1];
    const claims = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/"))) as { exp?: number };
    if (!claims.exp || claims.exp * 1000 <= Date.now()) {
      sessionStorage.removeItem(AUTH_TOKEN_KEY);
      return "";
    }
    return saved;
  } catch {
    sessionStorage.removeItem(AUTH_TOKEN_KEY);
    return "";
  }
}

function App() {
  const [token, setToken] = useState(readSavedToken);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loginError, setLoginError] = useState("");
  const [loginNotice, setLoginNotice] = useState("");
  const [authMode, setAuthMode] = useState<"login" | "register">("login");
  const [loginBusy, setLoginBusy] = useState(false);
  const [registerName, setRegisterName] = useState("");
  const [registerUsername, setRegisterUsername] = useState("");
  const [registerPassword, setRegisterPassword] = useState("");
  const [registerRole, setRegisterRole] = useState<"USUARIO" | "VETERINARIO">("USUARIO");
  const [registerError, setRegisterError] = useState("");
  const [registerBusy, setRegisterBusy] = useState(false);
  const [users, setUsers] = useState<UserAccount[]>([]);
  const [usersError, setUsersError] = useState("");
  const [activatingUser, setActivatingUser] = useState<number | null>(null);
  const [products, setProducts] = useState<Product[]>([]);
  const [catalogFormError, setCatalogFormError] = useState("");
  const [catalogFormNotice, setCatalogFormNotice] = useState("");
  const [catalogBusy, setCatalogBusy] = useState("");
  const [newLotName, setNewLotName] = useState("");
  const [newLotLocation, setNewLotLocation] = useState("");
  const [newLotPurpose, setNewLotPurpose] = useState("");
  const [newProductName, setNewProductName] = useState("");
  const [newProductType, setNewProductType] = useState("");
  const [newProductUnit, setNewProductUnit] = useState("ml");
  const [newProductMinDose, setNewProductMinDose] = useState("");
  const [newProductMaxDose, setNewProductMaxDose] = useState("");
  const [newProductPrice, setNewProductPrice] = useState("");
  const [newProductContent, setNewProductContent] = useState("");
  const [newPlanName, setNewPlanName] = useState("");
  const [newPlanDescription, setNewPlanDescription] = useState("");
  const [newPlanAge, setNewPlanAge] = useState("");
  const [newPlanInterval, setNewPlanInterval] = useState("7");
  const [newPlanProducts, setNewPlanProducts] = useState<number[]>([]);
  const [newAnimalTag, setNewAnimalTag] = useState("");
  const [newAnimalBreed, setNewAnimalBreed] = useState("");
  const [newAnimalSex, setNewAnimalSex] = useState("Hembra");
  const [newAnimalWeight, setNewAnimalWeight] = useState("");
  const [newAnimalBirthDate, setNewAnimalBirthDate] = useState(localDateString);
  const [newAnimalLot, setNewAnimalLot] = useState("");
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
  const [selectedCostJourney, setSelectedCostJourney] = useState<Journey | null>(null);
  const [costResult, setCostResult] = useState<JourneyCostResult | null>(null);
  const [costError, setCostError] = useState("");
  const [costBusy, setCostBusy] = useState(false);
  const [laborCost, setLaborCost] = useState("0.00");
  const [transportCost, setTransportCost] = useState("0.00");
  const [veterinarianCost, setVeterinarianCost] = useState("0.00");
  const [otherCost, setOtherCost] = useState("0.00");
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

  const authorizationRole = useMemo(() => {
    if (!token) return "";
    try {
      const payload = token.split(".")[1];
      const claims = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/"))) as {
        roles?: string[];
      };
      return claims.roles?.[0] ?? "";
    } catch {
      return "";
    }
  }, [token]);

  const signOut = useCallback(() => {
    sessionStorage.removeItem(AUTH_TOKEN_KEY);
    setToken("");
    setJourneys([]);
    setSelectedJourney(null);
    setSelectedCostJourney(null);
    setExecutionData(null);
    setCostResult(null);
  }, []);

  useEffect(() => {
    window.addEventListener(AUTH_UNAUTHORIZED_EVENT, signOut);
    return () => window.removeEventListener(AUTH_UNAUTHORIZED_EVENT, signOut);
  }, [signOut]);

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
    } finally {
      setListLoading(false);
    }
  }, [page, token]);

  const loadCatalogs = useCallback(async () => {
    if (!token) return;
    const [lotPage, planPage, productPage] = await Promise.all([
      api.lots(token),
      api.plans(token),
      api.products(token),
    ]);
    setLots(lotPage.content.filter((lot) => lot.activo));
    setPlans(planPage.content);
    setProducts(productPage.content);
    setCatalogError("");
  }, [token]);

  useEffect(() => {
    void loadJourneys();
  }, [loadJourneys]);

  useEffect(() => {
    if (!token) return;
    let cancelled = false;
    loadCatalogs().catch((error: unknown) => {
        if (!cancelled) {
          setCatalogError(error instanceof Error ? error.message : "No se pudieron cargar los catálogos.");
        }
      });
    return () => { cancelled = true; };
  }, [loadCatalogs, token]);

  useEffect(() => {
    if (!token || authorizationRole !== "ADMIN") {
      setUsers([]);
      setUsersError("");
      return;
    }
    let cancelled = false;
    api.users(token)
      .then((result) => {
        if (!cancelled) {
          setUsers(result);
          setUsersError("");
        }
      })
      .catch((error: unknown) => {
        if (!cancelled) setUsersError(error instanceof Error ? error.message : "No se pudieron cargar las cuentas.");
      });
    return () => { cancelled = true; };
  }, [authorizationRole, token]);

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
    setLoginNotice("");
    try {
      const response: TokenResponse = await api.login(username.trim(), password);
      sessionStorage.setItem(AUTH_TOKEN_KEY, response.accessToken);
      setToken(response.accessToken);
      setPassword("");
      setPage(0);
    } catch (error) {
      setLoginError(error instanceof Error ? error.message : "No fue posible iniciar sesión.");
    } finally {
      setLoginBusy(false);
    }
  }

  async function submitRegistration(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setRegisterBusy(true);
    setRegisterError("");
    setLoginNotice("");
    try {
      const result = await api.register({
        username: registerUsername.trim(),
        nombreCompleto: registerName.trim(),
        password: registerPassword,
        tipoCuenta: registerRole,
      });
      setUsername(result.username);
      setPassword("");
      setRegisterPassword("");
      setLoginNotice(result.mensaje);
      setAuthMode("login");
    } catch (error) {
      setRegisterError(error instanceof Error ? error.message : "No se pudo crear la cuenta.");
    } finally {
      setRegisterBusy(false);
    }
  }

  async function activateVeterinarian(userId: number) {
    setActivatingUser(userId);
    setUsersError("");
    try {
      const activated = await api.activateVeterinarian(token, userId);
      setUsers((current) => current.map((user) => user.id === activated.id ? activated : user));
    } catch (error) {
      setUsersError(error instanceof Error ? error.message : "No se pudo activar la cuenta.");
    } finally {
      setActivatingUser(null);
    }
  }

  async function saveCatalogItem(
    event: FormEvent<HTMLFormElement>,
    busyKey: string,
    successMessage: string,
    create: () => Promise<unknown>,
  ) {
    event.preventDefault();
    setCatalogBusy(busyKey);
    setCatalogFormError("");
    setCatalogFormNotice("");
    try {
      await create();
      setCatalogFormNotice(successMessage);
      try {
        await loadCatalogs();
      } catch (error) {
        const detail = error instanceof Error ? error.message : "Error desconocido";
        setCatalogFormError(`El registro se guardó, pero no se pudo actualizar la lista: ${detail}`);
      }
    } catch (error) {
      setCatalogFormError(error instanceof Error ? error.message : "No se pudo guardar el registro.");
    } finally {
      setCatalogBusy("");
    }
  }

  function submitLot(event: FormEvent<HTMLFormElement>) {
    return saveCatalogItem(event, "lote", "Lote creado y disponible para planificar.", async () => {
      const created = await api.createLot(token, {
        nombre: newLotName.trim(),
        ubicacion: newLotLocation.trim(),
        proposito: newLotPurpose.trim(),
        activo: true,
      });
      setNewAnimalLot(String(created.id));
      setNewLotName("");
      setNewLotLocation("");
      setNewLotPurpose("");
    });
  }

  function submitProduct(event: FormEvent<HTMLFormElement>) {
    return saveCatalogItem(event, "producto", "Producto creado; ya puedes incluirlo en un plan.", async () => {
      await api.createProduct(token, {
        nombre: newProductName.trim(),
        tipo: newProductType.trim(),
        unidadMedida: newProductUnit.trim(),
        dosisMinima: Number(newProductMinDose),
        dosisMaxima: Number(newProductMaxDose),
        precioUnitario: Number(newProductPrice),
        contenidoMlPorUnidad: Number(newProductContent),
        retiroCarneDias: 0,
        retiroLecheDias: 0,
      });
      setNewProductName("");
      setNewProductType("");
      setNewProductMinDose("");
      setNewProductMaxDose("");
      setNewProductPrice("");
      setNewProductContent("");
    });
  }

  function submitPlan(event: FormEvent<HTMLFormElement>) {
    if (newPlanProducts.length === 0) {
      event.preventDefault();
      setCatalogFormError("Seleccione al menos un producto para el plan.");
      return;
    }
    return saveCatalogItem(event, "plan", "Plan sanitario creado y listo para usar.", async () => {
      await api.createPlan(token, {
        nombre: newPlanName.trim(),
        descripcion: newPlanDescription.trim(),
        edadObjetivoMeses: newPlanAge ? Number(newPlanAge) : null,
        intervaloMinimoDias: Number(newPlanInterval),
        productosIds: newPlanProducts,
      });
      setNewPlanName("");
      setNewPlanDescription("");
      setNewPlanAge("");
      setNewPlanProducts([]);
    });
  }

  function submitAnimal(event: FormEvent<HTMLFormElement>) {
    return saveCatalogItem(event, "animal", "Animal registrado en el lote.", async () => {
      await api.createAnimal(token, {
        codigoArete: newAnimalTag.trim(),
        raza: newAnimalBreed.trim(),
        sexo: newAnimalSex,
        pesoKg: Number(newAnimalWeight),
        fechaNacimiento: newAnimalBirthDate,
        estado: "Activo",
        loteId: Number(newAnimalLot),
      });
      setNewAnimalTag("");
      setNewAnimalBreed("");
      setNewAnimalWeight("");
    });
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

  async function submitJourneyCost(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedCostJourney) return;
    setCostBusy(true);
    setCostError("");
    setCostResult(null);
    try {
      const result = await api.processJourneyCost(token, selectedCostJourney.id, {
        manoObra: Number(laborCost),
        transporte: Number(transportCost),
        veterinario: Number(veterinarianCost),
        otros: Number(otherCost),
      });
      setCostResult(result);
      setNotice(`Costo de la jornada #${result.jornadaId} registrado correctamente.`);
      setSelectedCostJourney(null);
      setLaborCost("0.00");
      setTransportCost("0.00");
      setVeterinarianCost("0.00");
      setOtherCost("0.00");
      await loadJourneys();
    } catch (error) {
      setCostError(error instanceof Error ? error.message : "No se pudo registrar el costo de la jornada.");
    } finally {
      setCostBusy(false);
    }
  }

  const canOperate = authorizationRole === "ADMIN" || authorizationRole === "VETERINARIO";

  if (!token) {
    return (
      <main className="login-shell">
        <div className="login-layout">
          <section className="login-illustration" aria-label="SisGanado, gestión sanitaria de finca">
            <img src="/sisganado-login.png" alt="Ilustración de una vaca en una finca con un tablero de control" />
            <div className="illustration-shade" />
            <div className="illustration-copy">
              <span className="illustration-brand"><span aria-hidden="true">✦</span> SISGANADO</span>
              <p>El cuidado de tu finca,<br />en un solo lugar.</p>
              <span className="illustration-caption">CONTROL SANITARIO · FINCA SAN ISIDRO</span>
            </div>
          </section>

          <section className="login-card">
            <a className="login-brand" href="/" aria-label="SisGanado inicio">
              <span className="brand-mark" aria-hidden="true">SG</span>
              <span>SisGanado <small>GESTIÓN GANADERA</small></span>
            </a>
            {authMode === "login" ? (
              <>
                <p className="eyebrow">QUÉ BUENO TENERTE DE VUELTA</p>
                <h1>Iniciar sesión</h1>
                <p className="muted">Ingresa a tu cuenta para continuar con la gestión sanitaria.</p>
                {loginNotice && <p className="notice" role="status">{loginNotice}</p>}
                <form onSubmit={submitLogin} className="stack auth-form">
                  <label>Nombre de usuario
                    <input autoComplete="username" value={username}
                      onChange={(event) => setUsername(event.target.value)} required />
                  </label>
                  <label>Contraseña
                    <input type="password" autoComplete="current-password" value={password}
                      onChange={(event) => setPassword(event.target.value)} required />
                  </label>
                  {loginError && <p className="error-message" role="alert">{loginError}</p>}
                  <button className="button primary full" disabled={loginBusy}>
                    {loginBusy ? "Validando…" : "Entrar a SisGanado"}
                  </button>
                </form>
                <p className="auth-switch">¿Aún no tienes cuenta?{" "}
                  <button type="button" className="text-button" onClick={() => {
                    setRegisterError("");
                    setAuthMode("register");
                  }}>Crear una cuenta</button>
                </p>
              </>
            ) : (
              <>
                <p className="eyebrow">ÚNETE A SISGANADO</p>
                <h1>Crear cuenta</h1>
                <p className="muted">Registra tus datos para comenzar a usar la plataforma.</p>
                <form onSubmit={submitRegistration} className="stack auth-form">
                  <label>Nombre completo
                    <input autoComplete="name" maxLength={120} value={registerName}
                      onChange={(event) => setRegisterName(event.target.value)} required />
                  </label>
                  <label>Nombre de usuario
                    <input autoComplete="username" minLength={3} maxLength={80}
                      pattern="[A-Za-z0-9._-]{3,80}" value={registerUsername}
                      onChange={(event) => setRegisterUsername(event.target.value)} required />
                    <small className="field-hint">Usa letras, números, punto, guion o guion bajo.</small>
                  </label>
                  <label>Tipo de cuenta
                    <select value={registerRole}
                      onChange={(event) => setRegisterRole(event.target.value as "USUARIO" | "VETERINARIO")}>
                      <option value="USUARIO">Usuario — consulta</option>
                      <option value="VETERINARIO">Veterinario — requiere aprobación</option>
                    </select>
                  </label>
                  <label>Contraseña
                    <input type="password" autoComplete="new-password" minLength={12} maxLength={72}
                      value={registerPassword}
                      onChange={(event) => setRegisterPassword(event.target.value)} required />
                    <small className="field-hint">Debe tener al menos 12 caracteres.</small>
                  </label>
                  {registerError && <p className="error-message" role="alert">{registerError}</p>}
                  <button className="button primary full" disabled={registerBusy}>
                    {registerBusy ? "Creando cuenta…" : "Crear cuenta"}
                  </button>
                </form>
                <p className="auth-switch">¿Ya tienes una cuenta?{" "}
                  <button type="button" className="text-button" onClick={() => {
                    setLoginNotice("");
                    setAuthMode("login");
                  }}>Iniciar sesión</button>
                </p>
              </>
            )}
            <p className="login-footer">Tu finca más organizada, cada día.</p>
          </section>
        </div>
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
            <p className="muted">{canOperate
              ? "Planifique, consulte y registre tratamientos para sus lotes."
              : "Consulta el seguimiento de jornadas sanitarias de tu cuenta."}</p>
          </div>
          <div className="hero-stat"><strong>{totalElements}</strong><span>jornadas registradas</span></div>
        </section>

        {notice && <p className="notice" role="status">{notice}</p>}
        {costResult && (
          <section className="panel cost-result" aria-label="Resultado del costeo">
            <div className="section-heading">
              <div><p className="eyebrow">COSTEO REGISTRADO</p><h2>Jornada #{costResult.jornadaId}</h2></div>
              <strong className="cost-total">{formatCurrency(costResult.costoTotal)}</strong>
            </div>
            <div className="cost-breakdown">
              <span>Productos e inventario <strong>{formatCurrency(costResult.costoProducto)}</strong></span>
              <span>Costos adicionales <strong>{formatCurrency(costResult.costosAdicionales)}</strong></span>
              <span>Promedio por animal <strong>{formatCurrency(costResult.costoPromedioPorAnimal)}</strong></span>
            </div>
          </section>
        )}
        {catalogError && <p className="error-message" role="alert">{catalogError}</p>}

        <div className={`dashboard-grid${canOperate ? "" : " is-readonly"}`}>
          {canOperate && <section className="panel plan-panel">
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
          </section>}

          <section className="panel list-panel">
            <div className="section-heading">
              <div><p className="eyebrow">SEGUIMIENTO</p><h2>Jornadas recientes</h2></div>
              <button className="button quiet" onClick={() => void loadJourneys()} disabled={listLoading}>Actualizar</button>
            </div>
            {listLoading && <div className="state-box" role="status"><span className="spinner" /> Cargando jornadas…</div>}
            {!listLoading && listError && <div className="state-box error-message" role="alert">{listError}</div>}
            {!listLoading && !listError && journeys.length === 0 && (
              <div className="state-box empty-state">{canOperate
                ? "No hay jornadas en esta página. Planifique la primera jornada."
                : "No hay jornadas registradas para mostrar."}</div>
            )}
            {!listLoading && !listError && journeys.length > 0 && (
              <>
                <div className="journey-list">
                  {journeys.map((journey) => {
                    const status = journey.estado || journey.mensaje;
                    return (
                      <article className="journey-card" key={journey.id}>
                        <div className="journey-date">{new Date(`${journey.fecha}T00:00:00`).toLocaleDateString("es-CR", {
                          day: "2-digit", month: "short", year: "numeric",
                        })}</div>
                        <div className="journey-main">
                          <strong>{journey.descripcion}</strong>
                          <span>Lote #{journey.loteId} · {journey.cantidadAnimales} animales · Plan #{journey.planId} · {status}</span>
                          {status === "COSTEADA" && (
                            <span>Costo total: {formatCurrency(journey.costoTotal ?? 0)}</span>
                          )}
                        </div>
                        {canOperate && status === "PLANIFICADA" && (
                          <button className="button outline" onClick={() => {
                            setFormError("");
                            setCostResult(null);
                            setSelectedJourney(journey);
                          }}>Ejecutar</button>
                        )}
                        {canOperate && status === "EJECUTADA" && (
                          <button className="button outline" onClick={() => {
                            setCostError("");
                            setCostResult(null);
                            setSelectedCostJourney(journey);
                          }}>Registrar costo</button>
                        )}
                      </article>
                    );
                  })}
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

        {authorizationRole === "ADMIN" && (
          <section className="panel catalog-panel">
            <div className="section-heading">
              <div><p className="eyebrow">CONFIGURACIÓN DE FINCA</p><h2>Ingreso de datos</h2></div>
              <span className="step-badge">ADMIN</span>
            </div>
            <p className="muted catalog-intro">
              Crea un lote, registra un producto, asígnalo a un plan y agrega animales para completar una jornada.
            </p>
            {catalogFormNotice && <p className="notice" role="status">{catalogFormNotice}</p>}
            {catalogFormError && <p className="error-message" role="alert">{catalogFormError}</p>}
            <div className="catalog-grid">
              <form onSubmit={submitLot} className="stack catalog-form">
                <h3>1. Crear lote</h3>
                <label>Nombre del lote
                  <input value={newLotName} onChange={(event) => setNewLotName(event.target.value)}
                    maxLength={160} required />
                </label>
                <label>Ubicación
                  <input value={newLotLocation} onChange={(event) => setNewLotLocation(event.target.value)}
                    maxLength={200} placeholder="Finca San Isidro" />
                </label>
                <label>Propósito
                  <input value={newLotPurpose} onChange={(event) => setNewLotPurpose(event.target.value)}
                    maxLength={100} placeholder="Crianza y engorde" />
                </label>
                <button className="button primary" disabled={catalogBusy !== ""}>
                  {catalogBusy === "lote" ? "Guardando…" : "Crear lote"}
                </button>
              </form>

              <form onSubmit={submitProduct} className="stack catalog-form">
                <h3>2. Registrar producto</h3>
                <label>Nombre
                  <input value={newProductName} onChange={(event) => setNewProductName(event.target.value)}
                    maxLength={160} required />
                </label>
                <label>Tipo
                  <input value={newProductType} onChange={(event) => setNewProductType(event.target.value)}
                    maxLength={80} placeholder="Vacuna" required />
                </label>
                <div className="catalog-fields-two">
                  <label>Unidad
                    <input value={newProductUnit} onChange={(event) => setNewProductUnit(event.target.value)}
                      maxLength={30} required />
                  </label>
                  <label>Contenido (ml)
                    <input type="number" min="0.01" step="any" value={newProductContent}
                      onChange={(event) => setNewProductContent(event.target.value)} required />
                  </label>
                </div>
                <div className="catalog-fields-two">
                  <label>Dosis mínima
                    <input type="number" min="0" step="any" value={newProductMinDose}
                      onChange={(event) => setNewProductMinDose(event.target.value)} required />
                  </label>
                  <label>Dosis máxima
                    <input type="number" min="0" step="any" value={newProductMaxDose}
                      onChange={(event) => setNewProductMaxDose(event.target.value)} required />
                  </label>
                </div>
                <label>Precio unitario
                  <input type="number" min="0" step="0.01" value={newProductPrice}
                    onChange={(event) => setNewProductPrice(event.target.value)} required />
                </label>
                <button className="button primary" disabled={catalogBusy !== ""}>
                  {catalogBusy === "producto" ? "Guardando…" : "Crear producto"}
                </button>
              </form>

              <form onSubmit={submitPlan} className="stack catalog-form">
                <h3>3. Crear plan sanitario</h3>
                <label>Nombre del plan
                  <input value={newPlanName} onChange={(event) => setNewPlanName(event.target.value)}
                    maxLength={160} required />
                </label>
                <label>Descripción
                  <textarea rows={2} maxLength={500} value={newPlanDescription}
                    onChange={(event) => setNewPlanDescription(event.target.value)} />
                </label>
                <div className="catalog-fields-two">
                  <label>Edad objetivo (meses)
                    <input type="number" min="0" value={newPlanAge}
                      onChange={(event) => setNewPlanAge(event.target.value)} placeholder="Opcional" />
                  </label>
                  <label>Intervalo mínimo (días)
                    <input type="number" min="0" value={newPlanInterval}
                      onChange={(event) => setNewPlanInterval(event.target.value)} required />
                  </label>
                </div>
                <fieldset className="catalog-product-options">
                  <legend>Productos del plan</legend>
                  {products.length === 0 ? <p className="field-hint">Primero registra un producto.</p> : (
                    products.map((product) => (
                      <label className="check-option" key={product.id}>
                        <input type="checkbox" checked={newPlanProducts.includes(product.id)}
                          onChange={(event) => setNewPlanProducts((current) => event.target.checked
                            ? [...current, product.id]
                            : current.filter((id) => id !== product.id))} />
                        <span><strong>{product.nombre}</strong><small>{product.tipo}</small></span>
                      </label>
                    ))
                  )}
                </fieldset>
                <button className="button primary" disabled={catalogBusy !== "" || products.length === 0}>
                  {catalogBusy === "plan" ? "Guardando…" : "Crear plan"}
                </button>
              </form>

              <form onSubmit={submitAnimal} className="stack catalog-form">
                <h3>4. Agregar animal</h3>
                <label>Lote
                  <select value={newAnimalLot} onChange={(event) => setNewAnimalLot(event.target.value)} required>
                    <option value="">Seleccione un lote</option>
                    {lots.map((lot) => <option value={lot.id} key={lot.id}>{lot.nombre}</option>)}
                  </select>
                </label>
                <div className="catalog-fields-two">
                  <label>Arete
                    <input value={newAnimalTag} onChange={(event) => setNewAnimalTag(event.target.value)}
                      maxLength={80} required />
                  </label>
                  <label>Raza
                    <input value={newAnimalBreed} onChange={(event) => setNewAnimalBreed(event.target.value)}
                      maxLength={100} required />
                  </label>
                </div>
                <div className="catalog-fields-two">
                  <label>Sexo
                    <select value={newAnimalSex} onChange={(event) => setNewAnimalSex(event.target.value)}>
                      <option>Hembra</option><option>Macho</option>
                    </select>
                  </label>
                  <label>Peso (kg)
                    <input type="number" min="0.01" step="any" value={newAnimalWeight}
                      onChange={(event) => setNewAnimalWeight(event.target.value)} required />
                  </label>
                </div>
                <label>Fecha de nacimiento
                  <input type="date" max={localDateString()} value={newAnimalBirthDate}
                    onChange={(event) => setNewAnimalBirthDate(event.target.value)} required />
                </label>
                <button className="button primary" disabled={catalogBusy !== "" || lots.length === 0}>
                  {catalogBusy === "animal" ? "Guardando…" : "Agregar animal"}
                </button>
              </form>
            </div>
          </section>
        )}

        {authorizationRole === "ADMIN" && (
          <section className="panel users-panel">
            <div className="section-heading">
              <div><p className="eyebrow">ACCESO A LA PLATAFORMA</p><h2>Cuentas de usuario</h2></div>
              <span className="step-badge">
                {users.filter((user) => user.rol === "VETERINARIO" && !user.activo).length}
              </span>
            </div>
            <p className="muted">Activa las solicitudes de acceso para veterinarios.</p>
            {usersError && <p className="error-message" role="alert">{usersError}</p>}
            {users.filter((user) => user.rol === "VETERINARIO" && !user.activo).length === 0 ? (
              <div className="empty-state">No hay solicitudes veterinarias pendientes.</div>
            ) : (
              <div className="account-list">
                {users.filter((user) => user.rol === "VETERINARIO" && !user.activo).map((user) => (
                  <article className="account-request" key={user.id}>
                    <span className="account-avatar" aria-hidden="true">
                      {user.nombre.slice(0, 1).toLocaleUpperCase()}
                    </span>
                    <span className="account-request-info">
                      <strong>{user.nombre}</strong>
                      <small>@{user.username} · Veterinario</small>
                    </span>
                    <button className="button primary"
                      onClick={() => void activateVeterinarian(user.id)}
                      disabled={activatingUser === user.id}>
                      {activatingUser === user.id ? "Activando…" : "Aprobar"}
                    </button>
                  </article>
                ))}
              </div>
            )}
          </section>
        )}

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
        {selectedCostJourney && (
          <section className="panel execution-panel">
            <div className="section-heading">
              <div><p className="eyebrow">CIERRE DE JORNADA</p><h2>Costear jornada #{selectedCostJourney.id}</h2></div>
              <button className="button quiet" onClick={() => setSelectedCostJourney(null)} disabled={costBusy}>Cerrar</button>
            </div>
            <p className="muted cost-help">
              El costo de productos se obtiene del inventario consumido durante la ejecución. Ingrese los gastos adicionales.
            </p>
            <form onSubmit={submitJourneyCost} className="stack">
              <div className="cost-input-grid">
                <label>Mano de obra (₡)
                  <input type="number" min="0" step="0.01" value={laborCost}
                    onChange={(event) => setLaborCost(event.target.value)} required />
                </label>
                <label>Transporte (₡)
                  <input type="number" min="0" step="0.01" value={transportCost}
                    onChange={(event) => setTransportCost(event.target.value)} required />
                </label>
                <label>Veterinario (₡)
                  <input type="number" min="0" step="0.01" value={veterinarianCost}
                    onChange={(event) => setVeterinarianCost(event.target.value)} required />
                </label>
                <label>Otros gastos (₡)
                  <input type="number" min="0" step="0.01" value={otherCost}
                    onChange={(event) => setOtherCost(event.target.value)} required />
                </label>
              </div>
              {costError && <p className="error-message" role="alert">{costError}</p>}
              <div className="form-actions">
                <button className="button primary" disabled={costBusy}>
                  {costBusy ? "Registrando costo…" : "Calcular y registrar costo"}
                </button>
                <button type="button" className="button quiet" onClick={() => setSelectedCostJourney(null)}
                  disabled={costBusy}>Cancelar</button>
              </div>
            </form>
          </section>
        )}
      </main>
    </div>
  );
}

function formatCurrency(value: number) {
  return new Intl.NumberFormat("es-CR", { style: "currency", currency: "CRC" }).format(value);
}

export default App;
