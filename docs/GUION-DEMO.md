# Recorrido de demostración

Inicia INICIAR-DEMO.cmd. En Cosecha elige parcela, fecha pasada, variedad y peso. En Lotes abre su detalle. En Calidad guarda puntaje 85, cero defectos primarios y dos secundarios. En Riesgo IA usa humedad ambiental 95 y demora 60; el simulador local genera alerta. En Alertas confirma con motivo e instrucción y revisa el detalle del lote. Para mostrar XGBoost, inicia ai-service y cambia PREDICTOR_MODE=xgboost con la misma clave. Indica explícitamente que los datos del modelo son sintéticos.

Para demostrar separación de actores utiliza Keycloak real con seguridad activa; la demo combinada no sirve como evidencia de login. Reinicia con SQL Server para conservar registros y probar persistencia.
