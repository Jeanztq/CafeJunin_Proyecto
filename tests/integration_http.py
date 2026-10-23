"""Prueba real HTTP: JAR + FastAPI/XGBoost; repositorio en memoria. No prueba SQL Server/OIDC."""
import argparse, json, os, subprocess, sys, time, urllib.request, urllib.error, uuid
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]

def request(base,path,body=None,method='GET'):
    req=urllib.request.Request(base+path,data=json.dumps(body).encode() if body is not None else None,method=method,headers={'Content-Type':'application/json'})
    try:
        with urllib.request.urlopen(req,timeout=10) as result:return result.status,json.loads(result.read())
    except urllib.error.HTTPError as error:return error.code,json.loads(error.read())
def ready(url):
    for _ in range(100):
        try:
            with urllib.request.urlopen(url,timeout=1) as response:
                if response.status==200:return
        except (OSError,urllib.error.URLError):time.sleep(.2)
    raise RuntimeError('Servicio no inició: '+url)
def main():
    parser=argparse.ArgumentParser();parser.add_argument('--jar',default=str(ROOT/'backend/target/cafejunin-backend-0.2.0.jar'));parser.add_argument('--output');args=parser.parse_args()
    env={**os.environ,'AI_API_KEY':'prueba-local-http-2026','PREDICTOR_MODE':'xgboost','PREDICTOR_URL':'http://127.0.0.1:18001/predict','SECURITY_ENABLED':'false','PORT':'18080','SERVER_ADDRESS':'127.0.0.1'}
    processes=[];report={'backend':'Spring Boot + memoria','predictor':'FastAPI + XGBoost con datos sintéticos','checks':[]}
    try:
        processes.append(subprocess.Popen([sys.executable,'-m','uvicorn','app:app','--host','127.0.0.1','--port','18001'],cwd=ROOT/'ai-service',env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL))
        ready('http://127.0.0.1:18001/health')
        processes.append(subprocess.Popen(['java','-jar',str(Path(args.jar).resolve()),'--spring.profiles.active=memory'],env=env,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL))
        base='http://127.0.0.1:18080';ready(base+'/api/info')
        with urllib.request.urlopen(base) as response:assert b'type="module"' in response.read()
        report['checks'].append('React servido desde JAR')
        lot=str(uuid.uuid4());body={'id':lot,'parcelaId':'11111111-1111-1111-1111-111111111111','fechaCosecha':'2025-01-01','variedad':'BOURBON','tipoRecoleccion':'SELECTIVA','pesoKg':120.5}
        assert request(base,'/api/lotes',body,'POST')[0]==201
        assert request(base,'/api/lotes',body,'POST')[0]==201
        assert len(request(base,'/api/lotes')[1])==1
        report['checks'].append('Cosecha persistida; reintento no duplica')
        assert request(base,f'/api/lotes/{lot}/evaluaciones',{'defectosPrimarios':0,'defectosSecundarios':2,'puntajeSca':85},'POST')[0]==201
        status,risk=request(base,f'/api/lotes/{lot}/riesgos',{'humedad':95,'horasDemora':60},'POST')
        assert status==201 and risk['simulada'] is True and risk['modelo']=='xgboost-demo-v1' and risk['probabilidad']['valor']>.7,risk
        report['checks'].append('Predicción real por HTTP; modelo marcado sintético')
        alert=request(base,'/api/alertas')[1][0]
        review={'operacionId':str(uuid.uuid4()),'estado':'CONFIRMADA','motivo':'Riesgo verificado en campo','instruccion':'Priorizar traslado del lote'}
        assert request(base,f"/api/alertas/{alert['id']}/decision",review,'PUT')[1]['estado']=='CONFIRMADA'
        assert request(base,f"/api/alertas/{alert['id']}/decision",review,'PUT')[0]==200
        changed={**review,'estado':'DESCARTADA','instruccion':None}
        assert request(base,f"/api/alertas/{alert['id']}/decision",changed,'PUT')[0]==409
        report['checks'].append('Revisión humana, idempotencia y conflicto de decisión')
        before=request(base,f'/api/lotes/{lot}/detalle')[1]
        processes[0].terminate();processes[0].wait(timeout=10)
        assert request(base,f'/api/lotes/{lot}/riesgos',{'humedad':95,'horasDemora':60},'POST')[0]==503
        after=request(base,f'/api/lotes/{lot}/detalle')[1]
        assert len(after['riesgos'])==len(before['riesgos'])
        report['checks'].append('Servicio IA caído: 503 sin evaluación ni alerta adicional')
        report['status']='PASS';print(json.dumps(report,indent=2,ensure_ascii=False))
        if args.output:Path(args.output).write_text(json.dumps(report,indent=2,ensure_ascii=False))
    finally:
        for p in reversed(processes):
            if p.poll() is None:
                p.terminate()
                try:p.wait(timeout=10)
                except subprocess.TimeoutExpired:p.kill();p.wait()
if __name__=='__main__':main()
