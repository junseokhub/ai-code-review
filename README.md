# AI Code Review, featuring Harness (FastAPI)


```json
 curl -X POST http://localhost:8080/api/v1/webhook/commit \
  -H "Content-Type: application/json" \
  -d '{
    "provider_type": "GITHUB",
    "llm_provider": "OPENAI",
    "repo_name": "reponame",
    "commit_sha": "commit_sha"
  }'