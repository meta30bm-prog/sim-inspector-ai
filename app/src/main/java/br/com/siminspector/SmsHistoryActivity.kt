package br.com.siminspector

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject

private const val SMS_PREFS = "sms_history"
private const val SMS_ITEMS = "messages"

data class SavedSms(val sender: String, val body: String, val date: Long)

class IncomingSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val prefs = context.getSharedPreferences(SMS_PREFS, Context.MODE_PRIVATE)
        val stored = runCatching { JSONArray(prefs.getString(SMS_ITEMS, "[]")) }.getOrElse { JSONArray() }
        messages.forEach { sms ->
            val body = sms.messageBody.orEmpty()
            // Não capturar nem armazenar mensagens que aparentem conter códigos de autenticação/uso único.
            val looksLikeOtp = Regex("""(?i)(verification|verify|authentication|auth|security|login|one[- ]?time|otp|c[oó]digo|c[oó]digo de acesso|whats.?app).{0,70}\b\d{4,8}\b|\b\d{4,8}\b.{0,70}(verification|code|c[oó]digo|whats.?app)""").containsMatchIn(body)
            if (body.isBlank() || looksLikeOtp) return@forEach
            val item = JSONObject().put("sender", sms.originatingAddress ?: "Remetente desconhecido")
                .put("body", body).put("date", sms.timestampMillis)
            stored.put(item)
        }
        // Limite local para evitar crescimento ilimitado do armazenamento.
        val trimmed = JSONArray()
        val start = (stored.length() - 200).coerceAtLeast(0)
        for (i in start until stored.length()) trimmed.put(stored.getJSONObject(i))
        prefs.edit().putString(SMS_ITEMS, trimmed.toString()).apply()
    }
}

class SmsHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SmsHistoryScreen(onBack = { finish() }) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SmsHistoryScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) }
    var items by remember { mutableStateOf(loadSms(context)) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("Histórico de SMS") }, navigationIcon = {
            TextButton(onClick = onBack) { Text("Voltar") }
        }) }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("As mensagens comuns recebidas serão guardadas somente neste aparelho. Mensagens que aparentem conter códigos de autenticação são descartadas e não entram no histórico.", style = MaterialTheme.typography.bodyMedium)
                if (!granted) {
                    Button(onClick = { permission.launch(Manifest.permission.RECEIVE_SMS) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Ativar recebimento de SMS")
                    }
                } else {
                    Text("Recebimento autorizado", color = MaterialTheme.colorScheme.primary)
                    OutlinedButton(onClick = { items = loadSms(context) }, modifier = Modifier.fillMaxWidth()) { Text("Atualizar histórico") }
                }
                OutlinedButton(onClick = {
                    context.getSharedPreferences(SMS_PREFS, Context.MODE_PRIVATE).edit().remove(SMS_ITEMS).apply()
                    items = emptyList()
                }, modifier = Modifier.fillMaxWidth()) { Text("Apagar histórico") }
                if (items.isEmpty()) Text("Nenhuma mensagem comum armazenada.")
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items.reversed()) { sms ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(sms.sender, style = MaterialTheme.typography.titleSmall)
                                Text(sms.body, style = MaterialTheme.typography.bodyMedium)
                                Text(java.text.DateFormat.getDateTimeInstance().format(java.util.Date(sms.date)), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun loadSms(context: Context): List<SavedSms> {
    val json = runCatching {
        JSONArray(context.getSharedPreferences(SMS_PREFS, Context.MODE_PRIVATE).getString(SMS_ITEMS, "[]"))
    }.getOrElse { JSONArray() }
    return (0 until json.length()).mapNotNull { i ->
        runCatching {
            val item = json.getJSONObject(i)
            SavedSms(item.optString("sender"), item.optString("body"), item.optLong("date"))
        }.getOrNull()
    }
}
