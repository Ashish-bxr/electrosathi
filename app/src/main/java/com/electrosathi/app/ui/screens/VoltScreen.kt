package com.electrosathi.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.electrosathi.app.R
import com.electrosathi.app.data.ChatStore
import com.electrosathi.app.data.DocKind
import com.electrosathi.app.data.Lang
import com.electrosathi.app.data.DocReadResult
import com.electrosathi.app.data.DocReader
import com.electrosathi.app.data.ModelDownloadState
import com.electrosathi.app.data.ModelDownloader
import com.electrosathi.app.data.VoltEngine
import com.electrosathi.app.data.VoltState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Volt — Batch 4 Part 2 restyle, from Ashish's reference board: robot
 * avatar header, chat bubbles and a pill input bar.
 *
 * Batch 9: Volt's real on-device brain is wired in. When the Llama
 * model files are present in the app's volt/ folder, answers stream
 * from [VoltEngine] (Llama 3.2 1B via ExecuTorch, fully offline) and
 * a small caption under the answer reports the on-device speed.
 * When the model is not installed, Volt falls back to honest canned
 * replies — the screen never goes dead. The status line always
 * reflects the real engine state.
 *
 * Batch 11: when the model is missing, a download card offers the
 * one-time in-app download (1.1 GB, Wi-Fi recommended, resumable) via
 * [ModelDownloader]; when it finishes, the engine re-checks and loads
 * without an app restart.
 *
 * Batch 12 — Volt control fixes: while Volt is answering, the round
 * send button becomes a red Stop button (a stopped answer stays on
 * screen as a partial, honestly captioned); Quiz mode now asks ONE
 * question and waits for the reply instead of generating questions
 * non-stop; and the chat list follows the answer as it streams,
 * unless the user has scrolled up to read — then it leaves them be.
 *
 * Batch 13 — Copy + Attach: long-pressing any chat bubble copies its
 * text. The attach button lets the student share a photo or a PDF;
 * [DocReader] reads the file's TEXT on-device (Volt's brain is
 * text-only — it never sees the picture itself) and that text rides
 * along with the question as context, with a bigger reading budget.
 *
 * Batch 15 — the conversation is stored in SQLite ([ChatStore]), as
 * the mentoring session requires: every question and answer is saved
 * the moment it appears, and opening this screen reloads the whole
 * history. The caption under each answer now also shows the TOTAL
 * inference time next to the tokens/sec speed.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun VoltScreen() {
    // Pair(first = isUserMessage, second = text)
    val messages = remember { mutableStateListOf<Pair<Boolean, String>>() }
    var input by remember { mutableStateOf("") }
    var typing by remember { mutableStateOf(false) }
    val modes = listOf("Explain", "Quiz me", "Debug", "Plan my week")
    var mode by remember { mutableStateOf(modes[0]) }

    val context = LocalContext.current
    val voltState by VoltEngine.state.collectAsState()
    val liveAnswer by VoltEngine.answer.collectAsState()
    val tps by VoltEngine.tokensPerSecond.collectAsState()
    val totalSec by VoltEngine.totalInferenceSeconds.collectAsState()
    var streaming by remember { mutableStateOf(false) }
    var statsLine by remember { mutableStateOf("") }

    // Batch 11: one-time in-app model download (shown when NO_MODEL).
    val dlState by ModelDownloader.state.collectAsState()
    var showMobileDataDialog by remember { mutableStateOf(false) }

    // Batch 13: copy + attach. The clipboard serves long-press copy;
    // the scope runs DocReader off the picker's callback.
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    // Batch 13: the shared file's extracted text, waiting to ride
    // along with the next question; a "Reading…" flag while DocReader
    // works; and a short note when a file held no readable text.
    var attachment by remember { mutableStateOf<DocReadResult.Ready?>(null) }
    var readingDoc by remember { mutableStateOf(false) }
    var docNote by remember { mutableStateOf("") }

    // System document picker — images and PDFs only, and no storage
    // permission is needed for files the student picks themselves.
    val docPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            docNote = ""
            readingDoc = true
            scope.launch {
                when (val result = DocReader.readText(context, uri)) {
                    is DocReadResult.Ready -> attachment = result
                    DocReadResult.Empty -> {
                        attachment = null
                        docNote = "No readable text found in that file"
                    }
                    is DocReadResult.Error -> {
                        attachment = null
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    }
                }
                readingDoc = false
            }
        }
    }

    // Batch 12: chat auto-scroll. The list follows the newest text
    // while Volt streams — unless the user scrolls up to read, which
    // turns following off until they come back to the bottom (or ask
    // a new question). autoScrolling marks our own programmatic
    // scrolls so they are never mistaken for the user's.
    val listState = rememberLazyListState()
    var followTail by remember { mutableStateOf(true) }
    var autoScrolling by remember { mutableStateOf(false) }

    // Pin the end of the conversation to the end of the viewport:
    // engage the last item, then push any part of it that still hangs
    // below the viewport (a long streaming bubble) into view.
    suspend fun pinToBottom() {
        val total = listState.layoutInfo.totalItemsCount
        if (total == 0) return
        autoScrolling = true
        try {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            if (lastVisible == null || lastVisible.index < total - 1) {
                listState.scrollToItem(total - 1)
            }
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()
            if (last != null && last.index == info.totalItemsCount - 1) {
                val overflow = (last.offset + last.size) - info.viewportEndOffset
                if (overflow > 0) listState.scroll { scrollBy(overflow.toFloat()) }
            }
        } finally {
            autoScrolling = false
        }
    }

    fun requestDownload() {
        if (ModelDownloader.isOnUnmeteredNetwork(context)) {
            ModelDownloader.start(context)
        } else {
            showMobileDataDialog = true
        }
    }

    fun send() {
        val typed = input.trim()
        val doc = attachment
        if (typing || streaming) return
        // Batch 13: an attachment alone is sendable — the question
        // then defaults to a plain "explain this".
        if (typed.isEmpty() && doc == null) return
        val question = if (typed.isEmpty()) "Explain this in simple words." else typed
        val userBubble = (if (doc != null) "📎 " else "") + question
        messages.add(true to userBubble)
        // Batch 15: the question is saved to SQLite the moment it is asked.
        ChatStore.add(context, "user", userBubble)
        input = ""
        statsLine = ""
        docNote = ""
        attachment = null // the shared text goes with this one question, then clears
        followTail = true // a new question always jumps back to the tail
        if (voltState == VoltState.READY) {
            streaming = true
            messages.add(false to "…")
            // Batch 12: Quiz mode asks must not run away — instruct the
            // engine to ask exactly ONE question (options A–D, answer
            // NOT revealed) and then stop and wait for the reply. The
            // instruction rides along to the engine only; the student's
            // own bubble keeps their original words.
            var engineText = if (mode == "Quiz me") {
                question + "\n\nQuiz me on this: ask exactly ONE question with options " +
                    "A, B, C and D. Do not reveal the correct answer. " +
                    "Then stop and wait for my reply."
            } else {
                question
            }
            // Batch 13: with a shared file, its extracted text becomes
            // the context and the question gets the bigger budget.
            if (doc != null) {
                engineText = "Text from a file the student shared:\n\"\"\"\n" + doc.text +
                    "\n\"\"\"\n\nStudent's question: " + engineText
            }
            // Batch 16: when Hindi is the chosen language, Volt answers
            // in Hindi. Like the quiz instruction, this rides to the
            // engine only — the student's bubble keeps their own words.
            if (Lang.isHindi) {
                engineText += "\n\nPlease answer in Hindi (Devanagari script), " +
                    "in simple, easy words."
            }
            // Batch 17: a plain question carries the previous exchange
            // along, so a follow-up ("how many pins?") keeps its topic.
            // The pair is read from the visible chat — which Batch 15
            // reloads from SQLite — so the memory survives restarts
            // too. Quiz-me and file questions stay single-turn: their
            // instructions must not be diluted.
            if (doc == null && mode != "Quiz me") {
                val prevVoltIdx = messages.indices.lastOrNull {
                    !messages[it].first && messages[it].second != "…"
                }
                val prevUserIdx = if (prevVoltIdx != null) {
                    (prevVoltIdx - 1 downTo 0).firstOrNull { messages[it].first }
                } else {
                    null
                }
                VoltEngine.ask(
                    engineText,
                    seqLen = 512,
                    previousQuestion = prevUserIdx?.let {
                        messages[it].second.removePrefix("📎 ")
                    },
                    previousAnswer = prevVoltIdx?.let { messages[it].second }
                )
            } else {
                VoltEngine.ask(engineText, seqLen = if (doc != null) 1024 else 512)
            }
        } else {
            typing = true
        }
    }

    // Canned path — used only while the real brain is unavailable.
    LaunchedEffect(typing) {
        if (typing) {
            delay(900)
            val reply = when (voltState) {
                VoltState.NO_MODEL ->
                    "My on-device brain isn't installed on this phone yet — the Llama " +
                        "model (about 1.1 GB) is added to the app separately so I stay " +
                        "offline and private. Until it's here, the Study section has " +
                        "real lessons on this topic!"
                VoltState.LOADING ->
                    "I'm still waking up — my brain takes a few seconds to load. " +
                        "Ask me again in a moment!"
                VoltState.ERROR -> {
                    val detail = VoltEngine.errorMessage.value
                    "My brain hit a snag while loading" +
                        (if (detail.isNotBlank()) ": $detail" else "") +
                        ". Please try again in a bit — meanwhile, the Study section can help."
                }
                else ->
                    "I'm Volt! My on-device brain (Llama via ExecuTorch) is warming up. " +
                        "Explore the Study section meanwhile."
            }
            messages.add(false to reply)
            // Batch 15: canned replies are part of the stored history too.
            ChatStore.add(context, "volt", reply)
            typing = false
        }
    }

    // While the engine streams, mirror the growing answer into the last bubble.
    LaunchedEffect(liveAnswer) {
        if (streaming && messages.isNotEmpty()) {
            messages[messages.lastIndex] = false to liveAnswer.ifEmpty { "…" }
        }
    }

    // Generation finished (or failed): close out the stream and show the speed caption.
    LaunchedEffect(voltState) {
        if (streaming && voltState == VoltState.READY) {
            streaming = false
            if (VoltEngine.answerStopped.value) {
                // Batch 12: the user pressed Stop. The partial answer
                // stays as the bubble; the caption says what happened.
                // If Stop landed before the first token, replace the
                // "…" placeholder instead of leaving it hanging.
                if (liveAnswer.isBlank() && messages.isNotEmpty()) {
                    messages[messages.lastIndex] =
                        false to "Stopped — Volt hadn't started answering yet. Ask me again!"
                }
                statsLine = buildString {
                    append("Stopped — partial answer kept")
                    if (totalSec > 0f) append(" · " + "%.1f".format(totalSec) + " s total")
                    if (tps > 0f) append(" · " + "%.1f".format(tps) + " tokens/sec")
                    append(" · Llama 3.2 1B via ExecuTorch")
                }
            } else if (tps > 0f || totalSec > 0f) {
                // Batch 15: the caption carries the TOTAL inference
                // time next to the speed — the pair the report quotes.
                statsLine = buildString {
                    append("Answered on-device")
                    if (totalSec > 0f) append(" · " + "%.1f".format(totalSec) + " s total")
                    if (tps > 0f) append(" · " + "%.1f".format(tps) + " tokens/sec")
                    append(" · Llama 3.2 1B via ExecuTorch")
                }
            }
            // Batch 15: the finished answer (or the kept partial) is
            // saved to SQLite, so the history survives leaving Volt.
            messages.lastOrNull()?.let { (isUser, text) ->
                if (!isUser && text.isNotBlank() && text != "…") {
                    ChatStore.add(context, "volt", text)
                }
            }
        }
        if (streaming && voltState == VoltState.ERROR) {
            streaming = false
            if (messages.isNotEmpty()) {
                messages[messages.lastIndex] =
                    false to "My brain hit a snag answering that. Please ask again in a moment."
            }
        }
    }

    // Batch 11: download finished — hand over to the engine, which
    // re-checks the folder and loads the model without an app restart.
    LaunchedEffect(dlState) {
        if (dlState is ModelDownloadState.Done) {
            VoltEngine.prepare(context)
        }
    }

    // Batch 12: watch the user's own scrolling — if they pull the
    // list away from the bottom, stop following the stream; when
    // they return to the bottom, follow again. Our programmatic
    // scrolls (autoScrolling) are ignored here.
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()
            val atEnd = info.totalItemsCount == 0 || last == null ||
                (last.index >= info.totalItemsCount - 1 &&
                    last.offset + last.size <= info.viewportEndOffset + 2)
            Triple(listState.isScrollInProgress, autoScrolling, atEnd)
        }.collect { (scrolling, auto, atEnd) ->
            if (scrolling && !auto) followTail = atEnd
        }
    }

    // Batch 12: follow the tail whenever the conversation grows —
    // a new message, a streamed token, the caption, the typing bubble.
    LaunchedEffect(messages.size, liveAnswer, statsLine, typing) {
        if (followTail) pinToBottom()
    }

    LaunchedEffect(Unit) {
        VoltEngine.prepare(context)
        if (messages.isEmpty()) {
            // Batch 15: reload the stored conversation from SQLite.
            // Only a phone with no history at all gets the greeting —
            // and the greeting itself is not stored, so it never
            // repeats at the top of a saved conversation.
            val stored = ChatStore.loadAll(context)
            if (stored.isNotEmpty()) {
                messages.addAll(stored)
            } else {
                messages.add(false to "Hi, I'm Volt — your offline electronics tutor. Ask me anything!")
            }
        }
    }

    val statusText = when (voltState) {
        VoltState.NO_MODEL -> "On-device • Model not installed"
        VoltState.LOADING -> "On-device • Waking up…"
        VoltState.ERROR -> "On-device • Load error"
        else -> "On-device • Offline · Llama 3.2 1B"
    }
    val statusDot = when (voltState) {
        VoltState.READY, VoltState.GENERATING -> Color(0xFF22C55E)
        VoltState.ERROR -> Color(0xFFEF4444)
        else -> Color(0xFFF59E0B)
    }

    Column(Modifier.fillMaxSize()) {
        // ---- Header: avatar, name, honest on-device status ----
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.volt_avatar),
                contentDescription = "Volt",
                modifier = Modifier.size(48.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    Lang.tr("Chat with Volt"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(8.dp)
                            .clip(CircleShape)
                            .background(statusDot)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ---- Mode chips (unchanged behaviour) ----
        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            modes.forEach { m ->
                FilterChip(
                    selected = mode == m,
                    onClick = { mode = m },
                    label = { Text(m) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ---- Batch 11: model download card (only while the brain is missing) ----
        if (voltState == VoltState.NO_MODEL) {
            ModelDownloadCard(
                dlState = dlState,
                hasPartial = ModelDownloader.hasPartialDownload(context),
                partialBytes = ModelDownloader.partialBytes(context),
                onDownloadClick = { requestDownload() },
                onCancelClick = { ModelDownloader.cancel() }
            )
            Spacer(Modifier.height(8.dp))
        }

        // ---- Messages ----
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { (isUser, text) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 18.dp, topEnd = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 18.dp
                        ),
                        color = if (isUser) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        // Batch 13: long-press any bubble — theirs or
                        // Volt's — to copy its full text.
                        modifier = Modifier.widthIn(max = 300.dp)
                            .combinedClickable(
                                onClick = { },
                                onLongClick = {
                                    clipboardManager.setText(AnnotatedString(text))
                                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                }
                            )
                    ) {
                        Text(
                            text,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isUser) Color.White
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            if (statsLine.isNotEmpty()) {
                item {
                    Text(
                        statsLine,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                    )
                }
            }
            if (typing) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Text(
                                "Volt is typing…",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ---- Batch 13: attachment status (reading / ready / no text) ----
        val currentDoc = attachment
        if (readingDoc) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Reading…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (currentDoc != null) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(start = 10.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (currentDoc.kind == DocKind.PDF) Icons.Filled.Description
                            else Icons.Filled.Image,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (currentDoc.kind == DocKind.PDF)
                                "PDF text ready · ${currentDoc.text.length} characters"
                            else
                                "Photo text ready · ${currentDoc.text.length} characters",
                            style = MaterialTheme.typography.bodySmall
                        )
                        IconButton(
                            onClick = { attachment = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Remove attachment",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        } else if (docNote.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(start = 10.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            docNote,
                            style = MaterialTheme.typography.bodySmall
                        )
                        IconButton(
                            onClick = { docNote = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Dismiss",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // ---- Input pill + round send button ----
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Batch 13: attach a photo or PDF for Volt to read (as text).
            IconButton(
                onClick = {
                    docPicker.launch(arrayOf("image/*", "application/pdf"))
                },
                enabled = !readingDoc,
                modifier = Modifier.size(50.dp)
            ) {
                Icon(Icons.Filled.AttachFile, contentDescription = "Attach a photo or PDF")
            }
            Spacer(Modifier.width(4.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(Lang.tr("Ask Volt anything...")) },
                singleLine = true,
                shape = RoundedCornerShape(50)
            )
            Spacer(Modifier.width(8.dp))
            // Batch 12: while Volt is answering, Send becomes Stop.
            if (voltState == VoltState.GENERATING) {
                FilledIconButton(
                    onClick = { VoltEngine.stop() },
                    modifier = Modifier.size(50.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Filled.Stop, contentDescription = "Stop")
                }
            } else {
                FilledIconButton(
                    onClick = { send() },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }

    // Batch 11: mobile-data guard — 1.1 GB is worth one honest question.
    if (showMobileDataDialog) {
        AlertDialog(
            onDismissRequest = { showMobileDataDialog = false },
            title = { Text("Use mobile data?") },
            text = {
                Text(
                    "You're on mobile data — this is a one-time 1.1 GB download. " +
                        "On Wi-Fi it's free and usually faster. Continue anyway?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMobileDataDialog = false
                        ModelDownloader.start(context)
                    }
                ) { Text("Continue anyway") }
            },
            dismissButton = {
                TextButton(onClick = { showMobileDataDialog = false }) { Text("Cancel") }
            }
        )
    }
}

/**
 * Batch 11 — the card that offers Volt's one-time brain download while
 * the model isn't on this phone. Every state of [ModelDownloader] gets
 * an honest label; the chat underneath keeps working with canned
 * replies the whole time.
 */
@Composable
private fun ModelDownloadCard(
    dlState: ModelDownloadState,
    hasPartial: Boolean,
    partialBytes: Long,
    onDownloadClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            when (dlState) {
                ModelDownloadState.Idle, ModelDownloadState.Cancelled -> {
                    val paused = dlState == ModelDownloadState.Cancelled || hasPartial
                    Text(
                        if (paused) "Volt's brain is partly downloaded"
                        else "Volt's brain isn't on this phone yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (paused)
                            "About ${formatMb(partialBytes)} of 1.1 GB is already saved " +
                                "on this phone. Resume to finish the one-time download — " +
                                "Wi-Fi recommended."
                        else
                            "Volt thinks with a Llama model that lives on this phone, so " +
                                "it works fully offline. It needs a one-time download of " +
                                "1.1 GB — Wi-Fi recommended.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = onDownloadClick) {
                        Text(if (paused) "Resume download" else "Download Volt's brain")
                    }
                }

                is ModelDownloadState.Downloading -> {
                    Text(
                        "Downloading Volt's brain…",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Now: the ${dlState.fileLabel} · ${dlState.percent}% · " +
                            "${formatMb(dlState.overallDone)} of ${formatMb(dlState.overallTotal)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = dlState.percent / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onCancelClick) { Text("Cancel") }
                    }
                }

                ModelDownloadState.Verifying -> {
                    Text(
                        "Checking the download…",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                ModelDownloadState.Done -> {
                    Text(
                        "Download complete ✓",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Waking Volt up — loading the brain takes a few seconds.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is ModelDownloadState.Error -> {
                    Text(
                        "Download didn't finish",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        dlState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = onDownloadClick) {
                        Text(if (hasPartial) "Resume" else "Try again")
                    }
                }
            }
        }
    }
}

private fun formatMb(bytes: Long): String = "%.0f MB".format(bytes / 1_000_000.0)
