# Solea - Tienda Virtual (Resumen del proyecto)

Este README contiene un inventario completo y documentado del código fuente actual del proyecto, incluyendo controladores, servicios, repositorios y modelos, además de instrucciones rápidas para ejecutar y comprobar puntos sensibles (Swagger / OpenAPI, formulario de contacto, logs).

---

## Índice

1. Resumen rápido
2. Cómo ejecutar el proyecto
3. Endpoints públicos / REST (controladores y métodos)
4. Servicios (interfaces e implementaciones) y sus métodos
5. Repositorios y métodos principales
6. Modelos (entidades) y sus campos/métodos
7. Configuración de OpenAPI / Swagger
8. Formulario de contacto: funcionamiento y ubicación del archivo
9. Logs y depuración
10. Notas y recomendaciones

---

## 1) Resumen rápido

- Proyecto Spring Boot (Java 21) con Thymeleaf para frontend y JPA/Hibernate para persistencia.
- Estructura principal bajo `src/main/java/com/solea/web/`.
- Swagger/OpenAPI metadata expuesto por `OpenApiConfig`.
- El formulario de contacto escribe en un archivo de texto en `uploads/contactos.txt` (implementado por `ServicioContactosImpl`).

---

## 2) Cómo ejecutar el proyecto (local)

