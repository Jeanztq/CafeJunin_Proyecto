# Recorrido de cosecha y cata

1. Abre **INICIAR-CAFE-JUNIN.cmd**, entra en http://localhost:8080 e inicia sesión como **productor**. Elige **Lotes → Registrar cosecha** y registra la cosecha en La Esperanza. Por ejemplo, selecciona Bourbon, 120.500 kg y la fecha de recolección. El sistema asigna el identificador del lote.
2. Cierra sesión y entra como **catador**. Al primer acceso, vincula tu autenticador. En **Calidad**, elige el lote. Registra 0 defectos físicos primarios y 2 secundarios. Estos conteos describen el grano verde.
3. Califica fragancia, acidez, posgusto, balance y apreciación global con 8. Asigna 8.5 a sabor y cuerpo. Registra 10 en uniformidad, dulzor y taza limpia. Las diez notas suman 87.
4. Registra una taza con defecto leve y cero con defecto grave. El total baja a **85**. Si intentas registrar seis tazas afectadas o una nota fuera de escala, puedes corregirla antes de guardar.
5. Confirma la evaluación. El servidor recalcula el puntaje y registra el protocolo, el catador, la fecha y el umbral comercial aplicado. Abre el detalle del lote para recuperar la ficha y sus movimientos, incluso después de reiniciar el portal.

## Evidencias para el informe

Para el informe final de HU08, documenta la ficha del catador, la corrección de un dato fuera de escala y el resultado recuperado desde el historial del mismo lote. En las capturas registra el identificador del lote y el puntaje obtenido. La ficha actual ya incluye taza limpia y apreciación global. Las referencias anteriores a ocho atributos corresponden a una versión previa.

## Método

HU08 implementa la ficha clásica SCA de 2004 para cinco tazas. Siete atributos se puntúan en pasos de 0.25 de 6 a 10. Uniformidad, dulzor y taza limpia reciben 2 puntos por taza que cumple. Cada taza afectada se cuenta con la mayor intensidad observada. Se descuentan 2 puntos por defecto leve y 4 por grave. El protocolo posterior CVA tiene formularios propios.

El umbral comercial inicial es 80. Configura UMBRAL_ESPECIALIDAD para las nuevas evaluaciones. Las anteriores conservan su criterio, y los registros de versiones que admitían puntaje manual mantienen el puntaje original.

[Formulario clásico de SCAA](https://static1.squarespace.com/static/587af1d4db29d69a1a226b95/t/60cbcb4df5fdf54d2fe78f8e/1623968589683/Cupping%2Bx4%2BSCAA%2Bcupping%2Bform.pdf) · [Coffee Value Assessment de SCA](https://sca.coffee/value-assessment)
