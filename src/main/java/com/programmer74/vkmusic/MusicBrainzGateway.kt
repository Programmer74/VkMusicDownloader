package com.programmer74.vkmusic

import org.jsoup.Jsoup

@Suppress("ArrayInDataClass")
data class TrackAlbumInfo(
  var album: String,
  var track: String,
  var cover: ByteArray? = null
)

//I do know that they have API, but it is so complicated that
//searching/parsing web page seems more convenient
class MusicBrainzGateway(
  private val restApi: RestApi
) {

  private fun tryGetAlbumCover(id: String): ByteArray? {
    val url = "$API_PATH$id"
    return try {
      val htmlBody = restApi.sendGet(url)
      val content = Jsoup.parse(htmlBody).body()
      val imageName = content.getElementById("page")
          .getElementById("sidebar")
          .getElementsByClass("cover-art")
          .first()
          .child(0)
          .attr("href")
      val imageURL = "http:$imageName"
      restApi.downloadFile(imageURL)
    } catch (e: Exception) {
      null
    }
  }

  fun tryGetAlbumInfo(reqArtist: String, reqTitle: String): TrackAlbumInfo? {
    val requestedArtist = reqArtist.lowercase()
    val requestedTitle = reqTitle.lowercase()
    val query = "$requestedArtist+$requestedTitle"
    val url = "$API_PATH/search?query=$query&type=recording&method=indexed"

    try {
      val body = restApi.sendGet(url)

      val table =
          Jsoup.parse(body).body().getElementById("content").getElementsByClass("tbl").first()

      val entries = table.getElementsByTag("tbody").first().children()

      entries.forEach {
        try {
          val title = it.children()[0].text().replace("“", "")
          val artist = it.children()[2].text().lowercase()

          if ((title.lowercase() == requestedTitle) && (artist.lowercase() == requestedArtist)) {

            val album = it.children()[4].text()
            val noInAlbum = it.children()[5].text()
            val track = if (noInAlbum.isNullOrEmpty()) "" else noInAlbum.split("/")[0]
            val albumUrl = it.children()[4].child(0).attr("href")

            return TrackAlbumInfo(album, track, tryGetAlbumCover(albumUrl))
          }
        } catch (e: Exception) {

        }
      }

    } catch (e: Exception) {
      return null
    }

    return null
  }

  companion object {
    private const val API_PATH = "https://musicbrainz.org"
  }
}