Requisitos:
- JDK 21
- Maven wrapper incluido (`mvnw` / `mvnw.cmd`)
- Base de datos MySQL accesible según `application.properties` (por defecto jdbc:mysql://localhost:3306/soleadb)

Comandos básicos (Windows PowerShell):

```powershell
# limpiar y empaquetar
./mvnw clean package -DskipTests

# ejecutar el JAR generado en target
java -jar target/solea-tienda-virtual-0.0.1-SNAPSHOT.jar --server.port=8081
```

Notas:
- Si necesitas cambiar el puerto, usa `--server.port=XXXX`.
- Los logs se guardan en `logs/solea.log`.

---

## 3) Endpoints públicos / REST (controladores y métodos)

A continuación se listan los controladores con métodos públicos (ruta, firma y descripción breve).

- `ContactoController` (`/contacto`)
  - POST `/contacto/enviar` enviar(nombre, email, asunto, mensaje, Model) — Procesa el formulario de contacto y guarda la entrada mediante `ServicioContactos.guardar`.

- `ImagenController`
  - GET `/imagenes/{id}` imagenPrenda(id) — Devuelve la imagen de una prenda (prioriza `imagePath` en disco; fallback a `imagenPrenda` en BD).

- `HomeController`
  - GET `/` home(Model) — Página principal.
  - GET `/inicio` inicioUsuario(Model) — Dashboard (requiere login).
  - GET `/catalogo` catalogo(Model, cat?, nombre?) — Catálogo con filtros.
  - GET `/contacto` contacto(Model) — Página contact.
  - GET `/detalle/{id}` detalle(id, Model) — Detalle de prenda.
  - GET `/nosotros`, GET `/ayuda`

- `PedidoController` (`/pedido`)
  - GET `/pedido/paso1` mostrarPaso1(Model)
  - POST `/pedido/paso1` procesarPaso1(nombre, direccion, provincia)
  - GET `/pedido/paso2` mostrarPaso2(Model)
  - POST `/pedido/paso2` procesarPaso2(titular, numero, tipoTarjeta)
  - GET `/pedido/paso3` mostrarPaso3(Model)
  - POST `/pedido/paso3` procesarPaso3(regalo?, observaciones?)
  - GET `/pedido/resumen` resumen(Model)
  - POST `/pedido/confirmar` confirmarPedido(Model)
  - GET `/pedido/pdf` descargarPdf() — actualmente redirige (no implementado).

- `CarritoController` (`/carrito`)
  - GET `/carrito` verCarrito(Model)
  - POST `/carrito/agregar` agregarProducto(prendaId, cantidad)
  - POST `/carrito/actualizar` actualizarCantidad(prendaId, cantidad)
  - POST `/carrito/eliminar` eliminarProducto(prendaId)
  - POST `/carrito/vaciar` vaciarCarrito()
  - GET `/carrito/checkout` irAlCheckout() — redirige a `/pedido/paso1`.

- `AuthController` (`/auth`)
  - GET `/auth/login` login()
  - GET `/auth/registro` registro(Model, request)
  - POST `/auth/registro` procesarRegistro(Usuario, Model, request)

- `AdminController` (`/admin`)
  - GET `/admin` adminHome()
  - GET `/admin/usuarios` listarUsuarios(Model)
  - GET `/admin/usuarios/{id}` detalleUsuario(id, Model)
  - POST `/admin/usuarios/{id}/cambiar-rol` cambiarRol(id, nuevoRol, Model)
  - GET `/admin/pedidos` listarPedidos(Model)
  - GET `/admin/pedidos/{id}` verPedido(id, Model)
  - POST `/admin/pedidos/{id}/estado` cambiarEstadoPedido(id, estado)
  - GET `/admin/perfil` perfilAdminRedirect() — redirige a `/perfil`.

- `PerfilController` (`/perfil`)
  - GET `/perfil` perfil(Model)
  - GET `/perfil/editar` editarForm(Model)
  - POST `/perfil/editar` guardarEdicion(nombre, telefono, pais, Model)
  - GET `/perfil/password` cambiarPassForm(Model)
  - POST `/perfil/password` cambiarPass(actual, nueva, repetir, Model)
  - GET `/perfil/mis-pedidos` misPedidos(Model)

- `UsuarioController`
  - GET `/usuario/avatar/{id}` avatar(id) — Devuelve avatar binario del usuario.

- `PrendaController` (admin - `/admin/prendas`)
  - GET `/admin/prendas` listar(Model)
  - GET `/admin/prendas/nueva` nueva(Model)
  - POST `/admin/prendas/guardar` guardar(Prenda, categoriaId?, imagenFile)
  - GET `/admin/prendas/{id}/editar` editar(id, Model)
  - POST `/admin/prendas/{id}/editar` editarGuardar(...) — actualizar prenda y opcional imagen
  - POST `/admin/prendas/{id}/eliminar` eliminar(id)

---

## 4) Servicios (interfaces e implementaciones)

- `ServicioContactos` (interface)
  - `void guardar(Contacto c)` — Implementado por `ServicioContactosImpl` que escribe en `uploads/contactos.txt`.

- `ServicioUsuarios` (interface) — Implementación `ServicioUsuarioImpl` con métodos como:
  - `void registarUsuario(Usuario u)`
  - `Usuario obtenerUserPorMailYpass(String email, String pass)`
  - `Usuario obtenerUserPorEmail(String email)`
  - `Usuario obtenerUserPorId(int id)`
  - `List<Usuario> obtenerUsuarios()`
  - `void actualizarDatos(Integer id, String nombreUsuario, String pass, String telefono, String pais)`
  - `UsuarioDetalleResponse nativeObtenerUserPorId(int id)`
  - `void guardarCambiosUsuario(Usuario usuarioEditar)`
  - `void cambiarRolUsuario(Integer id, Rol nuevoRol)`
  - `boolean esUltimoAdmin(Integer idUsuario)`
  - `Usuario processOAuthPostLogin(String providerName, Map<String,Object> attributes)`

- `ServicioPrendas` / `ServicioPrendasImpl` — CRUD y gestión de imágenes.
- `ServicioPedidos` / `ServicioPedidosImpl` — Procesos multi-paso del pedido y confirmación.
- `ServicioCarrito` / `ServicioCarritoImpl` — Agregar/actualizar/eliminar/vaciar carrito.
- `ServicioCategorias` — gestionar categorías (interface presente; implementación no detallada en este README).

---

## 5) Repositorios (Spring Data JPA)

- `UsuarioRepository` — findByEmail, findByRol, countByRol, findByProviderAndOauthId, findByProviderAndEmail
- `PrendaRepository` — findAllWithCategoria, findByIdWithCategoria, búsquedas por nombre y categoría
- `PedidoRepository` — findByUsuario_IdOrderByIdDesc, findActivosPorUsuario, findByIdWithProductosAndPrendas
- `ProductoCarritoRepository` — findByCarrito_Id, findByCarrito_IdAndPrenda_Id, deleteByCarrito_IdAndPrenda_Id, deleteByCarrito_Id
- `PedidoTempRepository` — findByUsuario_Id, deleteByUsuario_Id
- `CarritoRepository` — findByUsuario_Id

---

## 6) Modelos / Entidades (campos y getters/setters principales)

Documentación resumida de las entidades centrales:

- `Contacto` (no es entidad JPA; POJO):
  - Campos: nombre, email, asunto, mensaje, fecha
  - Métodos: getters/setters, constructor y `toString()` formateado para archivo

- `Usuario` (entidad JPA)
  - Campos: id, nombre, email, pass, tel, pais, rol (enum), avatar, provider (enum), oauthId
  - Métodos: getters y setters estándar

- `Prenda` (entidad JPA)
  - Campos: id, nombre, precio, stock, alta, imagenPrenda (LOB), categoria, descripcion, talla, imagePath
  - Métodos: getters/setters (setPrecio y setStock normalizan valores no-negativos)

- `Pedido` (entidad JPA)
  - Campos: id, usuario, productos (ProductoPedido), datos del paso1/paso2/paso3, total, estado (enum), fechas
  - Métodos: getters/setters, preUpdate para fechaActualizacion

(Otras entidades: `ProductoPedido`, `ProductoCarrito`, `Carrito`, `Categoria`, `PedidoTemp`, `Rol`, `UsuarioDetalleResponse` están en el código; si quieres las detallo explícitamente, puedo añadirlas en una sección extra).

---

## Entidades restantes (detalladas)

A continuación se describen las entidades que quedaron pendientes en la sección 6. Incluyo campos principales, métodos (getters/setters) y notas de uso observadas en el código.

- `ProductoPedido` (entidad JPA)
  - Campos principales:
    - `Integer id`
    - `Pedido pedido` (relación ManyToOne)
    - `Prenda prenda` (relación ManyToOne)
    - `int cantidad`
    - `Double precioUnitario`
    - `Double subtotal`
  - Métodos:
    - Getters y setters estándar (`getId`, `getPedido`, `setPedido`, `getPrenda`, `setPrenda`, `getCantidad`, `setCantidad`, `getPrecioUnitario`, `setPrecioUnitario`, `getSubtotal`, `setSubtotal`).
    - `calcularSubtotal()` — método que calcula `subtotal = precioUnitario * cantidad` (usado en `ServicioPedidosImpl`).
  - Notas: instanciada cuando se transforma `ProductoCarrito` en `ProductoPedido` durante la confirmación del pedido.

- `ProductoCarrito` (entidad JPA)
  - Campos principales:
    - `Integer id`
    - `Prenda prenda` (ManyToOne)
    - `Carrito carrito` (ManyToOne)
    - `int cantidad`
  - Métodos:
    - Getters/setters estándar y posiblemente un constructor de conveniencia `ProductoCarrito(Prenda prenda, Carrito carrito, int cantidad)` (empleado por `ServicioCarritoImpl`).
  - Notas: repositorio `ProductoCarritoRepository` ofrece búsqueda por carrito y prenda.

- `Carrito` (entidad JPA)
  - Campos principales:
    - `Integer id`
    - `Usuario usuario` (OneToOne o ManyToOne dependiendo de diseño)
    - `List<ProductoCarrito> productosCarrito`
  - Métodos:
    - Getters/setters (`getId`, `getUsuario`, `setUsuario`, `getProductosCarrito`, `setProductosCarrito`).
    - `addProducto(ProductoCarrito pc)` — método de conveniencia para añadir un `ProductoCarrito` a la lista (usado por `ServicioCarritoImpl`).
  - Notas: creado automáticamente si no existe al agregar un producto al carrito.

- `Categoria` (entidad JPA)
  - Campos principales:
    - `Integer id`
    - `String nombre`
  - Métodos:
    - Getters/setters (`getId`, `getNombre`, `setNombre`), y un constructor `Categoria(String nombre)` usado para inicializar categorías por defecto en `PrendaController`.

- `PedidoTemp` (entidad JPA para el flujo multipaso)
  - Campos principales:
    - `Integer id`
    - `Usuario usuario` (ManyToOne)
    - `String nombre` (paso1)
    - `String direccion` (paso1)
    - `String provincia` (paso1)
    - `String titularTarjeta` (paso2)
    - `String numeroTarjeta` (paso2)
    - `String tipoTarjeta` (paso2)
    - `String paraRegalo` (paso3, valores "si"/"no")
    - `String observaciones` (paso3)
  - Métodos:
    - Getters/setters estándar (getNombre, setNombre, getDireccion, setDireccion, etc.).
  - Notas: `ServicioPedidosImpl` crea/actualiza `PedidoTemp` durante los pasos y lo borra en `confirmarPedido`.

- `Rol` (enum)
  - Valores esperados:
    - `ADMIN`
    - `USER`
  - Uso: control de acceso y lógica de interfaz (por ejemplo: evitar que administradores usen el carrito). Repositorio `UsuarioRepository` puede consultar por `Rol`.

- `UsuarioDetalleResponse` (DTO para respuestas web)
  - Campos principales:
    - `Integer id`
    - `String nombre`
    - `String email`
    - `String tel`
    - `String pais`
    - `String rol` (nombre del rol)
  - Métodos: getters/setters estándar.
  - Uso: devuelto por `ServicioUsuarios.nativeObtenerUserPorId(int)` para respuestas API ligeras.

---

## 7) OpenAPI / Swagger

- Metadatos en `OpenApiConfig` (`com.solea.web.config.OpenApiConfig`): expone título, versión y descripción mediante la anotación `@OpenAPIDefinition`.
- Endpoint OpenAPI JSON por defecto (si `springdoc` está correctamente configurado): `GET /v3/api-docs`.
- Interfaz Swagger-UI (si `springdoc-openapi-starter-webmvc-ui` está en classpath): `GET /swagger-ui.html` o `/swagger-ui/index.html`.

Nota importante: en este proyecto ya se hicieron ajustes sobre dependencias `springdoc` para resolver conflictos de versiones. Si ves errores tipo `NoSuchMethodError` relacionados con `ControllerAdviceBean` al cargar `/v3/api-docs`, suele ser un conflicto de versiones entre `springdoc` y `spring-web` — la solución es alinear la versión de `springdoc` con la de Spring Boot / Spring Web usada. Si quieres, reviso y aseguro la versión exacta y hago un ajuste mínimo en `pom.xml`.

---

## 8) Formulario de contacto (comportamiento)

- HTML frontend: plantilla `src/main/resources/templates/home/contacto.html` (vista) presenta un formulario que `POST`s a `/contacto/enviar`.
- Procesamiento: `ContactoController.enviar(...)` crea `Contacto` con `LocalDateTime.now()` y llama `servicioContactos.guardar(c)`.
- Persistencia: `ServicioContactosImpl.guardar(Contacto)` escribe en un archivo de texto `contactos.txt` dentro del directorio `uploads` (por defecto). El valor de la propiedad `app.upload.dir` controla la carpeta; por defecto `uploads`.
- Ubicación de salida: si `app.upload.dir` = `uploads` (por defecto), el archivo final será `uploads/contactos.txt`. Si `app.upload.dir` incluye subcarpeta (ej `uploads/prendas`), la implementación ajusta la ruta para crear el archivo `uploads/contactos.txt` en el padre.

---

## 9) Logs y depuración

- Archivo de logs: `logs/solea.log` (contiene excepciones y trazas). Si hay errores al abrir `/v3/api-docs` o Swagger, revisa este archivo para encontrar `NoSuchMethodError` o conflictos de versión.
- Si la app devuelve HTML 500 en `/v3/api-docs`, busca en `solea.log` las líneas con `springdoc` o `NoSuchMethodError` para diagnosticar versión incompatible.

---

## 10) Notas y recomendaciones

- No modifiqué la lógica existente; este README es sólo documentación.
- Si quieres que genere la documentación OpenAPI (archivo YAML/JSON) directamente a partir de las rutas existentes, puedo:
  1) Añadir anotaciones `@Operation`, `@Parameter` en controladores (cambio de código), o
  2) Generar un archivo manual `openapi.yaml` basado en los controladores leídos (sin tocar código).

- Si prefieres que arregle los problemas con Swagger/springdoc (versiones), puedo aplicar el cambio mínimo en `pom.xml` para alinear versiones y reconstruiar; en el pasado reciente se detectó un conflicto entre `springdoc` 2.2.0 y la versión de `spring-web` empaquetada.

---

Si quieres, actualizo el README con:
- Detalle completo de las entidades restantes (`ProductoPedido`, `ProductoCarrito`, `Carrito`, `Categoria`, `PedidoTemp`, `Rol`, `UsuarioDetalleResponse`).
- Un `openapi.yaml` derivado automáticamente a partir de los controladores (sin modificar código).
- Ajustes mínimos en `pom.xml` para dejar Swagger funcionando sin errores (con pruebas de `/v3/api-docs`).

Indícame cuál de los tres prefieres y procedo.
