-- =====================================================================
-- Script completo de la base de datos: Sistema de Gestión Hotel Eben-Ezer
-- Motor: PostgreSQL (Supabase)
-- Uso: pegar y ejecutar completo en Supabase -> SQL Editor.
-- Orden: extensión, tablas, funciones, triggers, datos iniciales.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 0. Extensión necesaria para la restricción anti-solapamiento de reservas
--    (permite combinar igualdad de enteros con rangos de fechas en GiST)
-- ---------------------------------------------------------------------
create extension if not exists btree_gist;

-- ---------------------------------------------------------------------
-- 1. TABLAS
-- ---------------------------------------------------------------------

-- Usuarios del sistema (administrador y recepcionista).
-- La columna "clave" guarda el hash de la contraseña, nunca el texto plano.
create table public.usuarios (
  id serial not null,
  nombre character varying(100) not null,
  usuario character varying(30) not null,
  clave character varying(100) not null,
  rol character varying(20) not null,
  constraint usuarios_pkey primary key (id),
  constraint usuarios_usuario_key unique (usuario),
  constraint usuarios_rol_check check (
    (rol)::text = any (
      (array[
        'Administrador'::character varying,
        'Recepcionista'::character varying
      ])::text[]
    )
  )
);

-- Clientes / huéspedes. El DPI es único.
create table public.clientes (
  id serial not null,
  nombre character varying(100) not null,
  dpi character varying(20) not null,
  telefono character varying(20) null,
  correo character varying(100) null,
  constraint clientes_pkey primary key (id),
  constraint clientes_dpi_key unique (dpi)
);

-- Habitaciones con tipo, capacidad y tarifa por noche.
create table public.habitaciones (
  id serial not null,
  numero character varying(10) not null,
  tipo character varying(20) not null,
  capacidad integer not null,
  tarifa_noche numeric(10, 2) not null,
  estado character varying(20) not null default 'Disponible'::character varying,
  constraint habitaciones_pkey primary key (id),
  constraint habitaciones_numero_key unique (numero),
  constraint habitaciones_capacidad_check check ((capacidad > 0)),
  constraint habitaciones_estado_check check (
    (estado)::text = any (
      (array[
        'Disponible'::character varying,
        'Ocupada'::character varying,
        'Mantenimiento'::character varying
      ])::text[]
    )
  ),
  constraint habitaciones_tarifa_noche_check check ((tarifa_noche > (0)::numeric)),
  constraint habitaciones_tipo_check check (
    (tipo)::text = any (
      (array[
        'Sencilla'::character varying,
        'Doble'::character varying,
        'Suite'::character varying
      ])::text[]
    )
  )
);

-- Reservaciones. La fecha de salida se calcula sola (entrada + días).
-- La restricción "sin_solapamiento" impide dos reservas activas
-- (Pendiente, Confirmada o En curso) de la misma habitación en fechas que se cruzan.
create table public.reservas (
  id serial not null,
  id_cliente integer not null,
  id_habitacion integer not null,
  fecha_entrada date not null,
  dias integer not null,
  fecha_salida date generated always as ((fecha_entrada + dias)) stored null,
  total numeric(10, 2) null,
  estado character varying(20) not null default 'Pendiente'::character varying,
  entrada_real timestamp with time zone null,
  salida_real timestamp with time zone null,
  personas integer not null default 1,
  id_usuario integer null,
  constraint reservas_pkey primary key (id),
  constraint fk_reservas_habitacion foreign key (id_habitacion) references habitaciones (id),
  constraint fk_reservas_usuario foreign key (id_usuario) references usuarios (id),
  constraint fk_reservas_cliente foreign key (id_cliente) references clientes (id),
  constraint reservas_id_cliente_fkey foreign key (id_cliente) references clientes (id),
  constraint reservas_id_habitacion_fkey foreign key (id_habitacion) references habitaciones (id),
  constraint reservas_personas_check check ((personas > 0)),
  constraint reservas_estado_check check (
    (estado)::text = any (
      (array[
        'Pendiente'::character varying,
        'Confirmada'::character varying,
        'En curso'::character varying,
        'Finalizada'::character varying,
        'Cancelada'::character varying
      ])::text[]
    )
  ),
  constraint reservas_dias_check check ((dias > 0)),
  constraint sin_solapamiento exclude using gist (
    id_habitacion with =,
    daterange (fecha_entrada, (fecha_entrada + dias)) with &&
  )
  where (
    (estado)::text = any (
      (array[
        'Pendiente'::character varying,
        'Confirmada'::character varying,
        'En curso'::character varying
      ])::text[]
    )
  )
);

