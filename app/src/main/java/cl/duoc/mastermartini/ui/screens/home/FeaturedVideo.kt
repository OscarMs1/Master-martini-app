package cl.duoc.mastermartini.ui.screens.home

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import cl.duoc.mastermartini.R
import cl.duoc.mastermartini.ui.components.MMCard

/** Video técnico local con controles contenidos dentro de la tarjeta. */
@Composable
internal fun FeaturedVideoCard() {
    var videoOpen by remember { mutableStateOf(false) }
    var videoError by remember { mutableStateOf(false) }

    MMCard {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(205.dp)
                .clip(MaterialTheme.shapes.medium),
        ) {
            if (videoOpen) {
                EmbeddedChocolateVideo(
                    onClose = { videoOpen = false },
                    onError = {
                        videoError = true
                        videoOpen = false
                    },
                    onEnded = { videoOpen = false },
                )
            } else {
                Box(
                    Modifier.fillMaxSize().clickable {
                        videoError = false
                        videoOpen = true
                    },
                ) {
                    Image(
                        painter = painterResource(R.drawable.video_templado_portada),
                        contentDescription = "Templado de chocolate",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.05f),
                                    Color.Black.copy(alpha = 0.74f),
                                ),
                            ),
                        ),
                    )
                    Surface(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White,
                        shape = CircleShape,
                        shadowElevation = 4.dp,
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Reproducir video",
                            modifier = Modifier.padding(14.dp).size(34.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Column(Modifier.align(Alignment.BottomStart).padding(15.dp)) {
                        Text(
                            "VIDEO TÉCNICO",
                            color = Color(0xFFFFD478),
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            "Templado de chocolate",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        if (videoError) {
                            Text(
                                "No se pudo reproducir. Toca para reintentar.",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmbeddedChocolateVideo(
    onClose: () -> Unit,
    onError: () -> Unit,
    onEnded: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(
                MediaItem.fromUri(
                    Uri.parse("android.resource://${context.packageName}/${R.raw.templado_chocolate}"),
                ),
            )
        }
    }

    DisposableEffect(exoPlayer, lifecycleOwner) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                onError()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) onEnded()
            }
        }
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                exoPlayer.pause()
            }
        }
        exoPlayer.addListener(listener)
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    useController = true
                    controllerAutoShow = true
                    controllerShowTimeoutMs = 3500
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    player = exoPlayer
                }
            },
            update = { view ->
                if (view.player !== exoPlayer) view.player = exoPlayer
            },
            modifier = Modifier.fillMaxSize(),
        )
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                .background(Color.Black.copy(alpha = 0.65f), CircleShape),
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Cerrar video", tint = Color.White)
        }
    }
}
