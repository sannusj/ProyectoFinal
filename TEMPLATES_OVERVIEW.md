Resumen de vistas (templates) requeridas por controladores

He escaneado los controladores en src/main/java/com/solea/web/controladores y la carpeta de plantillas: src/main/resources/templates.

1) Mapas controlador -> vista (archivo HTML esperado y estado)

- AdminController
  - "admin/index" -> templates/admin/index.html (EXISTE)
  - "admin/usuarios" -> templates/admin/usuarios.html (EXISTE)
  - "admin/usuario-detalle" -> templates/admin/usuario-detalle.html (EXISTE)
  - "admin/pedidos" -> templates/admin/pedidos.html (EXISTE)
  - "admin/pedido-detalle" -> templates/admin/pedido-detalle.html (EXISTE)

- PrendaController
  - "admin/prendas" -> templates/admin/prendas.html (EXISTE)
  - "admin/prenda-form" -> templates/admin/prenda-form.html (EXISTE)

- AuthController
  - "login" -> templates/login.html (EXISTE)
  - "registro" -> templates/registro.html (EXISTE)

- CarritoController
  - "carrito/index" -> templates/carrito/index.html (EXISTE)

- HomeController
  - "home/index" -> templates/home/index.html (EXISTE)
  - "home/inicio" -> templates/home/inicio.html (EXISTE)
  - "home/catalogo" -> templates/home/catalogo.html (EXISTE)
  - "home/contacto" -> templates/home/contacto.html (EXISTE)
  - "home/nosotros" -> templates/home/nosotros.html (EXISTE)
  - "home/detalle" -> templates/home/detalle.html (EXISTE)
  - "home/ayuda" -> templates/home/ayuda.html (EXISTE)

- PedidoController
  - "pedido/paso1" -> templates/pedido/paso1.html (EXISTE)
  - "pedido/paso2" -> templates/pedido/paso2.html (EXISTE)
  - "pedido/paso3" -> templates/pedido/paso3.html (EXISTE)
  - "pedido/resumen" -> templates/pedido/resumen.html (EXISTE)
  - "pedido/confirmado" -> templates/pedido/confirmado.html (EXISTE)

- PerfilController
  - "perfil/perfil" -> templates/perfil/perfil.html (EXISTE)
  - "perfil/editar" -> templates/perfil/editar.html (EXISTE)
  - "perfil/cambiar-pass" -> templates/perfil/cambiar-pass.html (EXISTE)
  - "perfil/mis-pedidos" -> templates/perfil/mis-pedidos.html (EXISTE)

- UsuarioController (sirve imágenes, no templates)
  - endpoints de imágenes -> no aplica (no templates requeridas)

2) Conclusión rápida

- No faltan plantillas HTML requeridas por los controladores: todos los nombres de vista usados por los controladores tienen su fichero HTML correspondiente en src/main/resources/templates.

3) Recomendaciones de organización y mejoras (bajo riesgo)

- Mantener la estructura actual por módulos (admin/, home/, pedido/, perfil/, carrito/, fragments/) — ya está organizada correctamente.
- Añadir un archivo README o este mismo TEMPLATES_OVERVIEW.md (el que acabo de crear) para que futuros colaboradores entiendan dónde agregar vistas.
- Revisar las plantillas para comprobar que usan los atributos de modelo que los controladores proporcionan (por ejemplo: "usuario", "prendas", "productos", "resumen", etc.). Si quieres, puedo inspeccionar cada template y validar que usan correctamente los atributos.
- Opcional: añadir plantillas mínimas "placeholder" para vistas que potencialmente se necesiten en el futuro (por ejemplo, páginas de error personalizadas, 404, 500) — puedo crearlas si lo deseas.

4) Siguientes pasos que puedo hacer ahora (elige uno o varios)

- Validar cada template para asegurar que no faltan variables de Thymeleaf (leer y comparar los modelos desde controladores) y crear/ajustar las plantillas donde falten referencias.
- Crear plantillas placeholder para errores (404.html, 500.html).
- Generar tests ligeros o un script que liste automáticamente las vistas usadas por los controladores y las compare con los archivos en templates.

Si quieres, empiezo a validar plantilla por plantilla y crear ajustes mínimos (por ejemplo, asegurar que en "carrito/index.html" se itera sobre "productos" y muestra "total"), o creo páginas de error por ti. ¿Qué prefieres que haga ahora?

