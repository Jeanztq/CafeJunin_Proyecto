# Portal de trazabilidad

El portal usa React y Vite. La entrada es **index.html** y la ficha de HU08 está en **src/Calidad.jsx**. El JAR sirve la compilación de **dist**.

Durante el desarrollo, inicia el backend, instala las dependencias con npm ci y ejecuta npm run dev desde esta carpeta. El servidor de Vite reenvía las llamadas a la API local. Ejecuta npm test para validar el portal y npm run build para generar el contenido que empaqueta Maven.
