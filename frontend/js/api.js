const API_BASE = 'http://localhost:8080/api';

function getToken() {
    return localStorage.getItem('token');
}

function getAuthHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;
    return headers;
}

function parseApiError(data, status) {
    if (data.detail) return data.detail;
    if (data.title && data.errors) {
        const fields = Object.entries(data.errors).map(([k, v]) => `${k}: ${v}`).join('; ');
        return `${data.title}. ${fields}`;
    }
    if (data.error) return data.error;
    if (data.message) return data.message;
    return `Error en la solicitud (${status})`;
}

function showError(elementId, message) {
    const el = document.getElementById(elementId);
    if (!el) {
        alert(message);
        return;
    }
    el.textContent = message;
    el.style.display = 'block';
}

function hideError(elementId) {
    const el = document.getElementById(elementId);
    if (el) el.style.display = 'none';
}

async function apiRequest(path, options = {}) {
    const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers: { ...getAuthHeaders(), ...options.headers }
    });
    const contentType = response.headers.get('content-type') || '';
    const data = contentType.includes('json')
        ? await response.json().catch(() => ({}))
        : {};
    if (!response.ok) {
        throw new Error(parseApiError(data, response.status));
    }
    return data;
}

const api = {
    register: (body) => apiRequest('/auth/register', { method: 'POST', body: JSON.stringify(body) }),
    login: (body) => apiRequest('/auth/login', { method: 'POST', body: JSON.stringify(body) }),
    me: () => apiRequest('/auth/me'),
    categorias: () => apiRequest('/categorias'),
    productos: () => apiRequest('/productos/mis-productos'),
    crearProducto: (body) => apiRequest('/productos', { method: 'POST', body: JSON.stringify(body) }),
    subastas: (estado) => apiRequest(`/subastas${estado ? '?estado=' + estado : ''}`),
    subasta: (id) => apiRequest(`/subastas/${id}`),
    crearSubasta: (body) => apiRequest('/subastas', { method: 'POST', body: JSON.stringify(body) }),
    publicarSubasta: (id) => apiRequest(`/subastas/${id}/publicar`, { method: 'POST' }),
    cancelarSubasta: (id, motivo) => apiRequest(`/subastas/${id}/cancelar`, {
        method: 'POST', body: JSON.stringify({ motivo })
    }),
    pujar: (id, monto, version) => apiRequest(`/subastas/${id}/pujas`, {
        method: 'POST', body: JSON.stringify({ monto, version })
    }),
    pujasSubasta: (id) => apiRequest(`/subastas/${id}/pujas`),
    misPujas: () => apiRequest('/mis-pujas'),
    notificaciones: () => apiRequest('/notificaciones'),
    marcarNotificacionLeida: (id) => apiRequest(`/notificaciones/${id}/leer`, { method: 'PATCH' }),
    abrirDisputa: (id, body) => apiRequest(`/subastas/${id}/disputas`, {
        method: 'POST', body: JSON.stringify(body)
    }),
    disputasPendientes: () => apiRequest('/admin/disputas'),
    resolverDisputa: (id, body) => apiRequest(`/admin/disputas/${id}/resolver`, {
        method: 'PATCH', body: JSON.stringify(body)
    }),
    usuarios: () => apiRequest('/admin/usuarios'),
    bloquearUsuario: (id, bloqueado) => apiRequest(`/admin/usuarios/${id}/bloquear`, {
        method: 'PATCH', body: JSON.stringify({ bloqueado })
    })
};

function guardarSesion(authResponse) {
    localStorage.setItem('token', authResponse.token);
    localStorage.setItem('user', JSON.stringify(authResponse));
}

function cerrarSesion() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = 'login.html';
}

function usuarioActual() {
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
}

function requiereAuth() {
    if (!getToken()) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

function tieneRol(rol) {
    const user = usuarioActual();
    return user && user.roles && user.roles.includes(rol);
}
