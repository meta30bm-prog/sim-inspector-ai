# SIM Inspector AI

Aplicativo Android para consultar informações de cartões SIM que o próprio Android disponibiliza ao aplicativo.

## Recursos iniciais
- Identificação de SIM ativo e slot (SIM 1 / SIM 2).
- Nome da operadora e país, quando fornecidos pelo sistema.
- Número da linha, somente quando o Android e a operadora o disponibilizam.
- Estado básico do SIM.
- Interface em português e solicitação de permissões em tempo de execução.

## Limitações importantes
O Android restringe o acesso a dados sensíveis de telefonia. O número pode não estar gravado no SIM ou ser ocultado pela operadora. O ICCID não é disponibilizado a aplicativos comuns em muitas versões do Android; esta versão não tenta contornar essas restrições. Não há garantia de leitura do número, ICCID ou outros identificadores em todos os aparelhos.

## Gerar APK
O GitHub Actions compila automaticamente após um push na branch `main`. Também é possível iniciar manualmente em **Actions → Android APK → Run workflow**.

Ao terminar, abra a execução e baixe o artefato **sim-inspector-ai-debug**. O APK de depuração estará dentro do ZIP.

## Requisitos
- Android 8.0 (API 26) ou superior.
- Permissões de telefone concedidas pelo usuário para consultar os dados disponíveis.

## Desenvolvimento
- Kotlin
- Jetpack Compose
- Gradle
