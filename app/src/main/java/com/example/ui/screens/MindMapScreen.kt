package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.local.MindMapEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.IconifyIcons
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.GlassBorderWhite
import com.example.ui.theme.OnPrimary
import com.example.ui.theme.PrimaryContainer
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private data class MindNode(
    val id: String,
    val label: String,
    val parentId: String?,
    val x: Float,
    val y: Float,
    val colorHex: Long
)

private val MIND_NODE_COLORS = listOf(
    0xFF3B66F5L,
    0xFF7B61FFL,
    0xFFE55D87L,
    0xFF1F9D8BL
)

@Composable
fun MindMapScreen(
    mindMaps: List<MindMapEntity>,
    onSaveMindMap: (MindMapEntity) -> Unit,
    onCreateMindMap: (String) -> Unit,
    onDeleteMindMap: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMapId by remember { mutableStateOf<Long?>(null) }
    val activeMap = remember(mindMaps, selectedMapId) {
        mindMaps.firstOrNull { it.id == selectedMapId } ?: mindMaps.firstOrNull()
    }
    LaunchedEffect(activeMap?.id) {
        if (activeMap != null) selectedMapId = activeMap.id
    }

    val nodes = remember(activeMap?.id) {
        mutableStateListOf<MindNode>().apply {
            addAll(parseNodes(activeMap?.nodesJson))
        }
    }
    LaunchedEffect(activeMap?.id, activeMap?.nodesJson) {
        val parsed = parseNodes(activeMap?.nodesJson)
        if (nodes.toList() != parsed) {
            nodes.clear()
            nodes.addAll(parsed)
        }
    }

    var selectedNodeId by remember(activeMap?.id) { mutableStateOf(nodes.firstOrNull()?.id ?: "root") }
    var showNewMapDialog by remember { mutableStateOf(false) }
    var newMapTitle by remember { mutableStateOf("") }

    fun persistNodes() {
        val map = activeMap ?: return
        onSaveMindMap(
            map.copy(
                nodesJson = encodeNodes(nodes),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Board Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(0),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    mindMaps.forEach { map ->
                        val isSelected = map.id == activeMap?.id
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isSelected) PrimaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.78f),
                            border = BorderStroke(1.dp, GlassBorderWhite),
                            modifier = Modifier
                                .height(44.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { selectedMapId = map.id }
                                .testTag("mindmap_tab_${map.id}")
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = map.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) OnPrimary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = { showNewMapDialog = true },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("mindmap_create_button")
                ) {
                    Icon(
                        imageVector = IconifyIcons.Plus,
                        contentDescription = "New mind map",
                        tint = PrimaryContainer
                    )
                }

                if (mindMaps.size > 1 && activeMap != null) {
                    IconButton(
                        onClick = { onDeleteMindMap(activeMap.id) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("mindmap_delete_board_button")
                    ) {
                        Icon(
                            imageVector = IconifyIcons.Delete,
                            contentDescription = "Delete mind map",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Interactive Mind Map Canvas
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .staggeredEntrance(1),
                cornerRadius = 24.dp,
                alpha = 0.76f
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val density = LocalDensity.current
                    val widthPx = with(density) { maxWidth.toPx() }
                    val heightPx = with(density) { maxHeight.toPx() }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val nodeById = nodes.associateBy { it.id }
                        nodes.forEach { node ->
                            val parent = node.parentId?.let { nodeById[it] }
                            if (parent != null) {
                                val start = Offset(parent.x * size.width, parent.y * size.height)
                                val end = Offset(node.x * size.width, node.y * size.height)
                                val midX = (start.x + end.x) / 2f
                                val path = Path().apply {
                                    moveTo(start.x, start.y)
                                    cubicTo(midX, start.y, midX, end.y, end.x, end.y)
                                }
                                drawPath(
                                    path = path,
                                    color = Color(node.colorHex).copy(alpha = 0.55f),
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }
                    }

                    nodes.forEach { node ->
                        val isSelected = node.id == selectedNodeId
                        val nodeColor = Color(node.colorHex)
                        val offsetX = (node.x * widthPx - with(density) { 58.dp.toPx() }).roundToInt()
                        val offsetY = (node.y * heightPx - with(density) { 22.dp.toPx() }).roundToInt()

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isSelected) nodeColor else Color.White.copy(alpha = 0.94f),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.5.dp,
                                color = nodeColor
                            ),
                            shadowElevation = if (isSelected) 8.dp else 4.dp,
                            modifier = Modifier
                                .offset { IntOffset(offsetX, offsetY) }
                                .widthIn(min = 96.dp, max = 140.dp)
                                .pointerInput(node.id, widthPx, heightPx) {
                                    detectTapGestures {
                                        selectedNodeId = node.id
                                    }
                                }
                                .pointerInput(node.id, widthPx, heightPx) {
                                    detectDragGestures(
                                        onDragStart = { selectedNodeId = node.id },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val idx = nodes.indexOfFirst { it.id == node.id }
                                            if (idx >= 0 && widthPx > 0f && heightPx > 0f) {
                                                val current = nodes[idx]
                                                nodes[idx] = current.copy(
                                                    x = (current.x + dragAmount.x / widthPx).coerceIn(0.10f, 0.90f),
                                                    y = (current.y + dragAmount.y / heightPx).coerceIn(0.10f, 0.90f)
                                                )
                                            }
                                        },
                                        onDragEnd = { persistNodes() }
                                    )
                                }
                                .testTag("mindmap_node_${node.id}")
                        ) {
                            Text(
                                text = node.label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (node.parentId == null) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // Selected Node Inspector
            val selectedNode = nodes.firstOrNull { it.id == selectedNodeId } ?: nodes.firstOrNull()
            if (selectedNode != null) {
                var draftLabel by remember(selectedNode.id, activeMap?.id) {
                    mutableStateOf(selectedNode.label)
                }
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .staggeredEntrance(2),
                    cornerRadius = 24.dp,
                    alpha = 0.78f
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedNode.parentId == null) "Central Topic" else "Branch Node",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MIND_NODE_COLORS.forEach { hex ->
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color(hex))
                                            .clickable {
                                                val idx = nodes.indexOfFirst { it.id == selectedNode.id }
                                                if (idx >= 0) {
                                                    nodes[idx] = nodes[idx].copy(colorHex = hex)
                                                    persistNodes()
                                                }
                                            }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = draftLabel,
                                onValueChange = { draftLabel = it },
                                label = { Text("Node text") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("mindmap_node_label_input")
                            )
                            Button(
                                onClick = {
                                    val idx = nodes.indexOfFirst { it.id == selectedNode.id }
                                    if (idx >= 0) {
                                        nodes[idx] = nodes[idx].copy(
                                            label = draftLabel.trim().ifBlank { "Topic" }
                                        )
                                        persistNodes()
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryContainer,
                                    contentColor = OnPrimary
                                ),
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("mindmap_node_rename_apply")
                            ) {
                                Text("Update")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val angle = (nodes.size * 1.15f)
                                    val newId = "n_${System.currentTimeMillis()}"
                                    val child = MindNode(
                                        id = newId,
                                        label = "New Idea",
                                        parentId = selectedNode.id,
                                        x = (selectedNode.x + 0.24f * cos(angle)).coerceIn(0.14f, 0.86f),
                                        y = (selectedNode.y + 0.24f * sin(angle)).coerceIn(0.14f, 0.86f),
                                        colorHex = MIND_NODE_COLORS[nodes.size % MIND_NODE_COLORS.size]
                                    )
                                    nodes.add(child)
                                    selectedNodeId = newId
                                    persistNodes()
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryContainer,
                                    contentColor = OnPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("mindmap_add_branch_button")
                            ) {
                                Icon(IconifyIcons.Plus, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Add Branch")
                            }

                            if (selectedNode.parentId != null) {
                                IconButton(
                                    onClick = {
                                        val toRemove = collectSubtreeIds(nodes, selectedNode.id)
                                        nodes.removeAll { it.id in toRemove }
                                        selectedNodeId = nodes.firstOrNull()?.id ?: "root"
                                        persistNodes()
                                    },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("mindmap_delete_branch_button")
                                ) {
                                    Icon(
                                        imageVector = IconifyIcons.Delete,
                                        contentDescription = "Delete branch",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewMapDialog) {
        AlertDialog(
            onDismissRequest = { showNewMapDialog = false },
            title = { Text("New Mind Map") },
            text = {
                OutlinedTextField(
                    value = newMapTitle,
                    onValueChange = { newMapTitle = it },
                    label = { Text("Board title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCreateMindMap(newMapTitle)
                        newMapTitle = ""
                        showNewMapDialog = false
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewMapDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun parseNodes(json: String?): List<MindNode> {
    if (json.isNullOrBlank()) return emptyList()
    return runCatching {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            MindNode(
                id = obj.getString("id"),
                label = obj.getString("label"),
                parentId = if (obj.isNull("parentId")) null else obj.getString("parentId"),
                x = obj.optDouble("x", 0.5).toFloat().coerceIn(0.1f, 0.9f),
                y = obj.optDouble("y", 0.5).toFloat().coerceIn(0.1f, 0.9f),
                colorHex = obj.optLong("color", 0xFF3B66F5L)
            )
        }
    }.getOrDefault(emptyList())
}

private fun encodeNodes(nodes: List<MindNode>): String {
    val arr = JSONArray()
    nodes.forEach { n ->
        arr.put(
            JSONObject().apply {
                put("id", n.id)
                put("label", n.label)
                if (n.parentId == null) put("parentId", JSONObject.NULL) else put("parentId", n.parentId)
                put("x", n.x.toDouble())
                put("y", n.y.toDouble())
                put("color", n.colorHex)
            }
        )
    }
    return arr.toString()
}

private fun collectSubtreeIds(nodes: List<MindNode>, rootId: String): Set<String> {
    val result = mutableSetOf(rootId)
    var added = true
    while (added) {
        added = false
        nodes.forEach { node ->
            if (node.parentId in result && node.id !in result) {
                result.add(node.id)
                added = true
            }
        }
    }
    return result
}
