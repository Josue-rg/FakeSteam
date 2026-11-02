const ENV = process.env.NODE_ENV || "production";

// En producción (Render) se puede definir VITE_BACKEND_URL en el panel de Render.
// Si no existe, se usa la URL por defecto de AWS.
const BACKEND_URLS = {
  development: "http://localhost:5000",
  production:
    import.meta.env.VITE_BACKEND_URL ||
    "https://c9my416xy0.execute-api.us-east-1.amazonaws.com",
};

const URL_BACKEND = BACKEND_URLS[ENV];

export { URL_BACKEND };
