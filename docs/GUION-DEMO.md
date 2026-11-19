# Demostración de HU08

Abre la demo con **INICIAR-DEMO.cmd** y entra en http://localhost:8080. El módulo Calidad presenta un lote Bourbon de El Mirador, Satipo. Los valores siguientes sirven como muestra para la exposición.

| Paso | Qué hacer | Qué observar |
|---|---|---|
| 1 · Captura | Elige el lote de 120.500 kg. Ingresa 0 defectos primarios y 2 secundarios. | El formulario muestra el origen y separa los defectos de los ocho atributos. |
| 2 · Perfil sensorial | Ingresa fragancia 8, sabor 8.5, acidez 8, cuerpo 8.5, posgusto 8, balance 8, uniformidad 10 y dulzor 10. | Cada atributo usa una escala de 0 a 10. |
| 3 · Corrección (CA03) | Escribe 105 como puntaje final y pulsa Registrar evaluación. | Aparece el rango válido. El foco vuelve al campo y los datos de la muestra siguen en pantalla. |
| 4 · Confirmación (CA01) | Corrige el puntaje a 85 y vuelve a registrar. | El comprobante muestra el lote, la fecha, el catador de la sesión y la clasificación «Café especial». |
| 5 · Consulta (CA02) | Abre Lotes y muestras, luego Ver detalle y Ver atributos sensoriales. | La ficha conserva los ocho valores, los defectos y el puntaje tras recargar el portal. |

Para mostrar el límite, crea una ficha con 79.50 puntos. El historial conserva ambas muestras y clasifica la segunda como «Café comercial». Un puntaje de 80.00 ya alcanza el umbral acordado para este proyecto. El catador ingresa el puntaje final de forma independiente.

En la demo el nombre del responsable es «Demostración local». Con seguridad activa, el servidor obtiene la identidad del token y exige el rol CATADOR. La emisión de certificados corresponde a otro alcance.
