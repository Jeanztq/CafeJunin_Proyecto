USE cafejunin;
GO
IF NOT EXISTS(SELECT 1 FROM dbo.parcelas WHERE id='11111111-1111-1111-1111-111111111111') INSERT dbo.parcelas VALUES('11111111-1111-1111-1111-111111111111',N'El Mirador · Demo local',N'Satipo, Junín','demo');
IF NOT EXISTS(SELECT 1 FROM dbo.parcelas WHERE id='22222222-2222-2222-2222-222222222222') INSERT dbo.parcelas VALUES('22222222-2222-2222-2222-222222222222',N'El Mirador · Productor de prueba',N'Satipo, Junín','aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1');
GO
