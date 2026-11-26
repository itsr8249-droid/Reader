package com.example.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.ChecklistType
import com.example.data.local.LocalFileEntity
import com.example.ui.components.AppMotion
import com.example.ui.components.AppRoutes
import com.example.ui.components.GlassFloatingNavBar
import com.example.ui.components.GlassTopBar
import com.example.ui.components.IconifyIcons
import com.example.ui.components.InlineSearchResultsCard
import com.example.ui.components.PastelGradientBackground
import com.example.ui.components.pressBounceScale
import com.example.ui.screens.ChecklistScreen
import com.example.ui.screens.DeskScreen
import com.example.ui.screens.FilesScreen
import com.example.ui.screens.ImageEditorScreen
import com.example.ui.screens.MindMapScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.PdfViewerScreen
import com.example.ui.screens.PhotoNotesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.VoiceMemosScreen
import com.example.ui.theme.GlassBorderWhite
import com.example.ui.theme.GlassShadowNavy
import com.example.ui.theme.OnPrimary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.viewmodel.FileSortMode
import com.example.ui.viewmodel.WorkspaceViewModel

@Composable
fun ReaderApp(
    incomingDocumentUri: Uri? = null,
    onConsumeIncomingUri: () -> Unit = {},
    viewModel: WorkspaceViewModel = viewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val allFiles by viewModel.allFiles.collectAsStateWithLifecycle()
    val filteredFiles by viewModel.filteredFiles.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()
    val isGridView by viewModel.isGridView.collectAsStateWithLifecycle()

    val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
    val activeNote by viewModel.activeNote.collectAsStateWithLifecycle()

    val shoppingItems by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val todoItems by viewModel.todoItems.collectAsStateWithLifecycle()

    val voiceMemos by viewModel.voiceMemos.collectAsStateWithLifecycle()
    val photoNotes by viewModel.photoNotes.collectAsStateWithLifecycle()
    val mindMaps by viewModel.mindMaps.collectAsStateWithLifecycle()
    val isRecording by viewModel.voiceManager.isRecording.collectAsStateWithLifecycle()
    val elapsedSeconds by viewModel.voiceManager.elapsedSeconds.collectAsStateWithLifecycle()
    val waveformBars by viewModel.voiceManager.waveformBars.collectAsStateWithLifecycle()
    val playingMemoId by viewModel.voiceManager.playingMemoId.collectAsStateWithLifecycle()

    val preferences by viewModel.appPreferences.collectAsStateWithLifecycle()
    val isVoiceSpeaking by viewModel.readAloudManager.isSpeaking.collectAsStateWithLifecycle()

    val toolBusy by viewModel.toolBusy.collectAsStateWithLifecycle()
    val toolFeedback by viewModel.toolFeedback.collectAsStateWithLifecycle()

    var pendingUnlockFile by remember { mutableStateOf<LocalFileEntity?>(null) }
    var isEditingTopBarTitle by remember { mutableStateOf(false) }
    var topBarTitleDraft by remember { mutableStateOf("") }
    var deleteFileConfirmVisible by remember { mutableStateOf(false) }
    var viewerMenuExpanded by remember { mutableStateOf(false) }
    var filesMenuExpanded by remember { mutableStateOf(false) }
    var viewerEditMode by remember { mutableStateOf(false) }

    fun openDocument(file: LocalFileEntity) {
        if (file.isLocked && !viewModel.isFileUnlocked(file)) {
            pendingUnlockFile = file
        } else {
            navController.navigate(AppRoutes.pdfViewerRoute(file.id)) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(incomingDocumentUri) {
        if (incomingDocumentUri != null) {
            viewModel.importExternalFile(incomingDocumentUri) { imported ->
                openDocument(imported)
            }
            onConsumeIncomingUri()
        }
    }

    val isTabRoute = currentRoute in setOf(
        AppRoutes.DESK,
        AppRoutes.FILES,
        AppRoutes.TOOLS,
        AppRoutes.SETTINGS
    )

    val activeViewerFileId = backStackEntry?.arguments?.getLong("fileId")
    val activeViewerFile = remember(activeViewerFileId, allFiles) {
        allFiles.firstOrNull { it.id == activeViewerFileId }
    }
    val activeViewerFileName = activeViewerFile?.name
    var searchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    fun startTopBarTitleEdit() {
        viewerMenuExpanded = false
        searchExpanded = false
        when {
            currentRoute == AppRoutes.PDF_VIEWER && activeViewerFile != null -> {
                topBarTitleDraft = activeViewerFile.name
                isEditingTopBarTitle = true
            }
            currentRoute == AppRoutes.NOTE_EDITOR && activeNote != null -> {
                topBarTitleDraft = activeNote?.title.orEmpty()
                isEditingTopBarTitle = true
            }
        }
    }

    fun commitTopBarTitleEdit() {
        when (currentRoute) {
            AppRoutes.PDF_VIEWER -> {
                val target = activeViewerFile
                val trimmed = topBarTitleDraft.trim()
                if (target != null && trimmed.isNotEmpty() && trimmed != target.name) {
                    viewModel.renameFile(target, trimmed)
                }
            }
            AppRoutes.NOTE_EDITOR -> {
                val note = activeNote
                val nextTitle = topBarTitleDraft.trim().ifBlank { "Untitled Note" }
                if (note != null && nextTitle != note.title) {
                    viewModel.updateActiveNote(nextTitle, note.content)
                }
            }
        }
        isEditingTopBarTitle = false
    }

    LaunchedEffect(currentRoute, activeViewerFileId) {
        searchExpanded = false
        searchQuery = ""
        viewerMenuExpanded = false
        filesMenuExpanded = false
        viewerEditMode = false
        isEditingTopBarTitle = false
        topBarTitleDraft = ""
    }

    BackHandler(enabled = isEditingTopBarTitle) {
        isEditingTopBarTitle = false
    }

    BackHandler(enabled = searchExpanded && currentRoute != AppRoutes.DESK) {
        searchExpanded = false
        searchQuery = ""
    }

    BackHandler(enabled = viewerMenuExpanded) {
        viewerMenuExpanded = false
    }

    BackHandler(enabled = filesMenuExpanded) {
        filesMenuExpanded = false
    }

    val topBarTitle = when (currentRoute) {
        AppRoutes.DESK -> "Desk"
        AppRoutes.FILES -> "Files"
        AppRoutes.TOOLS -> "Tools"
        AppRoutes.SETTINGS -> "Settings"
        AppRoutes.NOTE_EDITOR -> activeNote?.title?.ifBlank { "Untitled Note" } ?: "Note"
        AppRoutes.VOICE_MEMOS -> "Voice Memos"
        AppRoutes.SHOPPING_LIST -> "Shopping List"
        AppRoutes.TODO_LIST -> "To-Do List"
        AppRoutes.IMAGE_EDITOR -> "Image Editor"
        AppRoutes.PHOTO_NOTES -> "Photo Notes"
        AppRoutes.MIND_MAP -> "Mind Map"
        AppRoutes.PDF_VIEWER -> activeViewerFileName ?: "Document"
        else -> "Reader"
    }

    PastelGradientBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    if (currentRoute != null && currentRoute != AppRoutes.DESK) {
                        GlassTopBar(
                            title = topBarTitle,
                            showBack = !isTabRoute,
                            onBackClick = {
                                if (isEditingTopBarTitle) commitTopBarTitleEdit()
                                navController.popBackStack()
                            },
                            onTitleClick = when {
                                (currentRoute == AppRoutes.NOTE_EDITOR && activeNote != null) ||
                                    (currentRoute == AppRoutes.PDF_VIEWER && activeViewerFile != null) -> {
                                    { startTopBarTitleEdit() }
                                }
                                else -> null
                            },
                            isTitleEditing = isEditingTopBarTitle && (
                                (currentRoute == AppRoutes.PDF_VIEWER && activeViewerFile != null) ||
                                    (currentRoute == AppRoutes.NOTE_EDITOR && activeNote != null)
                                ),
                            titleDraft = topBarTitleDraft,
                            onTitleDraftChange = { topBarTitleDraft = it },
                            onCommitTitleEdit = ::commitTopBarTitleEdit,
                            searchExpanded = searchExpanded,
                            searchQuery = searchQuery,
                            searchPlaceholder = if (currentRoute == AppRoutes.PDF_VIEWER && activeViewerFileName != null) {
                                "Find in $activeViewerFileName…"
                            } else {
                                "Search files, notes, or memos…"
                            },
                            onExpandSearch = {
                                if (isEditingTopBarTitle) commitTopBarTitleEdit()
                                viewerMenuExpanded = false
                                filesMenuExpanded = false
                                searchExpanded = true
                            },
                            onSearchQueryChange = { searchQuery = it },
                            onCloseSearch = {
                                searchExpanded = false
                                searchQuery = ""
                            },
                            trailingEndActions = when {
                                currentRoute == AppRoutes.FILES -> {
                                    {
                                        val tuneInteraction = remember { MutableInteractionSource() }
                                        val pillShape = RoundedCornerShape(28.dp)
                                        val tuneRotation by animateFloatAsState(
                                            targetValue = if (filesMenuExpanded) 90f else 0f,
                                            animationSpec = AppMotion.SpatialSpring,
                                            label = "files_tune_rotation"
                                        )
                                        Surface(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .pressBounceScale(tuneInteraction, pressedScale = 0.90f)
                                                .shadow(
                                                    elevation = 10.dp,
                                                    shape = pillShape,
                                                    ambientColor = GlassShadowNavy,
                                                    spotColor = GlassShadowNavy
                                                )
                                                .clip(pillShape)
                                                .clickable(
                                                    interactionSource = tuneInteraction,
                                                    indication = androidx.compose.material3.ripple(),
                                                    role = Role.Button,
                                                    onClick = {
                                                        searchExpanded = false
                                                        filesMenuExpanded = !filesMenuExpanded
                                                    }
                                                )
                                                .testTag("files_sort_view_button"),
                                            shape = pillShape,
                                            color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.72f),
                                            border = BorderStroke(1.dp, GlassBorderWhite)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = IconifyIcons.Tune,
                                                    contentDescription = "Sort and view options",
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .rotate(tuneRotation)
                                                )
                                            }
                                        }
                                    }
                                }
                                currentRoute == AppRoutes.PDF_VIEWER && activeViewerFile != null -> {
                                    {
                                        val moreInteraction = remember { MutableInteractionSource() }
                                        val pillShape = RoundedCornerShape(28.dp)
                                        val moreRotation by animateFloatAsState(
                                            targetValue = if (viewerMenuExpanded) 90f else 0f,
                                            animationSpec = AppMotion.SpatialSpring,
                                            label = "viewer_more_rotation"
                                        )
                                        Surface(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .pressBounceScale(moreInteraction, pressedScale = 0.90f)
                                                .shadow(
                                                    elevation = 10.dp,
                                                    shape = pillShape,
                                                    ambientColor = GlassShadowNavy,
                                                    spotColor = GlassShadowNavy
                                                )
                                                .clip(pillShape)
                                                .clickable(
                                                    interactionSource = moreInteraction,
                                                    indication = androidx.compose.material3.ripple(),
                                                    role = Role.Button,
                                                    onClick = {
                                                        if (isEditingTopBarTitle) commitTopBarTitleEdit()
                                                        searchExpanded = false
                                                        viewerMenuExpanded = !viewerMenuExpanded
                                                    }
                                                )
                                                .testTag("viewer_more_button"),
                                            shape = pillShape,
                                            color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.72f),
                                            border = BorderStroke(1.dp, GlassBorderWhite)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = IconifyIcons.MoreVert,
                                                    contentDescription = "Document options",
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .rotate(moreRotation)
                                                )
                                            }
                                        }
                                    }
                                }
                                else -> null
                            }
                        )
                    }
                }
            ) { innerPadding ->
                val tabOrder = listOf(AppRoutes.DESK, AppRoutes.FILES, AppRoutes.TOOLS, AppRoutes.SETTINGS)
                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = AppRoutes.DESK,
                        modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                        enterTransition = {
                            val fromIdx = tabOrder.indexOf(initialState.destination.route)
                            val toIdx = tabOrder.indexOf(targetState.destination.route)
                            if (fromIdx != -1 && toIdx != -1) {
                                val dir = if (toIdx >= fromIdx) 1 else -1
                                slideInHorizontally(
                                    initialOffsetX = { dir * (it / 6) },
                                    animationSpec = spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow)
                                ) + fadeIn(tween(240, easing = FastOutSlowInEasing))
                            } else {
                                slideInHorizontally(
                                    initialOffsetX = { it / 4 },
                                    animationSpec = spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow)
                                ) + scaleIn(initialScale = 0.96f, animationSpec = tween(260)) + fadeIn(tween(220))
                            }
                        },
                        exitTransition = {
                            val fromIdx = tabOrder.indexOf(initialState.destination.route)
                            val toIdx = tabOrder.indexOf(targetState.destination.route)
                            if (fromIdx != -1 && toIdx != -1) {
                                val dir = if (toIdx >= fromIdx) -1 else 1
                                slideOutHorizontally(
                                    targetOffsetX = { dir * (it / 8) },
                                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                                ) + fadeOut(tween(180))
                            } else {
                                slideOutHorizontally(
                                    targetOffsetX = { -it / 8 },
                                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                                ) + scaleOut(targetScale = 0.97f, animationSpec = tween(220)) + fadeOut(tween(180))
                            }
                        },
                        popEnterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { -it / 6 },
                                animationSpec = spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow)
                            ) + scaleIn(initialScale = 0.97f, animationSpec = tween(240)) + fadeIn(tween(220))
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it / 4 },
                                animationSpec = tween(220, easing = FastOutSlowInEasing)
                            ) + scaleOut(targetScale = 0.96f, animationSpec = tween(220)) + fadeOut(tween(180))
                        }
                    ) {
                        composable(AppRoutes.DESK) {
                            DeskScreen(
                                files = allFiles,
                                notes = allNotes,
                                memos = voiceMemos,
                                onSearchFileClick = { file -> openDocument(file) },
                                onSelectNote = { note ->
                                    viewModel.openNoteById(note.id)
                                    navController.navigate(AppRoutes.NOTE_EDITOR) {
                                        launchSingleTop = true
                                    }
                                },
                                onSeeAllFiles = {
                                    navController.navigate(AppRoutes.FILES) {
                                        popUpTo(AppRoutes.DESK) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onOpenDocument = { file -> openDocument(file) },
                                onOpenQuickNote = {
                                    viewModel.openLatestNote()
                                    navController.navigate(AppRoutes.NOTE_EDITOR)
                                },
                                onOpenTodoList = { navController.navigate(AppRoutes.TODO_LIST) },
                                onOpenShoppingList = { navController.navigate(AppRoutes.SHOPPING_LIST) },
                                onOpenVoiceMemos = { navController.navigate(AppRoutes.VOICE_MEMOS) },
                                onOpenPhotoNotes = { navController.navigate(AppRoutes.PHOTO_NOTES) },
                                onOpenMindMap = { navController.navigate(AppRoutes.MIND_MAP) },
                                onImportUri = { uri ->
                                    viewModel.importExternalFile(uri) { imported ->
                                        openDocument(imported)
                                    }
                                }
                            )
                        }

                        composable(AppRoutes.FILES) {
                            FilesScreen(
                                files = filteredFiles,
                                isGridView = isGridView,
                                onFileClick = { file -> openDocument(file) }
                            )
                        }

                    composable(AppRoutes.TOOLS) {
                        ToolsScreen(
                            files = allFiles,
                            toolBusy = toolBusy,
                            toolFeedback = toolFeedback,
                            onCreateScanTempUri = viewModel::createScanTempUri,
                            onMergePdfs = viewModel::mergePdfs,
                            onSplitPdf = viewModel::splitPdf,
                            onCompressPdf = viewModel::compressPdf,
                            onRotatePdf = viewModel::rotatePdf,
                            onExtractPages = viewModel::extractPdfPages,
                            onWatermarkPdf = viewModel::watermarkPdf,
                            onProtectPdf = viewModel::protectPdf,
                            onImagesToPdf = viewModel::imagesToPdf,
                            onOpenImageEditor = { navController.navigate(AppRoutes.IMAGE_EDITOR) },
                            onOpenGeneratedFile = { id ->
                                navController.navigate(AppRoutes.pdfViewerRoute(id))
                            },
                            onDismissFeedback = viewModel::clearToolFeedback
                        )
                    }

                    composable(AppRoutes.SETTINGS) {
                        SettingsScreen(
                            preferences = preferences,
                            isVoiceSpeaking = isVoiceSpeaking,
                            onSelectVoiceProfile = viewModel::setVoiceProfile,
                            onToggleVoicePreview = viewModel::toggleVoicePreview,
                            onSelectLanguage = viewModel::setLanguage
                        )
                    }

                    composable(AppRoutes.NOTE_EDITOR) {
                        LaunchedEffect(Unit) {
                            if (activeNote == null) {
                                viewModel.openLatestNote()
                            }
                        }
                        NoteEditorScreen(
                            note = activeNote,
                            onNoteChange = viewModel::updateActiveNote,
                            isVoiceSpeaking = isVoiceSpeaking,
                            onToggleReadAloud = { text ->
                                if (isVoiceSpeaking) viewModel.stopSpeaking()
                                else viewModel.speakDocumentText(text)
                            }
                        )
                    }

                    composable(AppRoutes.VOICE_MEMOS) {
                        VoiceMemosScreen(
                            memos = voiceMemos,
                            isRecording = isRecording,
                            elapsedSeconds = elapsedSeconds,
                            waveformBars = waveformBars,
                            playingMemoId = playingMemoId,
                            onToggleRecord = viewModel::toggleRecording,
                            onTogglePlay = viewModel::toggleMemoPlayback,
                            onDeleteMemo = viewModel::deleteMemo
                        )
                    }

                    composable(AppRoutes.SHOPPING_LIST) {
                        ChecklistScreen(
                            type = ChecklistType.SHOPPING,
                            items = shoppingItems,
                            onToggleItem = viewModel::toggleChecklistItem,
                            onAddItem = { text -> viewModel.addChecklistItem(ChecklistType.SHOPPING, text) },
                            onClearCompleted = { viewModel.clearCompleted(ChecklistType.SHOPPING) }
                        )
                    }

                    composable(AppRoutes.TODO_LIST) {
                        ChecklistScreen(
                            type = ChecklistType.TODO,
                            items = todoItems,
                            onToggleItem = viewModel::toggleChecklistItem,
                            onAddItem = { text -> viewModel.addChecklistItem(ChecklistType.TODO, text) },
                            onClearCompleted = { viewModel.clearCompleted(ChecklistType.TODO) }
                        )
                    }

                    composable(AppRoutes.IMAGE_EDITOR) {
                        ImageEditorScreen(
                            loadBitmapFromUri = viewModel::loadImageBitmapFromUri,
                            onExportPdf = { bitmap, title ->
                                viewModel.exportEditedImageAsPdf(bitmap, title)
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(AppRoutes.PHOTO_NOTES) {
                        PhotoNotesScreen(
                            photoNotes = photoNotes,
                            loadWorkspaceBitmap = viewModel::loadWorkspaceImageBitmap,
                            onCreateCameraTempUri = viewModel::createScanTempUri,
                            onAddPhotoNote = viewModel::addPhotoNote,
                            onDeletePhotoNote = viewModel::deletePhotoNote
                        )
                    }

                    composable(AppRoutes.MIND_MAP) {
                        MindMapScreen(
                            mindMaps = mindMaps,
                            onSaveMindMap = viewModel::saveMindMap,
                            onCreateMindMap = viewModel::createMindMap,
                            onDeleteMindMap = viewModel::deleteMindMap
                        )
                    }

                    composable(
                        route = AppRoutes.PDF_VIEWER,
                        arguments = listOf(navArgument("fileId") { type = NavType.LongType })
                    ) { entry ->
                        val fileId = entry.arguments?.getLong("fileId") ?: 0L
                        val file by viewModel.observeFile(fileId)
                            .collectAsStateWithLifecycle(initialValue = allFiles.firstOrNull { it.id == fileId })
                        val annotations by viewModel.observePdfAnnotations(fileId)
                            .collectAsStateWithLifecycle(initialValue = emptyList())

                        PdfViewerScreen(
                            file = file,
                            annotations = annotations,
                            searchQuery = searchQuery,
                            loadPageBitmap = viewModel::loadPdfPageBitmap,
                            readNonPdfText = viewModel::readDocumentText,
                            onSaveDocumentText = viewModel::saveDocumentText,
                            isVoiceSpeaking = isVoiceSpeaking,
                            onToggleReadAloud = { text ->
                                if (isVoiceSpeaking) viewModel.stopSpeaking()
                                else viewModel.speakDocumentText(text)
                            },
                            onToggleBookmark = viewModel::togglePdfBookmark,
                            onAddAnnotation = viewModel::addPdfAnnotation,
                            onDeleteAnnotation = viewModel::removePdfAnnotation,
                            isEditBarVisible = viewerEditMode,
                            onEditBarVisibleChange = { viewerEditMode = it }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = searchExpanded && currentRoute != AppRoutes.PDF_VIEWER && currentRoute != AppRoutes.DESK,
                    enter = fadeIn(tween(180)),
                    exit = fadeOut(tween(150))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.06f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                searchExpanded = false
                                searchQuery = ""
                            }
                    ) {
                        AnimatedVisibility(
                            visible = true,
                            enter = AppMotion.PopCardEnter,
                            exit = AppMotion.PopCardExit,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = innerPadding.calculateTopPadding() + 4.dp)
                                .padding(horizontal = 20.dp)
                                .widthIn(max = 640.dp)
                        ) {
                            InlineSearchResultsCard(
                                query = searchQuery,
                                files = allFiles,
                                notes = allNotes,
                                memos = voiceMemos,
                                onSelectFile = { file ->
                                    searchExpanded = false
                                    searchQuery = ""
                                    openDocument(file)
                                },
                                onSelectNote = { note ->
                                    searchExpanded = false
                                    searchQuery = ""
                                    viewModel.openNoteById(note.id)
                                    if (currentRoute != AppRoutes.NOTE_EDITOR) {
                                        navController.navigate(AppRoutes.NOTE_EDITOR) {
                                            launchSingleTop = true
                                        }
                                    }
                                },
                                onSelectMemo = {
                                    searchExpanded = false
                                    searchQuery = ""
                                    if (currentRoute != AppRoutes.VOICE_MEMOS) {
                                        navController.navigate(AppRoutes.VOICE_MEMOS) {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                if (isEditingTopBarTitle && (currentRoute == AppRoutes.PDF_VIEWER || currentRoute == AppRoutes.NOTE_EDITOR)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding())
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { commitTopBarTitleEdit() }
                    )
                }
                }
            }

            // Expanding Files Sort & View Frosted Menu (expands smoothly left & down like file viewer)
            AnimatedVisibility(
                visible = filesMenuExpanded && currentRoute == AppRoutes.FILES,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(140))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { filesMenuExpanded = false }
                ) {
                    val menuShape = RoundedCornerShape(24.dp)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(top = 10.dp, end = 20.dp)
                            .animateEnterExit(
                                enter = expandHorizontally(
                                    expandFrom = Alignment.End,
                                    animationSpec = spring(
                                        dampingRatio = 0.78f,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) + expandVertically(
                                    expandFrom = Alignment.Top,
                                    animationSpec = spring(
                                        dampingRatio = 0.78f,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) + fadeIn(tween(180)),
                                exit = shrinkHorizontally(
                                    shrinkTowards = Alignment.End,
                                    animationSpec = spring(
                                        dampingRatio = 0.88f,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ) + shrinkVertically(
                                    shrinkTowards = Alignment.Top,
                                    animationSpec = tween(160)
                                ) + fadeOut(tween(130))
                            )
                            .shadow(
                                elevation = 14.dp,
                                shape = menuShape,
                                ambientColor = GlassShadowNavy,
                                spotColor = GlassShadowNavy
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {},
                        shape = menuShape,
                        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, GlassBorderWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                                .padding(bottom = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .padding(start = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val viewInteraction = remember { MutableInteractionSource() }
                                val viewShape = RoundedCornerShape(14.dp)
                                Row(
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .pressBounceScale(viewInteraction, pressedScale = 0.94f)
                                        .clip(viewShape)
                                        .background(
                                            if (isGridView) PrimaryContainer.copy(alpha = 0.14f)
                                            else Color.Transparent
                                        )
                                        .clickable(
                                            interactionSource = viewInteraction,
                                            indication = androidx.compose.material3.ripple(),
                                            role = Role.Button,
                                            onClick = {
                                                viewModel.setGridView(!isGridView)
                                                filesMenuExpanded = false
                                            }
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("files_view_toggle_button"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isGridView) IconifyIcons.GridView else IconifyIcons.ListView,
                                        contentDescription = null,
                                        tint = if (isGridView) PrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (isGridView) "Grid view" else "List view",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isGridView) PrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .clickable(role = Role.Button) { filesMenuExpanded = false }
                                        .testTag("files_menu_close_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconifyIcons.Tune,
                                        contentDescription = "Close options",
                                        tint = PrimaryContainer,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .rotate(90f)
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )

                            FileSortMode.entries.forEach { mode ->
                                val selected = sortMode == mode
                                ViewerMenuActionRow(
                                    label = mode.label,
                                    icon = IconifyIcons.Sort,
                                    tint = if (selected) PrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    selected = selected,
                                    testTag = "files_sort_${mode.name.lowercase()}",
                                    onClick = {
                                        viewModel.setSortMode(mode)
                                        filesMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Expanding Three-Dot Frosted Menu (expands smoothly left & down with rounded corners)
            AnimatedVisibility(
                visible = viewerMenuExpanded && currentRoute == AppRoutes.PDF_VIEWER && activeViewerFile != null,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(140))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { viewerMenuExpanded = false }
                ) {
                    val menuShape = RoundedCornerShape(24.dp)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(top = 10.dp, end = 20.dp)
                            .animateEnterExit(
                                enter = expandHorizontally(
                                    expandFrom = Alignment.End,
                                    animationSpec = spring(
                                        dampingRatio = 0.78f,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) + expandVertically(
                                    expandFrom = Alignment.Top,
                                    animationSpec = spring(
                                        dampingRatio = 0.78f,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) + fadeIn(tween(180)),
                                exit = shrinkHorizontally(
                                    shrinkTowards = Alignment.End,
                                    animationSpec = spring(
                                        dampingRatio = 0.88f,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ) + shrinkVertically(
                                    shrinkTowards = Alignment.Top,
                                    animationSpec = tween(160)
                                ) + fadeOut(tween(130))
                            )
                            .shadow(
                                elevation = 14.dp,
                                shape = menuShape,
                                ambientColor = GlassShadowNavy,
                                spotColor = GlassShadowNavy
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {},
                        shape = menuShape,
                        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, GlassBorderWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                                .padding(bottom = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .padding(start = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val editInteraction = remember { MutableInteractionSource() }
                                val editShape = RoundedCornerShape(14.dp)
                                Row(
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .pressBounceScale(editInteraction, pressedScale = 0.94f)
                                        .clip(editShape)
                                        .background(
                                            if (viewerEditMode) PrimaryContainer.copy(alpha = 0.14f)
                                            else Color.Transparent
                                        )
                                        .clickable(
                                            interactionSource = editInteraction,
                                            indication = androidx.compose.material3.ripple(),
                                            role = Role.Button,
                                            onClick = {
                                                viewerEditMode = !viewerEditMode
                                                viewerMenuExpanded = false
                                            }
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("viewer_menu_edit"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = IconifyIcons.DrawPen,
                                        contentDescription = null,
                                        tint = if (viewerEditMode) PrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Edit",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (viewerEditMode) PrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .clickable(role = Role.Button) { viewerMenuExpanded = false }
                                        .testTag("viewer_more_close_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconifyIcons.MoreVert,
                                        contentDescription = "Close options",
                                        tint = PrimaryContainer,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .rotate(90f)
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )

                            ViewerMenuActionRow(
                                label = "Rename",
                                icon = IconifyIcons.Rename,
                                tint = MaterialTheme.colorScheme.onSurface,
                                testTag = "viewer_menu_rename",
                                onClick = { startTopBarTitleEdit() }
                            )
                            ViewerMenuActionRow(
                                label = "Share",
                                icon = IconifyIcons.Share,
                                tint = MaterialTheme.colorScheme.onSurface,
                                testTag = "viewer_menu_share",
                                onClick = {
                                    viewerMenuExpanded = false
                                    activeViewerFile?.let(viewModel::shareFile)
                                }
                            )
                            ViewerMenuActionRow(
                                label = "Delete",
                                icon = IconifyIcons.Delete,
                                tint = MaterialTheme.colorScheme.error,
                                testTag = "viewer_menu_delete",
                                onClick = {
                                    viewerMenuExpanded = false
                                    deleteFileConfirmVisible = true
                                }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isTabRoute,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.80f, stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(tween(240)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(180, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(160)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                GlassFloatingNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { targetRoute ->
                        if (targetRoute != currentRoute) {
                            navController.navigate(targetRoute) {
                                popUpTo(AppRoutes.DESK) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }

            val lockedTarget = pendingUnlockFile
            if (lockedTarget != null) {
                var passwordInput by remember(lockedTarget.id) { mutableStateOf("") }
                var passwordError by remember(lockedTarget.id) { mutableStateOf<String?>(null) }
                AlertDialog(
                    onDismissRequest = { pendingUnlockFile = null },
                    title = { Text("Protected Document") },
                    text = {
                        Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                            Text("Enter your password to unlock ${lockedTarget.name}.")
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = {
                                    passwordInput = it
                                    passwordError = null
                                },
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                isError = passwordError != null,
                                supportingText = passwordError?.let { err -> { Text(err) } },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.unlockFile(lockedTarget, passwordInput) { ok ->
                                    if (ok) {
                                        val targetId = lockedTarget.id
                                        pendingUnlockFile = null
                                        navController.navigate(AppRoutes.pdfViewerRoute(targetId)) {
                                            launchSingleTop = true
                                        }
                                    } else {
                                        passwordError = "Incorrect password"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryContainer,
                                contentColor = OnPrimary
                            )
                        ) {
                            Icon(IconifyIcons.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Unlock")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { pendingUnlockFile = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            val currentViewerFile = activeViewerFile
            if (deleteFileConfirmVisible && currentViewerFile != null) {
                AlertDialog(
                    onDismissRequest = { deleteFileConfirmVisible = false },
                    title = { Text("Delete ${currentViewerFile.name}?") },
                    text = { Text("This file and its annotations will be removed from your workspace.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                deleteFileConfirmVisible = false
                                viewModel.deleteFile(currentViewerFile)
                                navController.popBackStack()
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { deleteFileConfirmVisible = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ViewerMenuActionRow(
    label: String,
    icon: ImageVector,
    tint: Color,
    testTag: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val rowShape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 1.dp)
            .minimumInteractiveComponentSize()
            .pressBounceScale(interactionSource, pressedScale = 0.96f)
            .clip(rowShape)
            .background(
                if (selected) PrimaryContainer.copy(alpha = 0.12f) else Color.Transparent
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = tint,
            maxLines = 1
        )
        if (selected) {
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector = IconifyIcons.Check,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

