"""Entrenamiento reproducible. --demo utiliza una regla inventada, no evidencia agronómica."""
import argparse, hashlib, json
from pathlib import Path
import numpy as np
import pandas as pd
import xgboost as xgb
from sklearn.model_selection import train_test_split
from sklearn.metrics import roc_auc_score, brier_score_loss

FEATURES = ['humedad', 'horasDemora']
def train(destination, csv=None):
    rng = np.random.default_rng(42)
    if csv:
        frame = pd.read_csv(csv)
        kind = 'REAL_NO_VALIDADO'
    else:
        h, d = rng.uniform(0, 100, 2500), rng.uniform(0, 72, 2500)
        # Relación artificial para probar el circuito técnico, sin valor predictivo real.
        p = 1 / (1 + np.exp(-(-6 + h * .055 + d * .07)))
        frame = pd.DataFrame({'humedad': h, 'horasDemora': d, 'riesgo': rng.binomial(1, p)})
        kind = 'SYNTHETIC'
    if not set(FEATURES + ['riesgo']).issubset(frame.columns):
        raise ValueError('CSV requiere humedad,horasDemora,riesgo (etiqueta binaria observada).')
    x, y = frame[FEATURES].astype(float), frame['riesgo']
    if not np.isfinite(x).all().all() or not x.humedad.between(0,100).all() or not (x.horasDemora >= 0).all() or set(y.unique()) != {0,1} or y.value_counts().min()<10:
        raise ValueError('Datos inválidos o menos de diez ejemplos por clase.')
    train_x,test_x,train_y,test_y=train_test_split(x,y,test_size=.25,random_state=42,stratify=y)
    model=xgb.XGBClassifier(n_estimators=100,max_depth=3,learning_rate=.08,n_jobs=1,random_state=42,eval_metric='logloss')
    model.fit(train_x,train_y)
    out=Path(destination);out.mkdir(parents=True,exist_ok=True)
    model.save_model(out/'risk.json')
    probability=model.predict_proba(test_x)[:,1]
    card={'id':'xgboost-demo-v1' if not csv else 'xgboost-candidato-v1','datasetType':kind,'validated':False,'xgboostVersion':xgb.__version__,'features':{'humedad':'Humedad relativa AMBIENTAL (%)','horasDemora':'Demora observada desde cosecha (horas)'},'rows':len(frame),'split':'75/25 aleatorio estratificado; sustituir por validación temporal y por finca para producción','metrics':{'auc':float(roc_auc_score(test_y,probability)),'brier':float(brier_score_loss(test_y,probability))},'sha256':hashlib.sha256((out/'risk.json').read_bytes()).hexdigest(),'limitations':'Datos sintéticos o candidato no validado. No demuestra eficacia agronómica. Sin validación independiente, calibración ni seguimiento de deriva. No usar para decisiones reales.'}
    (out/'model-card.json').write_text(json.dumps(card,indent=2,ensure_ascii=False))
    return card
if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--demo',action='store_true');parser.add_argument('--csv');parser.add_argument('--output',default='model');args=parser.parse_args()
    if bool(args.demo)==bool(args.csv):parser.error('Selecciona --demo o --csv ruta.csv')
    print(json.dumps(train(args.output,args.csv),indent=2,ensure_ascii=False))
