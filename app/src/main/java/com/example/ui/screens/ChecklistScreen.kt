package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.example.ui.components.IconifyIcons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ChecklistItemEntity
import com.example.data.local.ChecklistType
import com.example.ui.components.AppMotion
import com.example.ui.components.GlassCard
import com.example.ui.components.pressBounceScale
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.GlassBorderWhite
import com.example.ui.theme.OnPrimary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryFixed

@Composable
fun ChecklistScreen(
    type: ChecklistType,
    items: List<ChecklistItemEntity>,
    onToggleItem: (ChecklistItemEntity) -> Unit,
    onAddItem: (String) -> Unit,
    onClearCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newItemText by remember { mutableStateOf("") }
    val total = items.size
    val done = items.count { it.isCompleted }
    val remaining = total - done

    val remainingLabel = when {
        total == 0 -> "List is empty"
        type == ChecklistType.TODO -> if (remaining == 1) "1 task left" else "$remaining tasks left"
        else -> if (remaining == 1) "1 item left" else "$remaining items left"
    }

    val submitNewItem = {
        if (newItemText.trim().isNotEmpty()) {
            onAddItem(newItemText)
            newItemText = ""
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Progress & Summary Header
            val animatedProgress by animateFloatAsState(
                targetValue = if (total == 0) 0f else done.toFloat() / total.toFloat(),
                animationSpec = AppMotion.SpatialSpring,
                label = "checklist_progress"
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(0),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedContent(
                        targetState = remainingLabel,
                        transitionSpec = {
                            fadeIn(tween(180)).togetherWith(fadeOut(tween(140)))
                        },
                        label = "remaining_label_anim"
                    ) { lbl ->
                        Text(
                            text = lbl,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, GlassBorderWhite),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = IconifyIcons.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            AnimatedContent(
                                targetState = "$done / $total",
                                transitionSpec = {
                                    (slideInVertically { it / 2 } + fadeIn(tween(180)))
                                        .togetherWith(slideOutVertically { -it / 2 } + fadeOut(tween(140)))
                                },
                                label = "done_count_anim"
                            ) { countText ->
                                Text(
                                    text = countText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.55f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer)
                    )
                }
            }

            // Main Frosted Glass Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(1)
                    .animateContentSize(
                        animationSpec = spring(dampingRatio = 0.80f, stiffness = Spring.StiffnessMediumLow)
                    ),
                cornerRadius = 24.dp,
                alpha = 0.65f
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (items.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PrimaryFixed,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (type == ChecklistType.SHOPPING) IconifyIcons.ShoppingBag else IconifyIcons.Checklist,
                                        contentDescription = null,
                                        tint = PrimaryContainer
                                    )
                                }
                            }
                            Text(
                                text = stringResource(
                                    if (type == ChecklistType.SHOPPING) R.string.empty_shopping_title
                                    else R.string.empty_todo_title
                                ),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(
                                    if (type == ChecklistType.SHOPPING) R.string.empty_shopping_subtitle
                                    else R.string.empty_todo_subtitle
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items.forEach { item ->
                            val rowInteraction = remember(item.id) { MutableInteractionSource() }
                            val boxBg by animateColorAsState(
                                targetValue = if (item.isCompleted) PrimaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.85f),
                                animationSpec = tween(220),
                                label = "check_bg"
                            )
                            val checkPopScale by animateFloatAsState(
                                targetValue = if (item.isCompleted) 1f else 0.92f,
                                animationSpec = AppMotion.BouncySpring,
                                label = "check_pop"
                            )
                            val rowTextColor by animateColorAsState(
                                targetValue = if (item.isCompleted) {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                animationSpec = tween(220),
                                label = "row_text_color"
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .pressBounceScale(rowInteraction, pressedScale = 0.98f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(
                                        interactionSource = rowInteraction,
                                        indication = androidx.compose.material3.ripple(),
                                        role = Role.Checkbox
                                    ) { onToggleItem(item) }
                                    .padding(horizontal = 8.dp)
                                    .testTag("checklist_row_${item.id}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .graphicsLayer {
                                                scaleX = checkPopScale
                                                scaleY = checkPopScale
                                            }
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(boxBg)
                                            .then(
                                                if (item.isCompleted) Modifier
                                                else Modifier.border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = item.isCompleted,
                                            enter = scaleIn(
                                                initialScale = 0.5f,
                                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                            ) + fadeIn(tween(150)),
                                            exit = scaleOut(targetScale = 0.5f) + fadeOut(tween(120))
                                        ) {
                                            Icon(
                                                imageVector = IconifyIcons.Check,
                                                contentDescription = "Checked",
                                                tint = OnPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = rowTextColor,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Clear Completed Action
            val clearAlpha by animateFloatAsState(
                targetValue = if (done > 0) 1f else 0.45f,
                animationSpec = tween(200),
                label = "clear_alpha"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onClearCompleted,
                    enabled = done > 0,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .alpha(clearAlpha)
                        .testTag("checklist_clear_completed_button")
                ) {
                    Icon(
                        imageVector = IconifyIcons.CleanSweep,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Clear completed",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Add Item Input Bar
            val addInteraction = remember { MutableInteractionSource() }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(2),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    placeholder = {
                        Text(
                            if (type == ChecklistType.SHOPPING) "Add an item..." else "Add a task..."
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submitNewItem() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.78f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.70f),
                        focusedBorderColor = PrimaryContainer,
                        unfocusedBorderColor = GlassBorderWhite
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("checklist_add_input")
                )
                Button(
                    onClick = submitNewItem,
                    interactionSource = addInteraction,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryContainer,
                        contentColor = OnPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .minimumInteractiveComponentSize()
                        .pressBounceScale(addInteraction, pressedScale = 0.94f)
                        .testTag("checklist_add_button")
                ) {
                    Text("Add", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
