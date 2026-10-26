#!/usr/bin/env bash
# Gera logins validos/invalidos e bloqueios para popular o dashboard do Grafana.
BASE=${BASE:-http://localhost:8080}
login() { curl -s -o /dev/null -w "%{http_code} login $1\n" -X POST "$BASE/login" -H 'Content-Type: application/json' -d "{\"username\":\"$1\",\"senha\":\"$2\"}"; }
for n in 1 2 3 4 5; do
  curl -s -o /dev/null -u manutencao:manut123 -X POST "$BASE/" -H 'Content-Type: application/json' -d "{\"username\":\"aluno$n\",\"senha\":\"senha$n\"}"
done
for i in $(seq 1 10); do
  n=$(( (i - 1) % 5 + 1 ))
  login maria maria123; login ana ana123; login "aluno$n" "senha$n"; login naoexiste x
  sleep 3
done
for u in joao aluno4 aluno5; do login $u errada; login $u errada; done
login joao joao123
echo "Pronto. Veja o dashboard em http://localhost:3000"
