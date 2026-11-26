package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.FileCategory
import com.example.ui.theme.Error
import com.example.ui.theme.GlassBorderWhite
import com.example.ui.theme.GlassShadowNavy
import com.example.ui.theme.GradientBlue
import com.example.ui.theme.GradientBlush
import com.example.ui.theme.GradientLavender
import com.example.ui.theme.OnError
import com.example.ui.theme.OnErrorContainer
import com.example.ui.theme.OnPrimary
import com.example.ui.theme.OnTertiary
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryFixed
import com.example.ui.theme.TertiaryContainer

object AppRoutes {
    const val DESK = "desk"
    const val FILES = "files"
    const val TOOLS = "tools"
    const val SETTINGS = "settings"
    const val NOTE_EDITOR = "note_editor"
    const val VOICE_MEMOS = "voice_memos"
    const val SHOPPING_LIST = "shopping_list"
    const val TODO_LIST = "todo_list"
    const val IMAGE_EDITOR = "image_editor"
    const val PHOTO_NOTES = "photo_notes"
    const val MIND_MAP = "mind_map"
    const val PDF_VIEWER = "pdf_viewer/{fileId}"

    fun pdfViewerRoute(fileId: Long): String = "pdf_viewer/$fileId"
}

@Composable
fun PastelGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_pastel")
    val driftOne by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift_one"
    )
    val driftTwo by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift_two"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(GradientBlue, GradientLavender, GradientBlush),
                    start = Offset(0f, 0f),
                    end = Offset(1200f, 2000f)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val topCenter = Offset(
                x = size.width * (0.18f + 0.28f * driftOne),
                y = size.height * (0.14f + 0.14f * driftTwo)
            )
            val bottomCenter = Offset(
                x = size.width * (0.82f - 0.30f * driftTwo),
                y = size.height * (0.78f - 0.18f * driftOne)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                    center = topCenter,
                    radius = size.maxDimension * 0.52f
                ),
                center = topCenter,
                radius = size.maxDimension * 0.52f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GradientLavender.copy(alpha = 0.42f), Color.Transparent),
                    center = bottomCenter,
                    radius = size.maxDimension * 0.56f
                ),
                center = bottomCenter,
                radius = size.maxDimension * 0.56f
            )
        }
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    alpha: Float = 0.68f,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val baseModifier = modifier
        .then(
            if (onClick != null) {
                Modifier.pressBounceScale(interactionSource, pressedScale = 0.965f)
            } else {
                Modifier
            }
        )
        .shadow(
            elevation = 10.dp,
            shape = shape,
            ambientColor = GlassShadowNavy,
            spotColor = GlassShadowNavy
        )
    Surface(
        modifier = if (onClick != null) {
            baseModifier
                .clip(shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = androidx.compose.material3.ripple(),
                    role = Role.Button,
                    onClick = onClick
                )
        } else {
            baseModifier
        },
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = alpha),
        border = BorderStroke(1.dp, GlassBorderWhite)
    ) {
        Column(content = content)
    }
}

