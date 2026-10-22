-- Sólo datos ficticios de desarrollo. No se ejecuta automáticamente en producción.
INSERT INTO parcelas(id,nombre,productor_referencia)
VALUES('11111111-1111-1111-1111-111111111111','Parcela de demostración — Junín','PRODUCTOR-DEMO')
ON CONFLICT(id) DO NOTHING;
