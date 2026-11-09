package br.com.siminspector

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

data class SimCardInfo(val slot: String, val carrier: String, val number: String, val country: String, val state: String, val network: String, val dataState: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SimInspectorScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimInspectorScreen() {
    val context = LocalContext.current
    var cards by remember { mutableStateOf<List<SimCardInfo>>(emptyList()) }
    var message by remember { mutableStateOf("Autorize o acesso ao telefone para consultar os dados disponíveis.") }
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
    }

    fun scan() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        if (!granted) { message = "Permissão de telefone necessária."; return }
        try {
            val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            val active = subscriptionManager.activeSubscriptionInfoList.orEmpty()
            cards = active.map { info ->
                val perSim = telephonyManager.createForSubscriptionId(info.subscriptionId)
                val phonePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED
                val phoneNumber = if (phonePermission) info.number?.takeIf { it.isNotBlank() } ?: "Não informado pela operadora" else "Permissão adicional necessária"
                SimCardInfo(
                    slot = "SIM ${info.simSlotIndex + 1}",
                    carrier = info.carrierName?.toString()?.takeIf { it.isNotBlank() } ?: perSim.networkOperatorName?.takeIf { it.isNotBlank() } ?: "Operadora indisponível",
                    number = phoneNumber,
                    country = info.countryIso?.uppercase() ?: "—",
                    state = if (perSim.simState == TelephonyManager.SIM_STATE_READY) "Pronto" else "Estado: ${perSim.simState}",
                    network = runCatching { TelephonyManager.getNetworkTypeName(perSim.dataNetworkType) }.getOrDefault("Indisponível"),
                    dataState = when (perSim.dataState) {
                        TelephonyManager.DATA_CONNECTED -> "Conectado"
                        TelephonyManager.DATA_CONNECTING -> "Conectando"
                        TelephonyManager.DATA_DISCONNECTED -> "Desconectado"
                        TelephonyManager.DATA_SUSPENDED -> "Suspenso"
                        else -> "Indisponível"
                    }
                )
            }
            message = if (cards.isEmpty()) "Nenhum chip ativo foi identificado. Verifique se o SIM está inserido e habilitado." else "${cards.size} chip(s) ativo(s) encontrado(s)."
        } catch (e: SecurityException) {
            message = "O Android bloqueou o acesso a algumas informações do chip."
            cards = emptyList()
        } catch (e: Exception) {
            message = "Não foi possível consultar o chip: ${e.localizedMessage ?: "erro desconhecido"}"
            cards = emptyList()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        hasPermission = result[Manifest.permission.READ_PHONE_STATE] == true
        if (hasPermission) scan() else message = "Permissão não concedida. A consulta ficará limitada."
    }

    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("SIM Inspector AI", fontWeight = FontWeight.Bold) }) }) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text("Diagnóstico do chip", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Consulte as informações que o Android disponibiliza para seus cartões SIM.", style = MaterialTheme.typography.bodyMedium)
                }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Status da verificação", fontWeight = FontWeight.Bold)
                            Text(message)
                            Button(
                                onClick = { permissionLauncher.launch(arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS)) },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(if (hasPermission) "Atualizar informações" else "Autorizar e verificar") }
                        }
                    }
                }
                if (cards.isNotEmpty()) {
                    item { Text("Chips encontrados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                    items(cards) { sim ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(sim.slot, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                InfoLine("Operadora", sim.carrier)
                                InfoLine("Número", sim.number)
                                InfoLine("País", sim.country)
                                InfoLine("Estado do SIM", sim.state)
                                InfoLine("Tecnologia de rede", sim.network)
                                InfoLine("Dados móveis", sim.dataState)
                            }
                        }
                    }
                }
                item {
                    Text("Observação: o Android e a operadora podem ocultar o número e o ICCID por motivos de privacidade. O aplicativo não consegue descobrir dados que o sistema não fornece.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Text(label, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(value, modifier = Modifier.weight(1.4f), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
