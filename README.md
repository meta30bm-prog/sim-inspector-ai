# SIM Inspector AI

Aplicativo Android para consultar informações de cartões SIM que o próprio Android disponibiliza ao aplicativo.

## Recursos
- Identificação de SIM ativo e slot (SIM 1 / SIM 2).
- Nome da operadora e país, quando fornecidos pelo sistema.
- Número da linha, somente quando o Android e a operadora o disponibilizam.
- Estado do SIM com descrições legíveis (pronto, sem chip, PIN/PUK necessário, bloqueios e erros).
- Código da operadora (MCC/MNC), quando disponível.
- Tecnologia de rede móvel e estado da conexão de dados.
- Indicação de roaming.
- Estimativa de qualidade do sinal (instantâneo, quando suportado pelo Android).
- Interface em português e solicitação de permissões em tempo de execução.

## Privacidade e limitações
O Android restringe o acesso a dados sensíveis de telefonia. O número pode não estar gravado no SIM ou ser ocultado pela operadora. O ICCID não é disponibilizado a aplicativos comuns em muitas versões do Android; esta versão não tenta contornar essas restrições. Não há garantia de leitura do número, ICCID ou outros identificadores em todos os aparelhos. As informações de rede dependem do modem, da operadora, do sinal e das permissões concedidas. A qualidade do sinal é uma leitura pontual e pode variar rapidamente; não substitui medições técnicas de RF.

O aplicativo não coleta nem envia dados para servidores. As consultas são feitas localmente usando as APIs oficiais do Android.

## Gerar APK
O GitHub Actions compila automaticamente após um push na branch `main` e em pull requests direcionados a ela. Também é possível iniciar manualmente em **Actions → Android APK → Run workflow**.

Ao terminar com sucesso, abra a execução e baixe o artefato **sim-inspector-ai-debug**. O APK de depuração estará dentro do ZIP. O workflow verifica se o arquivo APK foi realmente gerado antes de publicá-lo.

## Requisitos
- Android 8.0 (API 26) ou superior.
- Permissões de telefone concedidas pelo usuário para consultar os dados disponíveis.

## Desenvolvimento
- Kotlin
- Jetpack Compose
- Android Gradle Plugin 8.7.3
- Gradle 8.9
- JDK 17
