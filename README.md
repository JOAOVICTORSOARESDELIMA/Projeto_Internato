[![CI](https://github.com/JOAOVICTORSOARESDELIMA/Projeto_Internato/actions/workflows/ci.yml/badge.svg?branch=Grupo_Main_)](https://github.com/JOAOVICTORSOARESDELIMA/Projeto_Internato/actions/workflows/ci.yml)

# Projeto Internato: API de gestão de plantões

API REST para organizar o **internato** de um curso da área da saúde: semestres, especialidades, locais de estágio, plantões, escala de alunos e **check-in por GPS** no início de cada plantão.

Feita na disciplina de **Tecnologia para Back-end** do curso de Sistemas para Internet (UNIESP, 3º período), em grupo. O PDF [`2aAvaliacaoTecBackend.pdf`](2aAvaliacaoTecBackend.pdf) traz as evidências dos testes feitos no Insomnia.

## Funcionalidades

- **CRUD** de semestres, períodos, especialidades, locais, usuários e plantões, com paginação e ordenação
- **Perfis de usuário:** coordenador, aluno, preceptor e supervisor
- **Escala de alunos** nos plantões, respeitando o número de vagas e sem repetir o mesmo aluno no mesmo plantão
- **Check-in** só é aceito:
  - até **15 minutos** antes ou depois do início do plantão;
  - a no máximo **100 metros** do local, com a distância calculada pela fórmula de Haversine.
- **Check-out** com **advertência automática** se o plantão passar de 12 horas (indica que o aluno esqueceu de registrar a saída)
- **Exclusão lógica** de usuários e semestres: o registro é marcado como inativo, não apagado

## Tecnologias

Java 21 · Spring Boot 3.4 (Web, Data JPA, Validation) · PostgreSQL · Lombok · Maven · JUnit 5 · Docker · GitHub Actions

## Como executar

### Com Docker (sobe banco e API juntos)

```bash
docker compose up --build
```

A API fica em `http://localhost:8080`.

### Sem Docker

Requisitos: **Java 21** e **PostgreSQL** com um banco chamado `internato`.

```bash
cd api
./mvnw spring-boot:run
```

A conexão é configurada por variáveis de ambiente (com valores padrão para desenvolvimento):

| Variável | Padrão |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/internato` |
| `DB_USER` | `postgres` |
| `DB_PASSWORD` | `postgres` |

## Endpoints

Todos os recursos abaixo têm `POST` (cadastrar), `GET` (listar paginado, com `?page=0&size=10&sort=campo`), `GET /{id}`, `PUT` (atualizar, com o `id` no corpo) e `DELETE /{id}`.

| Recurso | Rota | Campos para cadastrar |
|---|---|---|
| Semestre | `/semestre` | `descricao`, `dataInicio`, `dataFim` |
| Período | `/periodo` | `nome` |
| Especialidade | `/especialidade` | `nome` |
| Período × especialidade | `/periodo-especialidade` | `idPeriodo`, `idEspecialidade`, `cargaHorariaMinima` |
| Local | `/local` | `nome`, `sigla`, `endereco`, `cep`, `cidade`, `latitude`, `longitude` |
| Local × especialidade | `/local-especialidade` | `idLocal`, `idEspecialidade`, `idSemestre`, vagas por turno |
| Usuário | `/usuario` | `nome`, `email`, `matricula`, `perfil` |
| Plantão | `/plantao` | `data` (futura), `horaInicio`, `horaFim`, `vagas`, `idLocalEspecialidade`, `idPreceptor` |
| Escala do aluno | `/plantao-aluno` | `idPlantao`, `idAluno` |

### Check-in e check-out

```http
POST /checkin
{ "idPlantaoAluno": 1, "dataHoraCheckIn": "2026-05-10T07:05:00",
  "latitude": -7.1366, "longitude": -34.8442 }

POST /checkin/checkout
{ "idCheckIn": 1, "dataHoraCheckOut": "2026-05-10T13:00:00" }
```

### Respostas de erro

| Situação | Status | Corpo |
|---|---|---|
| Registro não existe | `404` | `{"mensagem": "Registro não encontrado."}` |
| Campo obrigatório vazio ou inválido | `400` | `[{"campo": "email", "mensagem": "..."}]` |
| Regra de negócio (sem vaga, fora do horário, longe do local) | `400` | `{"mensagem": "..."}` |
| Registro duplicado (ex.: e-mail já usado) | `409` | `{"mensagem": "..."}` |

## Estrutura

Cada recurso tem sua própria pasta, com entidade JPA, repositório, controller e DTOs (`record`) de cadastro, atualização, listagem e detalhamento:

```
api/src/main/java/internato/projeto/api/
├── Semestre/  Periodo/  Especialidade/  PeriodoEspecialidade/
├── Local/  LocalEspecialidade/  Usuario/
├── Plantao/  PlantaoAluno/
├── CheckIn/
│   ├── CheckInController.java
│   └── RegrasCheckIn.java     # horário, distância e advertência, testáveis sem banco
└── infra/
    ├── TratadorDeErros.java   # transforma exceções em respostas 400/404/409
    └── RegraDeNegocioException.java
```

Os arquivos `V01` a `V10` em `src/main/resources` documentam o modelo do banco em SQL. As tabelas são criadas pelo Hibernate (`ddl-auto=update`).

## Testes

```bash
cd api
./mvnw test
```

- `RegrasCheckInTest`: tolerância de horário, cálculo de distância, limite de 100 m e advertência
- `ApiApplicationTests`: sobe a aplicação inteira conectada ao PostgreSQL

No GitHub Actions, um PostgreSQL temporário é criado para rodar os testes a cada push.

## O que mudou em relação à versão entregue

- **Segurança:** a senha do banco estava escrita no `application.properties`. Agora vem de variável de ambiente.
- **Listagem de usuários corrigida:** os inativos eram filtrados depois da paginação, o que gerava páginas com menos itens e total errado. O filtro agora é feito no banco.
- **Erros tratados:** buscar um ID inexistente devolvia erro 500. Agora responde 404, e validações e regras de negócio respondem 400 em JSON.
- **Nova regra:** o mesmo aluno não pode ser escalado duas vezes no mesmo plantão.
- As regras de check-in saíram do controller para a classe `RegrasCheckIn`, com testes unitários.
- Adicionados Docker, docker-compose e CI. A pasta `.idea` saiu do repositório.

## Equipe

- Maria Eduarda Maia Santos: [GitHub](https://github.com/mariaeduardams)
- Lucas Luna Accioly da Cruz
- **João Victor Soares de Lima**: [GitHub](https://github.com/JOAOVICTORSOARESDELIMA)

Repositório original: [mariaeduardams/Projeto_Internato](https://github.com/mariaeduardams/Projeto_Internato) · GitLab: [DevRafaelSa/estagio360](https://gitlab.com/DevRafaelSa/estagio360)
