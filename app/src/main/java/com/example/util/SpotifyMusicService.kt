package com.example.util

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log

data class SpotifyTrack(
    val id: String,
    val title: String,
    val artist: String,
    val albumArt: String,
    val spotifyUri: String,
    val spotifyUrl: String,
    val previewAudioUrl: String = "",
    val durationText: String = "3:20"
)

object SpotifyMusicService {

    val POPULAR_TRACKS: List<SpotifyTrack> = listOf(
        SpotifyTrack(
            id = "sp_1",
            title = "Espresso",
            artist = "Sabrina Carpenter",
            albumArt = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
            spotifyUri = "spotify:track:2qxmye6gAegTMjLKEBoR7d",
            spotifyUrl = "https://open.spotify.com/track/2qxmye6gAegTMjLKEBoR7d",
            durationText = "2:55"
        ),
        SpotifyTrack(
            id = "sp_2",
            title = "BIRDS OF A FEATHER",
            artist = "Billie Eilish",
            albumArt = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
            spotifyUri = "spotify:track:6dOtVTDmmpTTemRI9xYSUI",
            spotifyUrl = "https://open.spotify.com/track/6dOtVTDmmpTTemRI9xYSUI",
            durationText = "3:30"
        ),
        SpotifyTrack(
            id = "sp_3",
            title = "Not Like Us",
            artist = "Kendrick Lamar",
            albumArt = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&auto=format&fit=crop&q=80",
            spotifyUri = "spotify:track:7MmGBlY29n9O3o1Z6aXk1C",
            spotifyUrl = "https://open.spotify.com/track/7MmGBlY29n9O3o1Z6aXk1C",
            durationText = "4:34"
        ),
        SpotifyTrack(
            id = "sp_4",
            title = "Starboy",
            artist = "The Weeknd ft. Daft Punk",
            albumArt = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            spotifyUri = "spotify:track:7MXVkk9YM5IZxh0WSlVIyY",
            spotifyUrl = "https://open.spotify.com/track/7MXVkk9YM5IZxh0WSlVIyY",
            durationText = "3:50"
        ),
        SpotifyTrack(
            id = "sp_5",
            title = "FE!N",
            artist = "Travis Scott ft. Playboi Carti",
            albumArt = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=600&auto=format&fit=crop&q=80",
            spotifyUri = "spotify:track:42VsgItocQwOQC3XWZ8J43",
            spotifyUrl = "https://open.spotify.com/track/42VsgItocQwOQC3XWZ8J43",
            durationText = "3:11"
        ),
        SpotifyTrack(
            id = "sp_6",
            title = "Cruel Summer",
            artist = "Taylor Swift",
            albumArt = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600&auto=format&fit=crop&q=80",
            spotifyUri = "spotify:track:1BxfuPKGuaTgP7aM0XbdMe",
            spotifyUrl = "https://open.spotify.com/track/1BxfuPKGuaTgP7aM0XbdMe",
            durationText = "2:58"
        ),
        SpotifyTrack(
            id = "sp_7",
            title = "Blinding Lights",
            artist = "The Weeknd",
            albumArt = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
            spotifyUri = "spotify:track:0VjIjW4GlUZAMYd2vXMi3b",
            spotifyUrl = "https://open.spotify.com/track/0VjIjW4GlUZAMYd2vXMi3b",
            durationText = "3:20"
        ),
        SpotifyTrack(
            id = "sp_8",
            title = "Snooze",
            artist = "SZA",
            albumArt = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&auto=format&fit=crop&q=80",
            spotifyUri = "spotify:track:4iZ4mst790ZCrSV4YJAEBp",
            spotifyUrl = "https://open.spotify.com/track/4iZ4mst790ZCrSV4YJAEBp",
            durationText = "3:21"
        ),
        SpotifyTrack(
            id = "sp_9",
            title = "As It Was",
            artist = "Harry Styles",
            albumArt = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=600&auto=format&fit=crop&q=80",
            spotifyUri = "spotify:track:4Dvkj6JhhA12EX05Qv97zQ",
            spotifyUrl = "https://open.spotify.com/track/4Dvkj6JhhA12EX05Qv97zQ",
            durationText = "2:47"
        ),
        SpotifyTrack(
            id = "sp_10",
            title = "Glimpse of Us",
            artist = "Joji",
            albumArt = "https://images.unsplash.com/photo-1445985543469-221e57446370?w=600&auto=format&fit=crop&q=80",
            spotifyUri = "spotify:track:6xGruQdnhqCuG2K3KzL2bX",
            spotifyUrl = "https://open.spotify.com/track/6xGruQdnhqCuG2K3KzL2bX",
            durationText = "3:53"
        )
    )

    fun searchTracks(query: String): List<SpotifyTrack> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return POPULAR_TRACKS
        val matches = POPULAR_TRACKS.filter {
            it.title.lowercase().contains(q) || it.artist.lowercase().contains(q)
        }
        if (matches.isNotEmpty()) return matches
        // Dynamic search item when user searches something not in the top 10
        return listOf(
            SpotifyTrack(
                id = "sp_custom_${q.hashCode()}",
                title = query.replaceFirstChar { it.uppercase() },
                artist = "Spotify Artist",
                albumArt = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                spotifyUri = "spotify:search:$query",
                spotifyUrl = "https://open.spotify.com/search/$query",
                durationText = "3:15"
            )
        ) + POPULAR_TRACKS.take(3)
    }

    fun openInSpotify(context: Context, spotifyUri: String, fallbackUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUri))
            intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://${context.packageName}"))
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl.ifBlank { "https://open.spotify.com" }))
                context.startActivity(browserIntent)
            } catch (e: Exception) {
                Log.e("SpotifyService", "Failed to launch Spotify intent", e)
            }
        }
    }
}
