

LGTM Stack
- Grafana Tempo
- Grafana Loki
- Grafana Prometheus
- Grafana Grafana

Mimir is a time series database that is part of the LGTM Stack.

ELK Stack
- Elasticsearch
- Logstash
- Kibana



## Big Picture
一個完整的測試環境大約會包含以下容器：
Your-Java-App: 你的 Spring Boot 4 應用。
Grafana Alloy: 負責收集日誌和 Traces 並分發。
Loki: 接收日誌。
Tempo: 接收 Traces。
Prometheus: 接收 Metrics（你現有的）。
Grafana: 展示介面（你現有的）。


Saga Pattern (Saga 模式)： 如果一個業務流程涉及跨服務的數據修改（如：訂單 -> 庫存 -> 支付），2026 年推薦使用 Saga Choreography (編排式)
- Choreography (舞蹈式) 的核心概念： 沒有一個中央指揮官去命令大家做事。每個微服務就像舞池裡的舞者，聽到某種特定的音樂（事件/Event），就會做出對應的動作，做完後再放出下一段音樂給別人聽。

React + Next.js + GraphQL Code Generator： 既然用了 GraphQL，你的前端應該是「自動生成 Type」的。


Railway contain Private Network, so we can use the private network for the services to connect to each other. And only open the GraphQL API for the frontend to connect to.

In Azure, we could use Azure Container Apps (ACA) to deploy the services.

we need to setup a VNET , so the services can connect to each other.
Each service contain ingress and we could limit the access to the services to the private network.

and we just set the GraphQL API to accept the requests from anyway, so the frontend can connect to the GraphQL API.





