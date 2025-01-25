# Order Management API

A RESTful API construída com Spring WebFlux e R2DBC para gerenciar pedidos e produtos.

## Funcionalidades:
- Criar e gerenciar pedidos com produtos associados.
- Calcular o valor total dos pedidos.
- Programação reativa com Spring WebFlux.

## Tecnologias:
- **Spring WebFlux**
- **R2DBC**
- **PostgreSQL**
- **Kafka** (Opcional)

## Endpoints

### Ordem de Pedidos

#### Criar um novo pedido
**POST** `/api/orders`  
**Descrição:** Cria um novo pedido no sistema.  
**Corpo da Requisição:**




```json

{
  "customer": "string",
  "status": "string",
  "totalValue": 0,
  "products": [
    {
      "id": 0,
      "name": "string",
      "price": 0,
      "orderId": 0
    }
  ]
}
```

## Lógica Interna:

Insere um novo pedido na tabela orders com os campos customer, status e totalValue inicializado como 0.
Recupera o id gerado pelo banco de dados para o pedido.
Insere os produtos associados em batch na tabela products.
Atualiza o valor total (totalValue) do pedido com base nos produtos inseridos.
Resposta: Retorna o ID do pedido criado.

```json
{
  "orderId": 12345
}
```
Listar todos os pedidos
GET /api/orders
Descrição: Retorna todos os pedidos no sistema.
Resposta:
```json
[
  {
    "orderId": "long",
    "customer": "string",
    "status": "string",
    "totalValue": "decimal",
    "products": [
      {
        "id": 0,
        "name": "string",
        "price": 0,
        "orderId": 0
      }
    ]
  }
]
```
## Pré-requisitos:
- **Java 17 ou superior.**
- **PostgreSQL configurado e em execução.**
- **Kafka (opcional, dependendo do uso).**

## Configuração do Ambiente
- **Clone o repositório:**
git clone https://github.com/seu-usuario/order-management-api.git
cd order-management-api
Configure o arquivo .env com as credenciais do banco de dados e do Kafka:

- **R2DBC_URL=r2dbc:postgresql://localhost:5432/order_management_db**
- **R2DBC_USERNAME=seu-usuario**
- **R2DBC_PASSWORD=sua-senha**
- **KAFKA_CONSUMER_BOOTSTRAP_SERVERS=localhost:seuLocalhost**
#### Inicie o banco de dados PostgreSQL e configure as tabelas necessárias.**

## Compile e execute a aplicação:
./mvnw spring-boot:run
Documentação da API

## Acesse a documentação interativa da API usando o Swagger:

URL: http://localhost:8080/swagger-ui.html

