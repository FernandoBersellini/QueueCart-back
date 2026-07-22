# QueueCart — Contexto do Projeto

## Sobre

E-commerce fictício, projeto pessoal de portfólio. Objetivo central: **aprender técnicas e
tecnologias novas com profundidade**, não apenas entregar features. Se uma decisão for entre
"o jeito mais rápido" e "o jeito que ensina algo novo, mas cabe no tempo disponível", prefira o
segundo. Se não couber no tempo disponível, prefira reduzir escopo a cortar qualidade.

Contexto do desenvolvedor: fullstack dev buscando vaga júnior, com disponibilidade reduzida
para desenvolvimento nos próximos meses. Isso significa: **entregas pequenas e completas por
fase valem mais que um projeto ambicioso pela metade.**

## Stack

- **Backend**: Java 21, Spring Boot 4.1.0, Maven
- **Frontend**: Next.js (App Router) + TypeScript
- **Banco**: PostgreSQL + Flyway (migrations versionadas)
- **Mensageria**: RabbitMQ — é o foco principal de aprendizado deste projeto
- **Auth**: JWT via `jjwt` 0.12.6 — usar `jjwt-gson`, **não** `jjwt-jackson` (conflita com
  Jackson 3, que é o padrão no Spring Boot 4)
- **Docs de API**: springdoc-openapi (opcional, baixo custo de setup)
- **Testes**: seguir a convenção modular do Boot 4 (um starter de teste por starter principal
  usado, ex.: `spring-boot-starter-webmvc-test`) em vez do `spring-boot-starter-test` genérico

Dependências completas: ver `pom.xml` no repositório.

## Arquitetura

**Monólito modular**, não microsserviços de verdade — decisão deliberada para ter o gostinho da
decomposição por domínio sem o custo operacional (deploys separados, rede, descoberta de
serviço). Usar **Spring Modulith** para isso valer de verdade:

- Cada domínio é um módulo/pacote: `catalog`, `cart`, `order`, `inventory`, `notification`, `user`
- Módulos não acessam classes internas uns dos outros — só APIs públicas
- Rodar `ApplicationModules.verify()` como teste, pra garantir que as fronteiras não vazam
- Comunicação entre módulos via evento de domínio (`@ApplicationModuleListener`)
- O evento `order.created` é **externalizado** para o RabbitMQ (`@Externalized`) e dispara dois
  consumers: `inventory` (decrementa estoque) e `notification` (confirmação do pedido)
- Configurar **retry + dead-letter queue (DLQ)** no fluxo do RabbitMQ — não pular essa parte,
  é o principal ponto de aprendizado do projeto

**Hexagonal (ports & adapters)** — aplicar **somente no módulo `order`**, não no projeto
inteiro:
- Domínio (`Order` e regras de negócio) sem nenhuma dependência de Spring/JPA/AMQP
- Portas de saída: interfaces como `OrderRepository`, `OrderEventPublisher`
- Adaptadores: implementação JPA da `OrderRepository`, implementação RabbitMQ da
  `OrderEventPublisher`

Os outros módulos (catalog, cart, user) podem ficar em uma organização mais simples — não
precisa hexagonal em todo o lugar, só onde ajuda a sentir o padrão.

## Roadmap

**Fase 1 — Núcleo (prioridade atual)**
Modelagem de domínio (Product, Category, Cart, Order, User), API REST de catálogo/carrinho,
JWT, frontend Next.js consumindo a API.

**Fase 2 — Mensageria (o coração do projeto)**
Spring Modulith + RabbitMQ: fluxo `order.created → inventory + notification`, com retry e DLQ.

**Fase 3 — Se sobrar tempo**
Stripe (modo teste) para pagamento,Redis para carrinho/sessão, Docker Compose unificando os
serviços locais.

**Fora de escopo por agora** (não iniciar sem decisão explícita): Elasticsearch, Kubernetes,
stack de observabilidade completa (Prometheus/Grafana). Não são descartados, só não são o foco.

## Convenções de código

- **Idioma**: todo o código é em inglês — nomes de classes, métodos, variáveis, comentários,
  mensagens de commit, exceptions, logs. A conversa comigo pode continuar em português, mas
  nada em português entra no código ou no sistema.
- **Nomenclatura**:
    - `camelCase` para variáveis e métodos (Java e TypeScript)
    - `PascalCase` para classes e componentes React
    - `UPPER_SNAKE_CASE` para constantes
    - pacotes Java em minúsculo, sem underscore (ex.: `com.senhorcafe.queuecart.order`)
- Ao gerar código, siga essas convenções sem precisar que eu peça de novo a cada sessão.

## Quando estiver em dúvida

- Prefira a solução mais simples que ainda ensina algo do escopo desta fase.
- Não expanda escopo por conta própria (ex.: não introduzir Kubernetes ou Elasticsearch "de
  brinde") — se achar que vale a pena, sugira e espere confirmação.
- Não avance de fase sem a fase anterior estar funcional e testável.