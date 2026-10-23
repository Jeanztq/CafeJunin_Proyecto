import hashlib, hmac, json, os
from contextlib import asynccontextmanager
from pathlib import Path
import numpy as np
import xgboost as xgb
from fastapi import FastAPI, Header, HTTPException
from pydantic import BaseModel, Field, ConfigDict

@asynccontextmanager
async def lifespan(app):
    key=os.environ.get('AI_API_KEY','')
    if len(key)<16:raise RuntimeError('Configure AI_API_KEY con al menos 16 caracteres.')
    root=Path(os.environ.get('MODEL_DIR',str(Path(__file__).parent/'model')))
    card=json.loads((root/'model-card.json').read_text())
    if hashlib.sha256((root/'risk.json').read_bytes()).hexdigest()!=card['sha256']:raise RuntimeError('Hash del modelo inválido.')
    model=xgb.XGBClassifier();model.load_model(root/'risk.json')
    app.state.model=model;app.state.card=card;app.state.key=key
    yield
app=FastAPI(title='Café Junín · Predicción de riesgo',version='0.2.0',lifespan=lifespan)
class Inputs(BaseModel):
    model_config=ConfigDict(extra='forbid',allow_inf_nan=False)
    humedad:float=Field(ge=0,le=100)
    horasDemora:float=Field(ge=0)
@app.get('/health')
def health():return {'status':'UP','modelo':app.state.card['id'],'validado':app.state.card['validated']}
@app.post('/predict')
def predict(body:Inputs,x_api_key:str=Header(default='')):
    if not hmac.compare_digest(x_api_key,app.state.key):raise HTTPException(401,'Credencial inválida')
    probability=float(app.state.model.predict_proba(np.array([[body.humedad,body.horasDemora]]))[0,1])
    # Cualquier modelo candidato sigue identificado como demostración hasta validación independiente.
    return {'probabilidad':probability,'modelo':app.state.card['id'],'simulada':not app.state.card['validated']}
