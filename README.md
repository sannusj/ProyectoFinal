# Solea - Tienda Virtual

Proyecto Spring Boot (Java) que implementa una tienda online sencilla llamada "Solea". Usa Thymeleaf para las vistas, JPA/Hibernate para persistencia y Spring Security para autenticación básica.

Este README es la versión final y presentable para el repositorio: describe rápidamente el proyecto, cómo ejecutarlo, los endpoints principales, los modelos/entidades más relevantes y notas útiles para desarrolladores.

---

## Contenido

- Resumen
- Requisitos
- Ejecución local
- Endpoints principales (rutas y propósito)
- Modelos / Entidades (resumen)
- Servicios y repositorios (resumen)
- OpenAPI / Swagger
- Archivos importantes y estructura
- Pruebas rápidas y verificación
- Contacto y licencia

---

## Resumen

Solea es una aplicación web de comercio electrónico con funcionalidades básicas:

- Catálogo de productos (prendas) con imágenes, descripción, precio y stock.
- Carrito de compras por usuario autenticado (añadir, actualizar, eliminar, vaciar).
- Flujo de pedido multi-paso (datos de envío, pago, confirmación) y almacenamiento de pedidos.
- Área de administración con gestión de usuarios, prendas y pedidos.
- Almacenamiento híbrido de imágenes: prioriza archivos en disco (imagePath) y hace fallback a BLOB en la base de datos.

Tecnologías principales: Java 21, Spring Boot, Spring MVC, Spring Data JPA, Thymeleaf, Spring Security, Maven.

---

## Requisitos

- JDK 21
- Maven (se incluye `mvnw`/`mvnw.cmd`) o Maven instalado globalmente
- Base de datos (por defecto MySQL, conexión configurable en `src/main/resources/application.properties`)

---

## Cómo ejecutar (local)

Desde PowerShell en la raíz del proyecto:

```powershell
# Compilar (sin tests)
./mvnw clean package -DskipTests

# Ejecutar el JAR (ejemplo puerto 8081)
java -jar target/solea-*.jar --server.port=8081
```

Alternativamente ejecutar desde el IDE (Run / Spring Boot).

Notas:
- Configura la conexión a la base de datos en `src/main/resources/application.properties` antes de ejecutar en un entorno nuevo.
- Los logs se almacenan en `logs/solea.log`.

---

## Endpoints principales (resumen)

Rutas importantes y su propósito (resumen, no exhaustivo):

- `/` — Página principal (catálogo / home).
- `/catalogo` — Listado de productos.
- `/detalle/{id}` — Detalle de prenda.
- `/carrito` — Carrito del usuario (GET/POST/acciones: agregar/actualizar/eliminar/vaciar).
- `/pedido/*` — Flujo de pedido (paso1/paso2/paso3, resumen, confirmar).
- `/auth/*` — Autenticación y registro.
- `/admin/*` — Panel de administración (usuarios, pedidos, prendas).
- `/imagenes/{id}` — Endpoint REST que sirve la imagen de una prenda (prioriza imagePath, fallback a BLOB).
- `/v3/api-docs` y `/swagger-ui.html` (si está configurado) — OpenAPI / Swagger UI.

Para detalles por método y parámetros consulta los controladores en `src/main/java/com/solea/web/controladores`.

---

## Modelos / Entidades (resumen)

Entidades principales y campos más relevantes:

- Usuario
  - id, nombre, email, pass, tel, pais, rol (ENUM), avatar (BLOB), provider, oauthId

- Prenda
  - id, nombre, precio, stock, alta (activo), imagenPrenda (BLOB), imagePath (ruta en disco), descripcion, talla, categoria

- Categoria
  - id, nombre, lista de prendas

- Carrito
  - id, usuario (uno-a-uno), productosCarrito (lista de ProductoCarrito)

- ProductoCarrito
  - id, prenda, carrito, cantidad

- Pedido
  - id, usuario, productos (lista de ProductoPedido), datos de envío/pago, total, estado, fechas

- ProductoPedido
  - id, pedido, prenda, cantidad, precioUnitario, subtotal

- PedidoTemp
  - id, usuario, campos temporales usados en el flujo multi-paso (envío/pago/observaciones)

> Nota: los detalles completos de campos, relaciones y constraints están en el código fuente bajo `src/main/java/com/solea/web/model`.

---

## Servicios y repositorios (resumen)

Servicios principales (interfaces + implementaciones) manejan la lógica de negocio:

- `ServicioCarrito` / `ServicioCarritoImpl` — gestión del carrito.
- `ServicioPrendas` — CRUD y gestión de imágenes de prendas.
- `ServicioPedidos` — flujo multipart y confirmación de pedidos.
- `ServicioUsuarios` — gestión de usuarios y autenticación.
- `ServicioContactos` — procesamiento del formulario de contacto (escribe en `uploads/contactos.txt`).

Repositorios Spring Data JPA en `src/main/java/com/solea/web/repositorios` proveen métodos para consultas comunes (ej. `findByCarrito_Id`, `findByUsuario_Id`, búsquedas por nombre/categoría, etc.).

---

## OpenAPI / Swagger

El proyecto incluye anotaciones OpenAPI en controladores. Si la dependencia de Swagger/OpenAPI está presente, podrás acceder a la especificación en `/v3/api-docs` y a la interfaz Swagger UI en `/swagger-ui.html`.

---

## Archivos y estructura importantes

- `src/main/java/com/solea/web/` — código fuente (controladores, servicios, repositorios, modelos, config)
- `src/main/resources/templates/` — plantillas Thymeleaf (vistas)
- `src/main/resources/static/` — recursos estáticos (CSS, JS, imágenes de ejemplo)
- `uploads/` — carpeta donde se guardan archivos subidos (imágenes, contactos.txt)
- `logs/solea.log` — archivo de logs
- `diagram.puml`, `schema.sql`, `ENTITIES_SUMMARY.md` — documentos generados con el modelo ER y esquema (si están presentes en la raíz del repo)

---

## Pruebas rápidas / Verificación

- Verificar que la app arranca y la página `/` responde.
- Iniciar sesión con un usuario y comprobar que `/carrito` muestra productos añadidos.
- Probar subida y visualización de imágenes: al crear/editar una prenda se puede subir una imagen o indicar `imagePath`; la URL `/imagenes/{id}` debe devolver la imagen.
- Revisar `logs/solea.log` si ocurre un error y consultar trazas.

---

## Contribuir / Desarrollo

- Clona el repositorio, crea una rama para la tarea (`feature/...` o `fix/...`) y envía un Pull Request.
- Mantén las dependencias en `pom.xml` actualizadas con versiones compatibles con Spring Boot en uso.
- Añade comentarios y tests unitarios cuando modifiques lógica crítica (servicios, repositorios, controladores).

---

## Contacto y licencia

Este proyecto fue desarrollado como ejemplo/ejercicio. Para preguntas o colaboración abre un issue en el repositorio.

---

Gracias por revisar el proyecto. Si quieres que añada diagramas (PlantUML), migraciones (Flyway/Liquibase) o un `openapi.yaml` exportado, dímelo y lo agrego como archivos separados.
