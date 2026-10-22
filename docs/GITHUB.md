# Control de versiones y GitHub

La entrega es una carpeta de proyecto, **no un repositorio remoto publicado**.
No contiene credenciales GitHub ni modifica su cuenta. Use un solo repositorio para backend/web/mobile.

Desde la raíz, con Git instalado:

```powershell
git init -b main
git status
git add .
git diff --cached --stat
git commit -m "feat: prototipo hexagonal de cosecha calidad y riesgo"
```

Configure su nombre/correo de Git si es su primer uso. Revise los archivos preparados antes del commit.
`.env` está ignorado; `.env.example` sí se versiona.

Cree un repositorio vacío en su cuenta de GitHub y use **su URL real**:

```powershell
git remote add origin https://github.com/SU-USUARIO/cafejunin.git
git push -u origin main
```

No ejecute la URL de ejemplo literalmente. Si ya existe un remote, revise `git remote -v` antes de cambiarlo.
La autenticación se realiza con Git Credential Manager o la herramienta habitual de su equipo.

Trabajo por funcionalidad: `git switch -c feat/registro-cosecha`. Commits pequeños, Pull Request y pruebas antes de integrar.
El workflow valida Java y ejecuta una prueba real con PostgreSQL cuando se publique. No despliega el sistema.
No se pueden afirmar ejecuciones de GitHub Actions hasta que el repositorio se publique y el workflow termine.
