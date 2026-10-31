package com.clcwpwqi.cms2027

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.clcwpwqi.cms2027.core.Cms2027
import com.clcwpwqi.cms2027.data.AppSettings
import com.clcwpwqi.cms2027.data.AppStore
import com.clcwpwqi.cms2027.data.HistoryItem
import com.clcwpwqi.cms2027.ui.Qr
import com.clcwpwqi.cms2027.ui.S
import com.clcwpwqi.cms2027.ui.theme.CmsTheme
import com.clcwpwqi.cms2027.ui.theme.PRESET_SEEDS
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = AppStore(applicationContext)
        setContent {
            val settings by store.settings.collectAsStateWithLifecycle(AppSettings())
            val history by store.historyFlow.collectAsStateWithLifecycle(emptyList())
            CmsTheme(settings.themeMode, settings.colorMode, settings.seed) {
                AppRoot(store, settings, history)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRoot(store: AppStore, settings: AppSettings, history: List<HistoryItem>) {
    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route ?: "home"
    val lang = settings.language
    fun t(key: String) = S.t(lang, key)
    Scaffold(
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Lock, null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("CMS-2027", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(Modifier.height(10.dp))
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    listOf("home" to "nav.home", "history" to "nav.history", "settings" to "nav.settings").forEachIndexed { i, (r, key) ->
                        SegmentedButton(
                            selected = route == r,
                            onClick = { nav.navigate(r) { launchSingleTop = true } },
                            shape = SegmentedButtonDefaults.itemShape(i, 3),
                        ) { Text(t(key)) }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = "home", modifier = Modifier.padding(padding)) {
            composable("home") { HomeScreen(store, settings) }
            composable("history") { HistoryScreen(store, settings, history) }
            composable("settings") { SettingsScreen(store, settings) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(store: AppStore, settings: AppSettings) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var op by remember { mutableStateOf("encrypt") }
    var mode by remember { mutableStateOf("normal") }
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var qrBmp by remember { mutableStateOf<Bitmap?>(null) }
    fun t(k: String) = S.t(settings.language, k)
    val scanLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val parsed = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
        if (parsed?.contents != null) input = parsed.contents
    }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(t("home.title"), fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
        Text(t("home.subtitle"), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(op == "encrypt", { op = "encrypt" }, SegmentedButtonDefaults.itemShape(0, 2)) { Text(t("home.encrypt")) }
            SegmentedButton(op == "decrypt", { op = "decrypt" }, SegmentedButtonDefaults.itemShape(1, 2)) { Text(t("home.decrypt")) }
        }
        Spacer(Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(mode == "normal", { mode = "normal" }, SegmentedButtonDefaults.itemShape(0, 2)) { Text(t("home.modeNormal")) }
            SegmentedButton(mode == "strong", { mode = "strong" }, SegmentedButtonDefaults.itemShape(1, 2)) { Text(t("home.modeStrong")) }
        }
        Spacer(Modifier.height(16.dp))
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    placeholder = { Text(t(if (op == "encrypt") "home.inputEncrypt" else "home.inputDecrypt")) },
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(),
                )
                Row(Modifier.padding(top = 8.dp)) {
                    IconButton(onClick = {
                        val cm = ctx.getSystemService(ClipboardManager::class.java)
                        val clip = cm.primaryClip?.getItemAt(0)?.coerceToText(ctx)?.toString()
                        if (clip != null) input = clip else Toast.makeText(ctx, t("home.pasteFailed"), Toast.LENGTH_SHORT).show()
                    }) { Icon(Icons.Filled.ContentPaste, t("home.paste")) }
                    IconButton(onClick = {
                        val integrator = IntentIntegrator(ctx as ComponentActivity)
                        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                        integrator.setOrientationLocked(false)
                        integrator.setBeepEnabled(false)
                        integrator.setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity::class.java)
                        scanLauncher.launch(integrator.createScanIntent())
                    }) { Icon(Icons.Filled.QrCodeScanner, t("home.scan")) }
                    IconButton(onClick = { input = "" }) { Icon(Icons.Filled.Delete, t("home.clear")) }
                }
                if (mode == "strong") {
                    OutlinedTextField(
                        value = key,
                        onValueChange = { key = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(t("home.passphrase")) },
                        placeholder = { Text(t("home.passphraseHint")) },
                        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton({ showKey = !showKey }) {
                                Icon(if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                if (input.isBlank()) { error = t("home.emptyInput"); return@Button }
                if (mode == "strong" && key.isEmpty()) { error = t("home.emptyKey"); return@Button }
                working = true
                error = null
                scope.launch {
                    try {
                        val pass = if (mode == "strong") key else Cms2027.BUILTIN_KEY
                        val (plain, cipher) = withContext(Dispatchers.Default) {
                            if (op == "encrypt") input to Cms2027.encrypt(input, pass)
                            else Cms2027.decrypt(input, pass) to input
                        }
                        output = if (op == "encrypt") cipher else plain
                        store.addHistory(
                            HistoryItem(
                                id = UUID.randomUUID().toString(),
                                type = op,
                                mode = mode,
                                key = if (mode == "strong") key else null,
                                plain = plain,
                                cipher = cipher,
                                ts = System.currentTimeMillis(),
                            ),
                        )
                    } catch (e: Cms2027.DecryptionFailedException) {
                        error = t("home.decryptFailed")
                    } catch (e: Cms2027.InvalidPayloadException) {
                        error = t("home.invalidPayload")
                    } catch (e: Exception) {
                        error = t("home.decryptFailed")
                    } finally {
                        working = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !working,
            shape = RoundedCornerShape(28.dp),
        ) {
            if (working) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(8.dp))
                Text(t("home.working"))
            } else Text(t(if (op == "encrypt") "home.encrypt" else "home.decrypt"), fontWeight = FontWeight.Bold)
        }
        if (error != null) {
            Surface(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(16.dp),
            ) { Text(error!!, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer) }
        }
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = output,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text(t("home.output")) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                    shape = RoundedCornerShape(20.dp),
                )
                Row(Modifier.padding(top = 8.dp)) {
                    IconButton(onClick = {
                        val cm = ctx.getSystemService(ClipboardManager::class.java)
                        cm.setPrimaryClip(ClipData.newPlainText("cms", output))
                        Toast.makeText(ctx, t("home.copied"), Toast.LENGTH_SHORT).show()
                    }) { Icon(Icons.Filled.ContentCopy, t("home.copy")) }
                    IconButton(onClick = {
                        if (output.isBlank()) return@IconButton
                        val bmp = Qr.encode(output)
                        if (bmp == null) Toast.makeText(ctx, t("home.qrTooLong"), Toast.LENGTH_SHORT).show()
                        else qrBmp = bmp
                    }) { Icon(Icons.Filled.QrCode, t("home.qr")) }
                    IconButton(onClick = { output = ""; error = null }) { Icon(Icons.Filled.Delete, t("home.clear")) }
                }
            }
        }
    }
    if (qrBmp != null) {
        AlertDialog(
            onDismissRequest = { qrBmp = null },
            title = { Text(t("dialog.qrTitle")) },
            text = {
                Image(qrBmp!!.asImageBitmap(), null, Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)))
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = android.content.ContentValues().let { v ->
                        v.put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "cms2027-qr.png")
                        v.put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                        val u = ctx.contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v)
                        if (u != null) ctx.contentResolver.openOutputStream(u)?.use { qrBmp!!.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        u
                    }
                    if (uri != null) {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        ctx.startActivity(Intent.createChooser(share, t("dialog.qrDownload")))
                    }
                }) { Text(t("dialog.qrDownload")) }
            },
            dismissButton = { TextButton({ qrBmp = null }) { Text(t("dialog.close")) } },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun HistoryScreen(store: AppStore, settings: AppSettings, history: List<HistoryItem>) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("all") }
    var mode by remember { mutableStateOf("all") }
    var time by remember { mutableStateOf("all") }
    var selecting by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var confirm by remember { mutableStateOf<String?>(null) }
    var qrBmp by remember { mutableStateOf<Bitmap?>(null) }
    fun t(k: String) = S.t(settings.language, k)
    val now = System.currentTimeMillis()
    val filtered = history.filter { item ->
        if (type != "all" && item.type != type) return@filter false
        if (mode != "all" && item.mode != mode) return@filter false
        if (time == "today") {
            val start = now - ((now + 8 * 3600000) % 86400000)
            if (item.ts < start) return@filter false
        } else if (time == "7d" && item.ts < now - 7L * 86400000) return@filter false
        else if (time == "30d" && item.ts < now - 30L * 86400000) return@filter false
        if (query.isNotBlank()) {
            val blob = (item.plain + " " + item.cipher + " " + (item.key ?: "")).lowercase()
            if (!blob.contains(query.lowercase())) return@filter false
        }
        true
    }
    val fmt = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).apply { timeZone = TimeZone.getTimeZone("GMT+8") }
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(t("history.title"), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), placeholder = { Text(t("history.search")) }, shape = RoundedCornerShape(28.dp), singleLine = true)
        FlowRow(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("all" to "history.typeAll", "encrypt" to "history.typeEncrypt", "decrypt" to "history.typeDecrypt").forEach { (v, k) ->
                FilterChip(type == v, { type = v }, { Text(t(k)) })
            }
            listOf("all" to "history.modeAll", "normal" to "history.modeNormal", "strong" to "history.modeStrong").forEach { (v, k) ->
                FilterChip(mode == v, { mode = v }, { Text(t(k)) })
            }
            listOf("all" to "history.timeAll", "today" to "history.timeToday", "7d" to "history.time7d", "30d" to "history.time30d").forEach { (v, k) ->
                FilterChip(time == v, { time = v }, { Text(t(k)) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (selecting) {
                TextButton({ selected = filtered.map { it.id }.toSet() }) { Text(t("history.selectAll")) }
                TextButton({ confirm = "delete" }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text(t("history.deleteSelected")) }
                TextButton({ selecting = false; selected = emptySet() }) { Text(t("history.cancel")) }
            } else {
                TextButton({ selecting = true }) { Text(t("history.select")) }
                TextButton({ confirm = "clear" }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text(t("history.clearAll")) }
            }
        }
        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(t("history.empty"), fontWeight = FontWeight.Bold)
                    Text(t("history.emptyHint"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn {
                items(filtered, key = { it.id }) { item ->
                    Card(
                        Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerLow),
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            if (selecting) {
                                Checkbox(item.id in selected, { on ->
                                    selected = if (on) selected + item.id else selected - item.id
                                })
                            }
                            Column(Modifier.weight(1f)) {
                                Row {
                                    Assist(t(if (item.type == "encrypt") "history.typeEncrypt" else "history.typeDecrypt"))
                                    Spacer(Modifier.width(6.dp))
                                    Assist(t(if (item.mode == "strong") "history.modeStrong" else "history.modeNormal"))
                                }
                                Text(item.plain.take(80), maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
                                Text(item.cipher.take(80), maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    fmt.format(Date(item.ts)) + if (item.key != null) " · ${t("history.keyLabel")} ${item.key}" else "",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }
                            Column {
                                IconButton({
                                    val cm = ctx.getSystemService(ClipboardManager::class.java)
                                    cm.setPrimaryClip(ClipData.newPlainText("cms", item.cipher))
                                    Toast.makeText(ctx, t("home.copied"), Toast.LENGTH_SHORT).show()
                                }) { Icon(Icons.Filled.ContentCopy, null) }
                                IconButton({ qrBmp = Qr.encode(item.cipher) }) { Icon(Icons.Filled.QrCode, null) }
                                IconButton({
                                    scope.launch { store.deleteHistory(setOf(item.id)) }
                                }) { Icon(Icons.Filled.Delete, null) }
                            }
                        }
                    }
                }
            }
        }
    }
    if (confirm != null) {
        AlertDialog(
            onDismissRequest = { confirm = null },
            text = { Text(t(if (confirm == "clear") "history.clearConfirm" else "history.deleteConfirm")) },
            confirmButton = {
                TextButton({
                    scope.launch {
                        if (confirm == "clear") store.clearHistory() else store.deleteHistory(selected)
                        selecting = false
                        selected = emptySet()
                        confirm = null
                    }
                }) { Text(t("dialog.confirm")) }
            },
            dismissButton = { TextButton({ confirm = null }) { Text(t("dialog.cancel")) } },
        )
    }
    if (qrBmp != null) {
        AlertDialog(
            onDismissRequest = { qrBmp = null },
            title = { Text(t("dialog.qrTitle")) },
            text = { Image(qrBmp!!.asImageBitmap(), null, Modifier.fillMaxWidth()) },
            confirmButton = { TextButton({ qrBmp = null }) { Text(t("dialog.close")) } },
        )
    }
}

@Composable
private fun Assist(text: String) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.tertiaryContainer) {
        Text(text, Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SettingsScreen(store: AppStore, settings: AppSettings) {
    val scope = rememberCoroutineScope()
    fun t(k: String) = S.t(settings.language, k)
    fun update(block: AppSettings.() -> AppSettings) {
        scope.launch { store.saveSettings(settings.block()) }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.padding(16.dp)) {
                Text(t("settings.appearance"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text(t("settings.theme"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    listOf("system" to "settings.themeSystem", "light" to "settings.themeLight", "dark" to "settings.themeDark").forEachIndexed { i, (v, k) ->
                        SegmentedButton(settings.themeMode == v, { update { copy(themeMode = v) } }, SegmentedButtonDefaults.itemShape(i, 3)) { Text(t(k)) }
                    }
                }
                Text(t("settings.color"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    SegmentedButton(settings.colorMode == "system", { update { copy(colorMode = "system") } }, SegmentedButtonDefaults.itemShape(0, 2)) { Text(t("settings.colorDefault")) }
                    SegmentedButton(settings.colorMode == "custom", { update { copy(colorMode = "custom") } }, SegmentedButtonDefaults.itemShape(1, 2)) { Text(t("settings.colorCustom")) }
                }
                if (settings.colorMode == "custom") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PRESET_SEEDS.forEach { seed ->
                            val selected = settings.seed == seed.seed
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(androidx.compose.ui.graphics.Color(seed.seed))
                                    .then(if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                                    .clickable { update { copy(seed = seed.seed) } },
                            )
                        }
                    }
                }
            }
        }
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.padding(16.dp)) {
                Text(t("settings.language"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                listOf("auto", "zh-CN", "zh-TW", "en", "ru", "fr", "de", "la").forEach { id ->
                    Row(
                        Modifier.fillMaxWidth().clickable { update { copy(language = id) } }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(settings.language == id, { update { copy(language = id) } })
                        Text(if (id == "auto") t("settings.langAuto") else t("lang.$id"))
                    }
                }
            }
        }
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.padding(16.dp)) {
                Text(t("settings.about"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(t("settings.appDesc"), Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                AboutRow(t("settings.repo"), "github.com/clcwpwqi/CMS-2027")
                AboutRow(t("settings.developer"), "clcwpwqi")
                AboutRow("Telegram", "@clcwpwqi")
                AboutRow("WeChat", "@clcwpwqi")
                AboutRow("Email", "clcwpwqi@outlook.com")
                AboutRow(t("settings.algorithm"), t("settings.algoDetail"))
            }
        }
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, modifier = Modifier.padding(start = 12.dp), maxLines = 2)
    }
}
