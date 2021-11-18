package com.programmer74.vkmusic

import org.jsoup.Jsoup
import java.net.URL
import java.net.URLEncoder
import java.nio.file.Files

data class TrackAlbumInfo(
  var album: String,
  var track: String,
  var cover: ByteArray? = null
)

class TagEnhancer(
  val restApi: RestApi
) {

  private val API_PATH = "https://musicbrainz.org"

  private val downloader = RedirectorDownloader()

  fun tryGetAlbumCover(id: String): ByteArray? {
      val url = "$API_PATH$id"

    var htmlBody = ""
    try {
      htmlBody = restApi.sendGet(url)
      val content = Jsoup.parse(htmlBody).body()
      var imageName = content.getElementById("page")
          .getElementById("sidebar")
          .getElementsByClass("cover-art")
          .first()
          .child(0)
          .attr("href")
      val imageURL = "http:$imageName"
      val file = Files.createTempFile(id.split("/")[2], ".png").toFile()
      val cover = downloader.download(URL(imageURL), file)

      return Files.readAllBytes(cover.toPath())
    } catch (e: Exception) {
      return null
    }
    return null
  }

  fun tryGetAlbumInfo(requestedArtist: String, requestedTitle: String): TrackAlbumInfo? {
    var query = requestedArtist + " " + requestedTitle
    query = URLEncoder.encode(query)

    val url = "$API_PATH/search?query=$query&type=recording&method=indexed"

    var body = ""
    try {
      body = restApi.sendGet(url)

      val table =
          Jsoup.parse(body).body().getElementById("content").getElementsByClass("tbl").first()

      val entries = table.getElementsByTag("tbody").first().children()

      entries.forEach {
        try {
          val title = it.children()[0].text().replace("“", "").replace("”", "")
          val artist = it.children()[2].text()

          if ((title == requestedTitle) && (artist == requestedArtist)) {

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
}