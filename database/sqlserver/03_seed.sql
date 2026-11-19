USE cafejunin;
GO
IF NOT EXISTS(SELECT 1 FROM dbo.parcelas WHERE id='11111111-1111-1111-1111-111111111111') INSERT dbo.parcelas VALUES('11111111-1111-1111-1111-111111111111',N'El Mirador',N'Satipo, Junín','operador-local');
IF NOT EXISTS(SELECT 1 FROM dbo.parcelas WHERE id='22222222-2222-2222-2222-222222222222') INSERT dbo.parcelas VALUES('22222222-2222-2222-2222-222222222222',N'La Esperanza',N'Satipo, Junín','aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1');
GO
-- Actualiza exclusivamente la parcela y el propietario insertados por la versión anterior.
UPDATE dbo.parcelas SET nombre=N'El Mirador', propietario_subject='operador-local'
WHERE id='11111111-1111-1111-1111-111111111111' AND propietario_subject='demo' AND nombre=N'El Mirador · Demo local';
GO
