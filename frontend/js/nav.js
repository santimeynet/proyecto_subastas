function sesionActiva() {
    return !!getToken() && !!usuarioActual();
}

function renderNavbar(navElement) {
    if (!navElement) return;

    const user = usuarioActual();
    const autenticado = sesionActiva();
    let html = '';

    if (autenticado && user) {
        html += `<span class="nav-user">Hola, ${user.nombre}</span>`;
        html += `<a href="subastas.html">Subastas</a>`;
        if (tieneRol('SELLER')) {
            html += `<a href="vendedor.html">Vendedor</a>`;
        }
        if (tieneRol('ADMIN')) {
            html += `<a href="admin.html">Admin</a>`;
        }
        html += `<div class="notif-wrapper">
            <button type="button" class="notif-btn" id="notifBtn" aria-label="Notificaciones">
                🔔 <span class="notif-badge" id="notifBadge" style="display:none">0</span>
            </button>
            <div class="notif-panel" id="notifPanel" style="display:none">
                <div class="notif-panel-header">Notificaciones</div>
                <div class="notif-list" id="notifList"></div>
            </div>
        </div>`;
        html += `<a href="#" onclick="cerrarSesion(); return false;">Salir</a>`;
    } else {
        html += `<a href="login.html">Iniciar Sesión</a>`;
        html += `<a href="register.html">Registro</a>`;
    }

    navElement.innerHTML = html;

    if (autenticado) {
        initNotificaciones();
    }
}

function ocultarLoginSiAutenticado() {
    const btn = document.getElementById('btnLoginHero');
    if (btn && sesionActiva()) {
        btn.style.display = 'none';
    }
}

function initNotificaciones() {
    const btn = document.getElementById('notifBtn');
    const panel = document.getElementById('notifPanel');
    if (!btn || !panel) return;

    btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const visible = panel.style.display !== 'none';
        panel.style.display = visible ? 'none' : 'block';
        if (!visible) cargarNotificaciones();
    });

    document.addEventListener('click', (e) => {
        if (!panel.contains(e.target) && e.target !== btn) {
            panel.style.display = 'none';
        }
    });

    cargarNotificaciones();
}

async function cargarNotificaciones() {
    const list = document.getElementById('notifList');
    const badge = document.getElementById('notifBadge');
    if (!list) return;

    try {
        const notificaciones = await api.notificaciones();
        const noLeidas = notificaciones.filter(n => !n.leida).length;

        if (badge) {
            if (noLeidas > 0) {
                badge.textContent = noLeidas > 99 ? '99+' : noLeidas;
                badge.style.display = 'inline-block';
            } else {
                badge.style.display = 'none';
            }
        }

        if (!notificaciones.length) {
            list.innerHTML = '<p class="notif-empty">Sin notificaciones.</p>';
            return;
        }

        list.innerHTML = notificaciones.map(n => `
            <div class="notif-item ${n.leida ? 'leida' : ''}" data-id="${n.id}">
                <p class="notif-msg">${n.mensaje}</p>
                <small>${new Date(n.creadoEn).toLocaleString()} UTC</small>
                ${!n.leida ? `<button type="button" class="notif-mark-read" onclick="marcarLeida(${n.id})">Marcar leída</button>` : ''}
            </div>
        `).join('');
    } catch (ex) {
        list.innerHTML = `<p class="notif-empty">${ex.message}</p>`;
    }
}

async function marcarLeida(id) {
    try {
        await api.marcarNotificacionLeida(id);
        await cargarNotificaciones();
    } catch (ex) {
        alert(ex.message);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const nav = document.getElementById('nav');
    if (nav) renderNavbar(nav);
    ocultarLoginSiAutenticado();
});
