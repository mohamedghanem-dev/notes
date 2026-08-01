package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.util.LocalAppStrings

data class LinePath(
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
    val isEraser: Boolean = false
)

@Composable
fun DrawingCanvasModal(
    onDismiss: () -> Unit,
    onSaveDrawing: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var paths by remember { mutableStateOf(listOf<LinePath>()) }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var strokeWidth by remember { mutableFloatStateOf(8f) }
    var isEraser by remember { mutableStateOf(false) }

    val colorList = listOf(
        Color.Black,
        Color(0xFFDC2626), // Red
        Color(0xFF2563EB), // Blue
        Color(0xFF16A34A), // Green
        Color(0xFFCA8A04), // Yellow
        Color(0xFF9333EA)  // Purple
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.fillMaxWidth(0.95f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.drawingPad,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(
                    onClick = {
                        paths = emptyList()
                    },
                    modifier = Modifier.testTag("clear_drawing_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = strings.clearDrawing,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                // Drawing Canvas Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .pointerInput(isEraser, selectedColor, strokeWidth) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath = Path().apply {
                                        moveTo(offset.x, offset.y)
                                    }
                                },
                                onDrag = { change, _ ->
                                    currentPath?.lineTo(change.position.x, change.position.y)
                                    // Trigger recomposition
                                    currentPath = Path().apply {
                                        currentPath?.let { addPath(it) }
                                    }
                                },
                                onDragEnd = {
                                    currentPath?.let { p ->
                                        paths = paths + LinePath(
                                            path = p,
                                            color = if (isEraser) Color.White else selectedColor,
                                            strokeWidth = if (isEraser) strokeWidth * 2.5f else strokeWidth,
                                            isEraser = isEraser
                                        )
                                    }
                                    currentPath = null
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        paths.forEach { line ->
                            drawPath(
                                path = line.path,
                                color = line.color,
                                style = Stroke(
                                    width = line.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                        currentPath?.let { p ->
                            drawPath(
                                path = p,
                                color = if (isEraser) Color.White else selectedColor,
                                style = Stroke(
                                    width = if (isEraser) strokeWidth * 2.5f else strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Drawing Tools Toolbar (Matching Samsung Notes drawing toolbar style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tool toggle (Pen vs Eraser)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                    ) {
                        IconButton(
                            onClick = { isEraser = false },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (!isEraser) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Pen",
                                tint = if (!isEraser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { isEraser = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isEraser) MaterialTheme.colorScheme.errorContainer else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = "Eraser",
                                tint = if (isEraser) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Color palette chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        colorList.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selectedColor == color && !isEraser) 3.dp else 1.dp,
                                        color = if (selectedColor == color && !isEraser) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .pointerInput(Unit) {
                                        detectDragGestures { _, _ -> }
                                    }
                                    .border(0.dp, Color.Transparent)
                                    .padding(2.dp)
                            ) {
                                Button(
                                    onClick = {
                                        selectedColor = color
                                        isEraser = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {}
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // For local canvas representation, pass path count or simple vector string indicator
                    onSaveDrawing("[Sketch Note - ${paths.size} strokes]")
                    onDismiss()
                },
                modifier = Modifier.testTag("save_drawing_button")
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_drawing_button")
            ) {
                Text(strings.cancel)
            }
        }
    )
}
