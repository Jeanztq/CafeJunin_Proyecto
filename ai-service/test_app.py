import os
os.environ['AI_API_KEY']='clave-de-prueba-local'
from fastapi.testclient import TestClient
from app import app

def test_auth_limits_and_provenance():
    with TestClient(app) as client:
        assert client.get('/health').json()['validado'] is False
        assert client.post('/predict',json={'humedad':95,'horasDemora':36}).status_code==401
        headers={'X-API-Key':os.environ['AI_API_KEY']}
        result=client.post('/predict',headers=headers,json={'humedad':95,'horasDemora':36})
        assert result.status_code==200
        assert 0<=result.json()['probabilidad']<=1
        assert result.json()['simulada'] is True
        assert client.post('/predict',headers=headers,json={'humedad':101,'horasDemora':36}).status_code==422
        assert client.post('/predict',headers=headers,json={'humedad':50,'horasDemora':-1}).status_code==422
