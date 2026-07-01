CREATE TABLE roles (
    id   BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE usuarios (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre        VARCHAR(150) NOT NULL,
    bloqueado     BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE usuario_roles (
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    rol_id     BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, rol_id)
);

CREATE TABLE categorias (
    id     BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE productos (
    id           BIGSERIAL PRIMARY KEY,
    titulo       VARCHAR(200) NOT NULL,
    descripcion  TEXT,
    imagen_url   VARCHAR(500),
    vendedor_id  BIGINT NOT NULL REFERENCES usuarios(id),
    creado_en    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE subastas (
    id                 BIGSERIAL PRIMARY KEY,
    producto_id        BIGINT NOT NULL UNIQUE REFERENCES productos(id),
    categoria_id       BIGINT NOT NULL REFERENCES categorias(id),
    vendedor_id        BIGINT NOT NULL REFERENCES usuarios(id),
    precio_base        NUMERIC(19, 2) NOT NULL,
    incremento_minimo  NUMERIC(19, 2) NOT NULL,
    monto_actual       NUMERIC(19, 2) NOT NULL,
    precio_final       NUMERIC(19, 2),
    inicio_utc         TIMESTAMPTZ NOT NULL,
    cierre_utc         TIMESTAMPTZ NOT NULL,
    estado             VARCHAR(30) NOT NULL,
    descripcion        TEXT,
    ganador_id         BIGINT REFERENCES usuarios(id),
    fecha_adjudicacion TIMESTAMPTZ,
    version            BIGINT NOT NULL DEFAULT 0,
    creado_en          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE pujas (
    id          BIGSERIAL PRIMARY KEY,
    subasta_id  BIGINT NOT NULL REFERENCES subastas(id),
    usuario_id  BIGINT NOT NULL REFERENCES usuarios(id),
    monto       NUMERIC(19, 2) NOT NULL,
    confirmada  BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pujas_subasta ON pujas(subasta_id, creado_en DESC);

CREATE TABLE historial_estados (
    id             BIGSERIAL PRIMARY KEY,
    subasta_id     BIGINT NOT NULL REFERENCES subastas(id),
    estado_anterior VARCHAR(30),
    estado_nuevo   VARCHAR(30) NOT NULL,
    usuario_id     BIGINT REFERENCES usuarios(id),
    motivo         TEXT,
    creado_en      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE notificaciones (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT NOT NULL REFERENCES usuarios(id),
    tipo        VARCHAR(50) NOT NULL,
    mensaje     TEXT NOT NULL,
    leida       BOOLEAN NOT NULL DEFAULT FALSE,
    subasta_id  BIGINT REFERENCES subastas(id),
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE disputas (
    id              BIGSERIAL PRIMARY KEY,
    subasta_id      BIGINT NOT NULL UNIQUE REFERENCES subastas(id),
    iniciador_id    BIGINT NOT NULL REFERENCES usuarios(id),
    motivo          VARCHAR(200) NOT NULL,
    descripcion     TEXT NOT NULL,
    resolucion      TEXT,
    admin_id        BIGINT REFERENCES usuarios(id),
    estado_final    VARCHAR(30),
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resuelta_en     TIMESTAMPTZ
);

CREATE INDEX idx_subastas_estado ON subastas(estado);
CREATE INDEX idx_subastas_cierre ON subastas(cierre_utc);