@Composable
fun GlassTopBar(
    title: String,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    onTitleClick: (() -> Unit)? = null,
    isTitleEditing: Boolean = false,
    titleDraft: String = title,
    onTitleDraftChange: (String) -> Unit = {},
    onCommitTitleEdit: () -> Unit = {},
    searchExpanded: Boolean = false,
    searchQuery: String = "",
    searchPlaceholder: String = "Search files, notes, or memos…",
    onExpandSearch: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onCloseSearch: () -> Unit = {},
    trailingActions: (@Composable RowScope.() -> Unit)? = null,
    trailingEndActions: (@Composable RowScope.() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(28.dp)
    val pillColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.72f)
    val searchEndPadding by animateDpAsState(
        targetValue = if (!searchExpanded && trailingEndActions != null) 56.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "search_end_padding"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        AnimatedVisibility(
            visible = !searchExpanded,
            enter = slideInHorizontally(initialOffsetX = { -it / 10 }) + fadeIn(tween(220)),
            exit = slideOutHorizontally(targetOffsetX = { -it / 10 }) + fadeOut(tween(150)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (showBack) {
                        val backInteraction = remember { MutableInteractionSource() }
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .pressBounceScale(backInteraction, pressedScale = 0.90f)
                                .shadow(
                                    elevation = 10.dp,
                                    shape = pillShape,
                                    ambientColor = GlassShadowNavy,
                                    spotColor = GlassShadowNavy
                                )
                                .clip(pillShape)
                                .clickable(
                                    interactionSource = backInteraction,
                                    indication = androidx.compose.material3.ripple(),
                                    role = Role.Button,
                                    onClick = onBackClick
                                )
                                .testTag("top_bar_back_button"),
                            shape = pillShape,
                            color = pillColor,
                            border = BorderStroke(1.dp, GlassBorderWhite)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = IconifyIcons.ArrowLeft,
                                    contentDescription = stringResource(R.string.cd_back),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    val titleInteraction = remember { MutableInteractionSource() }
                    val titleFocusRequester = remember { FocusRequester() }
                    val keyboardController = LocalSoftwareKeyboardController.current
                    var titleFieldValue by remember(isTitleEditing) {
                        mutableStateOf(
                            TextFieldValue(
                                text = titleDraft,
                                selection = TextRange(titleDraft.length)
                            )
                        )
                    }
                    LaunchedEffect(isTitleEditing) {
                        if (isTitleEditing) {
                            titleFocusRequester.requestFocus()
                        }
                    }
                    LaunchedEffect(titleDraft) {
                        if (titleFieldValue.text != titleDraft) {
                            titleFieldValue = TextFieldValue(
                                text = titleDraft,
                                selection = TextRange(titleDraft.length)
                            )
                        }
                    }

                    val baseTitleModifier = Modifier
                        .height(48.dp)
                        .then(
                            if (isTitleEditing) Modifier.weight(1f) else Modifier
                        )
                        .then(
                            if (!isTitleEditing && onTitleClick != null) {
                                Modifier.pressBounceScale(titleInteraction, pressedScale = 0.96f)
                            } else {
                                Modifier
                            }
                        )
                        .shadow(
                            elevation = 10.dp,
                            shape = pillShape,
                            ambientColor = GlassShadowNavy,
                            spotColor = GlassShadowNavy
                        )
                    Surface(
                        modifier = if (!isTitleEditing && onTitleClick != null) {
                            baseTitleModifier
                                .clip(pillShape)
                                .clickable(
                                    interactionSource = titleInteraction,
                                    indication = androidx.compose.material3.ripple(),
                                    role = Role.Button,
                                    onClick = onTitleClick
                                )
                                .testTag("top_bar_title_button")
                        } else {
                            baseTitleModifier
                        },
                        shape = pillShape,
                        color = if (isTitleEditing) {
                            MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f)
                        } else {
                            pillColor
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isTitleEditing) PrimaryContainer.copy(alpha = 0.55f) else GlassBorderWhite
                        )
                    ) {
                        if (isTitleEditing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = titleFieldValue,
                                    onValueChange = { next ->
                                        titleFieldValue = next
                                        onTitleDraftChange(next.text)
                                    },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.titleMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    cursorBrush = SolidColor(PrimaryContainer),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            keyboardController?.hide()
                                            onCommitTitleEdit()
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(titleFocusRequester)
                                        .testTag("top_bar_title_input")
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.padding(horizontal = 18.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (trailingActions != null) {
                        trailingActions()
                    }
                    // Reserve exact 48.dp slot for the overlaid ExpandableSearchBarPill
                    Spacer(Modifier.size(48.dp))
                    if (trailingEndActions != null) {
                        trailingEndActions()
                    }
                }
            }
        }

        ExpandableSearchBarPill(
            expanded = searchExpanded,
            query = searchQuery,
            placeholder = searchPlaceholder,
            onExpand = onExpandSearch,
            onQueryChange = onSearchQueryChange,
            onClose = onCloseSearch,
            buttonTestTag = "top_bar_search_button",
            inputTestTag = "top_bar_search_input",
            closeTestTag = "top_bar_search_close",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = searchEndPadding)
        )
    }
}

