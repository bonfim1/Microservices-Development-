# Gera logins validos/invalidos e bloqueios para popular o dashboard do Grafana.
# Uso (PowerShell, com a aplicacao rodando em http://localhost:8080):
#   powershell -ExecutionPolicy Bypass -File scripts\gerar-trafego.ps1
$base = "http://localhost:8080"

function Login($u, $s) {
    $body = @{ username = $u; senha = $s } | ConvertTo-Json
    try {
        Invoke-RestMethod -Method Post -Uri "$base/login" -ContentType "application/json" -Body $body | Out-Null
        Write-Host "200 login $u"
    } catch { Write-Host "$($_.Exception.Response.StatusCode.value__) login $u" }
}

$cred = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("manutencao:manut123"))
foreach ($n in 1..5) {
    $body = @{ username = "aluno$n"; senha = "senha$n" } | ConvertTo-Json
    try { Invoke-RestMethod -Method Post -Uri "$base/" -ContentType "application/json" -Body $body -Headers @{ Authorization = "Basic $cred" } | Out-Null } catch {}
}

foreach ($i in 1..10) {
    Login "maria" "maria123"
    Login "ana" "ana123"
    Login "aluno$((($i - 1) % 5) + 1)" "senha$((($i - 1) % 5) + 1)"
    Login "naoexiste" "x"
    Start-Sleep -Seconds 3
}
# bloqueia joao, aluno4 e aluno5 (2 falhas seguidas)
foreach ($u in "joao", "aluno4", "aluno5") { Login $u "errada"; Login $u "errada" }
Login "joao" "joao123"   # bloqueado -> 401
Write-Host "Pronto. Veja o dashboard em http://localhost:3000"
