<div align="center">

# ⚔️ AdvancedProtection

### 🛡️ The Ultimate Land Protection Plugin for Minecraft

[![Spigot](https://img.shields.io/badge/Spigot-1.19.4+-orange.svg?style=for-the-badge&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAACXBIWXMAAAsTAAALEwEAmpwYAAAA+0lEQVQokZ2SzQ3CMAyFn1sGYATuDNBV2r8NIEiRkJCAFbo1SoEruJUBYATEdYcQ7YJR4iMqUi0lH+y8Z78kRIRtGJERR6YUY+u8mxHj4/kSnElrcADkAPYB7AG4IGI0OBDRlIjmAE5rcAiArFE9EcWFiIg4J6IKAGZ/YK/nRGQRsdQYLf/YaVFG5OUGuASw1dqk81GCIClFjS6rMNNaJCKqAsiyZ3l3d5Nmg4C6mQEIXBJqZg7g/NOX5Iv7qKobOeeaAPYArJnZ/Q2oP38tAPMvIc0PEbFYW3WfmZ+Z+YmZ64ioqN7l6LIK57/+GprjGOb8B/X+ATw/hfv9GwB0AAAAAElFTkSuQmCC)](https://www.spigotmc.org/resources/advancedprotection.130494/)
[![Version](https://img.shields.io/badge/version-1.5-blue.svg?style=for-the-badge)](https://github.com/tomas2193xd-arch/AdvancedProtection/releases/latest)
[![Java](https://img.shields.io/badge/Java-17+-red.svg?style=for-the-badge&logo=openjdk)](https://adoptium.net/)
[![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-important.svg?style=for-the-badge)](LICENSE)

<br>

> **Protege tu tierra con estilo.** Sistema de protección avanzado con GUI profesional, efectos visuales, sistema de impuestos, permisos granulares, sistema de efectos de zona, traducción completa y mucho más.

---

### 📥 [Descargar Última Versión](https://github.com/tomas2193xd-arch/AdvancedProtection/releases/latest) · 🌐 [Página en SpigotMC](https://www.spigotmc.org/resources/advancedprotection.130494/) · 💬 Discord: `Tomas2193`

---

</div>

## ✨ Características Principales

<table>
<tr>
<td width="50%">

### 🛡️ Protección Avanzada
- **6 niveles** de protección (Carbón → Obsidiana)
- Radio dinámico de 10m a 100m
- Detección de colisiones inteligente
- Integración con WorldGuard
- Sistema anti-overlap

</td>
<td width="50%">

### 👥 Permisos Granulares
- 10 tipos de permisos individuales
- 5 roles predefinidos (Visitante → Co-Dueño)
- Roles personalizables por miembro
- Compatible con LuckPerms
- GUI visual para gestionar permisos

</td>
</tr>
<tr>
<td>

### ⚡ Efectos de Zona
- Velocidad, Fuerza, Haste, Salto, etc.
- Sistema de compra con economía
- Niveles mejorables por efecto
- Auto-aplicación a jugadores en la zona
- 12 efectos disponibles

</td>
<td>

### 🎨 Efectos Visuales
- 7 partículas personalizables
- Hologramas 3D con cristales rotativos
- Visualización de bordes en tiempo real
- Soporte para DecentHolograms
- TextDisplay nativo (1.19.4+)

</td>
</tr>
<tr>
<td>

### 💰 Sistema de Impuestos
- Cobro automático configurable
- Tasas por porcentaje o fijas
- Período de gracia antes de eliminación
- Notificaciones multi-nivel
- Aviso al iniciar sesión

</td>
<td>

### 🌍 Multi-Idioma
- Español e Inglés incluidos
- **100% traducible** desde config.yml
- Selector de idioma en el menú
- Todos los GUIs se traducen al instante
- Fácil para agregar tu propio idioma

</td>
</tr>
</table>

---

## 🖥️ Sistema de GUI Profesional

| Menú                 | Descripción                         |
| -------------------- | ----------------------------------- |
| 🏠 **Menú Principal** | Acceso a todas las funciones        |
| 🛒 **Tienda**         | Comprar protecciones con economía   |
| 👤 **Miembros**       | Gestionar miembros y permisos       |
| 🔧 **Flags**          | PVP, Mob Spawning, TNT, Fuego, etc. |
| ⚡ **Efectos**        | Comprar/mejorar efectos de zona     |
| 🎨 **Partículas**     | Cambiar efecto visual del borde     |
| 🌍 **Idioma**         | Cambiar idioma del plugin           |
| 👑 **Admin Panel**    | Gestión total con paginación        |

---

## 📋 Comandos

### Jugadores
```
/ap menu              Abrir menú principal
/ap visualize         Ver bordes de protección
/ap trust <jugador>   Añadir miembro
/ap untrust <jugador> Eliminar miembro
/ap trustlist         Ver lista de miembros
/ap rename <nombre>   Renombrar protección
/ap clearname         Eliminar nombre
/ap tp [id]           Teletransportarse (seguro)
/ap effects           Menú de efectos de zona
/ap paytax            Pagar impuestos
/ap help              Lista de comandos
```

### Administradores
```
/ap reload                       Recargar configuración
/ap give <jugador> <nivel> [x]   Dar protección a jugador
/ap logs                         Ver historial
/ap manager                      Panel de administración
```

---

## 🔐 Permisos

| Permiso                               | Descripción                    | Default |
| ------------------------------------- | ------------------------------ | ------- |
| `advancedprotection.use`              | Uso básico del plugin          | `true`  |
| `advancedprotection.tp`               | Teletransportar a protecciones | `true`  |
| `advancedprotection.effects`          | Acceso al menú de efectos      | `true`  |
| `advancedprotection.admin`            | Acceso total de admin          | `op`    |
| `advancedprotection.admin.give`       | Dar protecciones               | `op`    |
| `advancedprotection.admin.logs`       | Ver logs                       | `op`    |
| `advancedprotection.admin.manager`    | Panel de administración        | `op`    |
| `advancedprotection.bypass`           | Bypass de protecciones         | `op`    |
| `advancedprotection.tp.bypass_warmup` | Sin espera al TP               | `op`    |
| `advancedprotection.limit.X`          | Límite de protecciones         | `true`  |

---

## ⚙️ Requisitos

| Requisito     | Versión                 |
| ------------- | ----------------------- |
| **Minecraft** | 1.19.4+                 |
| **Java**      | 17+                     |
| **Server**    | Spigot / Paper / Purpur |
| **Vault**     | Requerido               |

### Dependencias Opcionales
- **WorldGuard** — Integración con regiones
- **PlaceholderAPI** — Placeholders
- **LuckPerms** — Permisos avanzados
- **DecentHolograms** — Motor de hologramas alternativo

---

## 📥 Instalación

1. **Descarga** el JAR desde [Releases](https://github.com/tomas2193xd-arch/AdvancedProtection/releases/latest)
2. **Coloca** el archivo en la carpeta `/plugins` de tu servidor
3. **Instala** [Vault](https://www.spigotmc.org/resources/vault.34315/) (requerido)
4. **Reinicia** el servidor
5. **Configura** el `config.yml` a tu gusto
6. **Recarga** con `/ap reload`

---

## 📞 Soporte

- 🌐 **SpigotMC:** [Página del Plugin](https://www.spigotmc.org/resources/advancedprotection.130494/)
- 💬 **Discord:** `Tomas2193`
- 🐛 **Issues:** [Reportar Bug](https://github.com/tomas2193xd-arch/AdvancedProtection/issues)

---

<div align="center">

### ⭐ Si te gusta el plugin, ¡deja una review en SpigotMC! ⭐

**Desarrollado con ❤️ por CrystalVerse**

*Versión 1.5 · Febrero 2025*

</div>
