# CivicaTech API — Back-End Java (DDD)

## Integrantes
- **Gustavo Costa Moreira Pinheiro de Almeida** — RM: 573074 — Turma: 1TDSPV

## Links do Projeto
- **URL da API no Render:** https://civicatech-api.onrender.com
- **URL do Front-End na Vercel:** https://civicatech.vercel.app
- **Vídeo Demonstração (YouTube):** https://youtu.be/seu-video-aqui

## Variáveis de Ambiente Necessárias (Render)
Configure no painel do Render (nunca deixe senha fixa no código):
- `DB_URL`: `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL`
- `DB_USERNAME`: `seu_rm_oracle`
- `DB_PASSWORD`: `sua_senha_oracle`

## Como Executar Localmente
```bash
export DB_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
export DB_USERNAME=seu_rm
export DB_PASSWORD=sua_senha
mvn spring-boot:run
```
