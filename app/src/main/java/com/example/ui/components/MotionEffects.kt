package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Material 3 Expressive motion tokens tuned for responsive, tactile spring physics.
 */
object AppMotion {
    val SpatialSpring = spring<Float>(
        dampingRatio = 0.76f,
        stiffness = Spring.StiffnessMediumLow
    )

    val SpatialIntOffsetSpring = spring<IntOffset>(
        dampingRatio = 0.80f,
        stiffness = Spring.StiffnessMediumLow
    )

    val SnappySpring = spring<Float>(
        dampingRatio = 0.62f,
        stiffness = Spring.StiffnessMedium
    )

    val BouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val EffectsSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val SoftExpandEnter: EnterTransition =
        expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing))

    val SoftCollapseExit: ExitTransition =
        shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = spring(
                dampingRatio = 0.90f,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeOut(animationSpec = tween(160, easing = FastOutSlowInEasing))

    val PopCardEnter: EnterTransition =
        expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = spring(
                dampingRatio = 0.80f,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + scaleIn(
            initialScale = 0.96f,
            animationSpec = spring(
                dampingRatio = 0.78f,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(animationSpec = tween(200))

    val PopCardExit: ExitTransition =
        shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = tween(180, easing = FastOutSlowInEasing)
        ) + scaleOut(
            targetScale = 0.96f,
            animationSpec = tween(180)
        ) + fadeOut(animationSpec = tween(150))
}

/**
 * Adds tactile spring press feedback in the graphicsLayer phase without triggering relayout.
 */
fun Modifier.pressBounceScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.96f,
    enabled: Boolean = true
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed) pressedScale else 1f,
        animationSpec = AppMotion.SnappySpring,
        label = "press_bounce_scale"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * GPU-accelerated staggered spring entrance animation (slide up + fade + gentle scale).
 */
fun Modifier.staggeredEntrance(
    index: Int,
    stepDelayMs: Long = 45L,
    initialOffsetY: Dp = 18.dp
): Modifier = composed {
    val alphaAnim = remember { Animatable(0f) }
    val offsetAnim = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        val delayMs = (index.coerceAtMost(8)) * stepDelayMs
        if (delayMs > 0L) delay(delayMs)
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
            )
        }
        launch {
            offsetAnim.animateTo(
                targetValue = 0f,
                animationSpec = AppMotion.SpatialSpring
            )
        }
    }

    this.graphicsLayer {
        alpha = alphaAnim.value
        translationY = offsetAnim.value * initialOffsetY.toPx()
        val s = 0.97f + (1f - offsetAnim.value) * 0.03f
        scaleX = s
        scaleY = s
    }
}

@Composable
fun SoftAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = AppMotion.SoftExpandEnter,
    exit: ExitTransition = AppMotion.SoftCollapseExit,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit
    ) {
        content()
    }
}
