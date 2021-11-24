package com.programmer74.vkmusic

import com.mpatric.mp3agic.ID3v2
import com.mpatric.mp3agic.ID3v24Tag
import com.mpatric.mp3agic.Mp3File
import mu.KLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.system.exitProcess

class Application {
  companion object : KLogging() {

    private val regex = Regex("[\\\\/:\"*<>|]")
    private val restApi = RestApi()
    private lateinit var vkApiGateway: VkApiGateway
    private val lyricsRetriever = LyricsRetriever(restApi)
    private val musicBrainzGateway = MusicBrainzGateway(restApi)

    @JvmStatic
    fun main(args: Array<String>) {
      if (args.isEmpty()) {
        logger.warn { "Usage: ./executable <token> <secret> <target music folder> <offset> <userId>" }
      }

      val token = args[0]
      val secret = args[1]
      val parentDestination = File(args[2])
      val offset = args[3].toInt()
      val userId = args[4].toInt()

      if (!parentDestination.isDirectory) {
        logger.error { "$parentDestination should be directory" }
        exitProcess(-1)
      }

      parentDestination.mkdir()

      logger.warn { "Retrieving audios..." }
      vkApiGateway = VkApiGateway(token, secret)
      val audios = vkApiGateway.getAudios(userId)

      logger.warn { "Got ${audios.size} tracks" }
      audios.drop(offset)
          .forEachIndexed { index, vkAudio ->
            try {
              saveAudio(index, vkAudio, audios.size, parentDestination, offset)
            } catch (e: Exception) {
              logger.error(e) { "Error in main loop" }
            }
          }
    }

    private fun saveAudio(
      index: Int,
      vkAudio: VkAudio,
      count: Int,
      parentDestination: File,
      offset: Int
    ) {
      val clearArtist = clearString(vkAudio.artist!!)
      val clearTitle = clearString(vkAudio.title!!)

      val dirName = File(parentDestination, clearArtist)
      dirName.mkdir()

      val downloadedFile = File(dirName, "$clearTitle.mp3")
      restApi.downloadFile(vkAudio.url!!, downloadedFile)
      val preamble = "[${(index + offset).toString().padStart(4, ' ')}/${count}]"
      logger.warn { "$preamble Downloaded $downloadedFile" }

      val mp3file = Mp3File(downloadedFile)

      var tag =
          if (!mp3file.hasId3v2Tag() || (mp3file.id3v2Tag.title.isNullOrEmpty())) ID3v24Tag()
          else mp3file.id3v2Tag

      mp3file.id3v2Tag = tag

      tag = fillArtistAndTitle(tag, vkAudio)
      tag = fillLyrics(tag, vkAudio)
      tag = fillAlbumCover(tag)

      val fixedFileName = File(dirName, "$clearTitle-fixed.mp3")
      mp3file.save(fixedFileName.absolutePath)
      Files.move(
          fixedFileName.toPath(),
          downloadedFile.toPath(),
          StandardCopyOption.REPLACE_EXISTING)

      if (!tag.album.isNullOrEmpty()) {
        val albumDir = File(dirName.absolutePath, clearString(tag.album))
        albumDir.mkdir()
        val patchedFile = File(albumDir, clearString(tag.title) + ".mp3")
        Files.move(
            downloadedFile.toPath(),
            patchedFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING)
      }

      logger.warn { "  Done for this file" }
      //to avoid being banned for scraping
      Thread.sleep(1000L + Random.nextInt(500..1500))
    }

    private fun fillArtistAndTitle(tag: ID3v2, vkAudio: VkAudio): ID3v2 {
      if (tag.title.isNullOrEmpty() || tag.artist.isNullOrEmpty()) {
        logger.warn { "  Adding Artist/Title to mp3 info" }
        tag.artist = vkAudio.artist!!
        tag.title = vkAudio.title!!
      } else {
        logger.warn { "  Artist/Title in mp3 info OK" }
      }
      return tag
    }

    private fun fillLyrics(tag: ID3v2, vkAudio: VkAudio): ID3v2 {
      if (tag.lyrics.isNullOrEmpty()) {
        logger.warn { "  Lyrics are not present in mp3 info" }
        return if (vkAudio.lyrics_id != null) {
          logger.warn { "  Lyrics are present in vk; retrieving" }
          val lyrics = vkApiGateway.getLyrics(vkAudio.lyrics_id!!)
          fillLyrics(tag, lyrics)
        } else {
          logger.warn { "  Lyrics are not present in vk; retrieving from external source..." }
          val lyrics = lyricsRetriever.getLyrics(tag.artist, tag.title)
          fillLyrics(tag, lyrics)
        }
      } else {
        logger.warn { "  Lyrics are present in mp3 info" }
      }
      return tag
    }

    private fun fillLyrics(tag: ID3v2, lyrics: String?): ID3v2 {
      if (lyrics != null) {
        logger.warn { "    Done; shall add that to mp3 info" }
        tag.lyrics = lyrics
      } else {
        logger.warn { "    Failed to find lyrics" }
      }
      return tag
    }

    private fun fillAlbumCover(tag: ID3v2): ID3v2 {
      if (tag.album.isNullOrEmpty()) {
        logger.warn { "  Album cover is not present in mp3 info; trying to retrieve" }
        val info = musicBrainzGateway.tryGetAlbumInfo(tag.artist, tag.title)
        if (info != null) {
          tag.album = info.album
          tag.track = info.track
          logger.warn { "  Found album" }
          if (info.cover != null) {
            tag.setAlbumImage(info.cover, "image/png")
            logger.warn { "    Found cover; shall add it to mp3 info" }
          } else {
            logger.warn { "    Failed to find cover" }
          }
        } else {
          logger.warn { "    Failed to find album info" }
        }
      } else {
        logger.warn { "  Album cover is present in mp3 info" }
      }
      return tag
    }

    private fun clearString(s: String): String {
      return s.replace(regex, "")
    }
  }
}