@Composable
fun ExpandableSearchBarPill(
    expanded: Boolean,
    query: String,
    placeholder: String,
    onExpand: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    buttonTestTag: String = "search_button",
    inputTestTag: String = "search_input",
    closeTestTag: String = "search_close",
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(28.dp)
    val pillColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surfaceContainerLowest.copy(
            alpha = if (expanded) 0.90f else 0.72f
        ),
        animationSpec = tween(220),
        label = "search_pill_color"
    )
    val focusRequester = remember { FocusRequester() }
    val collapsedInteraction = remember { MutableInteractionSource() }

    LaunchedEffect(expanded) {
        if (expanded) {
            focusRequester.requestFocus()
        }
    }

    val containerModifier = modifier
        .height(48.dp)
        .shadow(
            elevation = 10.dp,
            shape = pillShape,
            ambientColor = GlassShadowNavy,
            spotColor = GlassShadowNavy
        )
        .animateContentSize(
            animationSpec = spring(
                dampingRatio = 0.78f,
                stiffness = Spring.StiffnessMediumLow
            )
        )

    if (!expanded) {
        Surface(
            modifier = containerModifier
                .width(48.dp)
                .pressBounceScale(collapsedInteraction, pressedScale = 0.90f)
                .clip(pillShape)
                .clickable(
                    interactionSource = collapsedInteraction,
                    indication = androidx.compose.material3.ripple(),
                    role = Role.Button,
                    onClick = onExpand
                )
                .testTag(buttonTestTag),
            shape = pillShape,
            color = pillColor,
            border = BorderStroke(1.dp, GlassBorderWhite)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = IconifyIcons.Search,
                    contentDescription = stringResource(R.string.cd_search),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    } else {
        Surface(
            modifier = containerModifier.fillMaxWidth(),
            shape = pillShape,
            color = pillColor,
            border = BorderStroke(1.dp, GlassBorderWhite)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = IconifyIcons.Search,
                    contentDescription = null,
                    tint = PrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(PrimaryContainer),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .testTag(inputTestTag)
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(closeTestTag)
                ) {
                    Icon(
                        imageVector = IconifyIcons.Close,
                        contentDescription = "Close search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

data class NavTabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun GlassFloatingNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        NavTabItem(AppRoutes.DESK, stringResource(R.string.nav_desk), IconifyIcons.HomeBold, IconifyIcons.HomeLinear),
        NavTabItem(AppRoutes.FILES, stringResource(R.string.nav_files), IconifyIcons.FolderBold, IconifyIcons.FolderLinear),
        NavTabItem(AppRoutes.TOOLS, stringResource(R.string.nav_tools), IconifyIcons.ToolsBold, IconifyIcons.ToolsLinear),
        NavTabItem(AppRoutes.SETTINGS, stringResource(R.string.nav_settings), IconifyIcons.SettingsBold, IconifyIcons.SettingsLinear)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = GlassShadowNavy,
                    spotColor = GlassShadowNavy
                ),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.72f),
            border = BorderStroke(1.dp, GlassBorderWhite)
        ) {
            Row(
                modifier = Modifier
                    .height(68.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    val tint by animateColorAsState(
                        targetValue = if (selected) PrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(220),
                        label = "nav_tab_tint"
                    )
                    val indicatorBg by animateColorAsState(
                        targetValue = if (selected) PrimaryFixed.copy(alpha = 0.72f) else Color.Transparent,
                        animationSpec = tween(240),
                        label = "nav_tab_bg"
                    )
                    val iconScale by animateFloatAsState(
                        targetValue = if (selected) 1.12f else 1f,
                        animationSpec = AppMotion.BouncySpring,
                        label = "nav_icon_scale"
                    )
                    val tabInteraction = remember { MutableInteractionSource() }
                    Column(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .pressBounceScale(tabInteraction, pressedScale = 0.92f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(indicatorBg)
                            .clickable(
                                interactionSource = tabInteraction,
                                indication = androidx.compose.material3.ripple(),
                                role = Role.Tab
                            ) { onNavigate(tab.route) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("nav_tab_${tab.route}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label,
                            tint = tint,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                }
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = tint,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileTypeBadge(
    category: FileCategory,
    isLocked: Boolean = false,
    size: Dp = 44.dp,
    cornerRadius: Dp = 12.dp
) {
    val icon = when {
        isLocked -> IconifyIcons.LockFile
        category == FileCategory.PDF -> IconifyIcons.FilePdf
        category == FileCategory.EXCEL -> IconifyIcons.FileExcel
        category == FileCategory.WORD -> IconifyIcons.FileWord
        else -> IconifyIcons.FilePpt
    }

    Icon(
        imageVector = icon,
        contentDescription = if (isLocked) "Locked ${category.label}" else category.label,
        tint = Color.Unspecified,
        modifier = Modifier.size(size)
    )
}
