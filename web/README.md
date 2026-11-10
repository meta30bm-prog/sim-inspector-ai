# SIM Inspector AI — Portal Web

Portal responsivo para gerenciamento de estoque, catalogação e localização de chips.

## Executar
Abra `web/index.html` em um navegador moderno ou publique a pasta `web/` em uma hospedagem estática. Não há etapa de compilação neste MVP.

## Funcionalidades implementadas
- Dashboard com totais por etapa do estoque.
- Cadastro e edição de chips por número telefônico, operadora, responsável e ICCID opcional.
- Fluxo de estoque: não ativados → ativados → utilizados.
- Mapa inicial da Caixa 01 com 28 compartimentos (7 colunas × 4 linhas), capacidade de 40 chips por compartimento.
- Cadastro por compartimento e posição lógica de 01 a 40.
- Pesquisa por número, responsável, operadora ou ICCID.
- Histórico de cadastros e atualizações.
- Exportação CSV e backup/restauração JSON.
- Layout adaptável para computador e celular.

## Persistência e estado atual
Esta primeira entrega é um MVP funcional de interface. Os dados são gravados no `localStorage` do navegador e, portanto, ficam apenas naquele navegador/dispositivo. Limpar os dados do navegador pode apagá-los. Exporte backups regularmente durante a avaliação.

**Ainda não existe sincronização com o aplicativo Android nem banco de dados central.** O app Android atual usa armazenamento/consultas locais e não possui cliente de API para este portal. Não configure este MVP como sistema de produção nem cadastre nele dados operacionais reais até a etapa de backend, autenticação e sincronização estar concluída.

## Arquitetura de integração planejada
- Front-end web: esta aplicação responsiva.
- API HTTPS autenticada: contrato REST versionado em `/api/v1`.
- Banco central: PostgreSQL.
- Android: cliente autenticado da mesma API, mantendo cache local e sincronização.
- Usuário: inicialmente um único administrador, sem fluxo de convite/múltiplas contas.
- Segurança: sessão segura no servidor, hash de senha (Argon2id/bcrypt), HTTPS, controle de sessão, trilha de auditoria e backups.

### Recursos REST previstos
- `GET/POST /api/v1/chips`
- `GET/PATCH /api/v1/chips/:id`
- `GET /api/v1/chips/lookup?number=`
- `GET/POST /api/v1/boxes`
- `GET /api/v1/boxes/:id/compartments`
- `POST /api/v1/movements`
- `GET /api/v1/movements`
- `POST /api/v1/sync`

### Modelo de dados
- `chips`: id UUID, phone_e164, operator, iccid, owner, stock_stage, status, compartment_id, position, notes, created_at, updated_at.
- `boxes`: id UUID, name, active.
- `compartments`: id UUID, box_id, code, label, capacity (default 40).
- `movements`: id UUID, chip_id, action, from_location, to_location, actor_id, occurred_at, notes.
- `users`: uma conta administradora na primeira versão.
- Restrições: número telefônico não pode estar associado a dois chips simultaneamente ativos; posição única por compartimento; capacidade máxima transacional de 40; movimentações imutáveis.

## Organização física
O portal cria posições lógicas 01–40. Como os chips atualmente ficam soltos, a posição exibida não identifica por si só um chip físico específico. Para localização individual confiável, use divisórias, envelopes ou suportes numerados. Sem isso, utilize apenas a localização por compartimento.

## Próximas etapas técnicas
1. Criar API e banco PostgreSQL com migrações e validações transacionais.
2. Implementar autenticação de um administrador e configuração inicial segura da senha.
3. Substituir o adaptador local do portal por chamadas à API.
4. Adicionar sincronização e cache offline ao Android.
5. Testar fluxo completo, concorrência, backups e recuperação antes de uso operacional.