-- Historial de cambios (auditoría). Solo se inserta; no se modifica ni se borra.
create table public.historial (
  id bigserial not null,
  fecha timestamp with time zone not null default now(),
  tabla character varying(30) not null,
  operacion character varying(10) not null,
  id_registro integer null,
  usuario_app character varying(50) null,
  datos_anteriores jsonb null,
  datos_nuevos jsonb null,
  constraint historial_pkey primary key (id),
  constraint fk_historial_usuario foreign key (usuario_app) references usuarios (usuario),
  constraint historial_operacion_check check (
    (operacion)::text = any (
      (array[
        'INSERT'::character varying,
        'UPDATE'::character varying,
        'DELETE'::character varying
      ])::text[]
    )
  )
);

-- ---------------------------------------------------------------------
-- 2. FUNCIONES
-- ---------------------------------------------------------------------

-- Registra en "historial" cada INSERT, UPDATE o DELETE de las tablas auditadas.
CREATE OR REPLACE FUNCTION public.registrar_historial()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
declare
  -- Usuario de la aplicación; lo envía el programa Java al conectarse
  v_usuario text := nullif(current_setting('app.usuario', true), '');
  v_viejo jsonb;
  v_nuevo jsonb;
  v_id int;
begin
  if tg_op in ('UPDATE','DELETE') then v_viejo := to_jsonb(old); end if;
  if tg_op in ('INSERT','UPDATE') then v_nuevo := to_jsonb(new); end if;

  -- Nunca se guarda el hash de la contraseña en el historial
  if tg_table_name = 'usuarios' then
    v_viejo := v_viejo - 'clave';
    v_nuevo := v_nuevo - 'clave';
  end if;

  v_id := coalesce((v_nuevo->>'id')::int, (v_viejo->>'id')::int);

  insert into historial
    (tabla, operacion, id_registro, usuario_app, datos_anteriores, datos_nuevos)
  values
    (tg_table_name, tg_op, v_id, v_usuario, v_viejo, v_nuevo);

  if tg_op = 'DELETE' then return old; end if;
  return new;
end;
$function$;

-- Impide modificar o borrar filas del historial (registro inmutable).
-- NOTA: versión reconstruida; si tu base original tiene otra, reemplázala aquí.
CREATE OR REPLACE FUNCTION public.bloquear_cambios_historial()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
begin
  raise exception 'El historial no se puede modificar ni eliminar';
end;
$function$;

-- ---------------------------------------------------------------------
-- 3. TRIGGERS
-- ---------------------------------------------------------------------

create trigger trg_hist_usuarios
after insert or delete or update on usuarios
for each row execute function registrar_historial();

create trigger trg_hist_clientes
after insert or delete or update on clientes
for each row execute function registrar_historial();

create trigger trg_hist_habitaciones
after insert or delete or update on habitaciones
for each row execute function registrar_historial();

create trigger trg_hist_reservas
after insert or delete or update on reservas
for each row execute function registrar_historial();

create trigger trg_hist_inmutable
before delete or update on historial
for each row execute function bloquear_cambios_historial();

-- ---------------------------------------------------------------------
-- 4. DATOS INICIALES
-- ---------------------------------------------------------------------

-- Usuario administrador de prueba: Admin / admin123
-- La clave se guarda como hash SHA-256 en hexadecimal.
insert into usuarios (nombre, usuario, clave, rol)
values (
  'Administrador del sistema',
  'Admin',
  encode(sha256('admin123'::bytea), 'hex'),
  'Administrador'
)
on conflict (usuario) do nothing;

-- Habitaciones de ejemplo (datos ficticios para probar la aplicación)
insert into habitaciones (numero, tipo, capacidad, tarifa_noche, estado) values
  ('101', 'Sencilla', 1, 150.00, 'Disponible'),
  ('102', 'Doble',    2, 250.00, 'Disponible'),
  ('201', 'Suite',    4, 500.00, 'Disponible')
on conflict (numero) do nothing;
