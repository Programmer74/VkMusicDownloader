//package com.programmer74.vkmusic
//
//import com.mpatric.mp3agic.ID3v24Tag
//import com.mpatric.mp3agic.Mp3File
//import java.io.File
//import java.nio.file.Files
//import java.nio.file.StandardCopyOption
//import kotlin.system.exitProcess
//
//class Application {
//  companion object {
//
//    private fun clearString(s: String): String {
//      //I know this looks ugly AF, but for some reason regexps do not work
//      return s.replace("<", "")
//          .replace(">", "")
//          .replace("\"", "")
//          .replace("|", "")
//          .replace(":", "")
//          .replace("*", "")
//          .replace("/", "")
//          .replace("\\", "")
//    }
//
//      @JvmStatic
//    fun main(args: Array<String>) {
//      val token = args[0]
//      val secret = args[1]
//      val parentDestination = File(args[2])
//      val offset = args[3].toInt()
//      val userId = args[4].toInt()
//
//      if (!parentDestination.isDirectory) {
//        println("$parentDestination should be directory")
//        exitProcess(-1)
//      }
//
//      parentDestination.mkdir()
//
//      val api = VkApiGateway(token, secret)
//      val lyricsRetriever = LyricsRetriever(api.restApi)
//      val tagEnhancer = TagEnhancer(api.restApi)
//
//      println("Retrieving audios...")
//      val audios = api.getAudios(userId)
//
//      println("Got ${audios.size} tracks")
//
//      audios.drop(offset)
//          .forEachIndexed { index, vkAudio ->
//
//            try {
//              val clearArtist = clearString(vkAudio.artist!!)
//              val clearTitle = clearString(vkAudio.title!!)
//
//              val dirName = File(parentDestination, clearArtist)
//              dirName.mkdir()
//
//              var fileName = File(dirName, "$clearTitle.mp3")
//
//              api.restApi.downloadFile(vkAudio.url!!, fileName)
//              print(
//                  "[${
//                    (index + offset).toString()
//                        .padStart(4, ' ')
//                  }/${audios.size}] Downloaded $fileName... ")
//
//              val mp3file = Mp3File(fileName)
//
//              val tag =
//                  if (!mp3file.hasId3v2Tag() || (mp3file.id3v2Tag.title.isNullOrEmpty())) ID3v24Tag()
//                  else mp3file.id3v2Tag
//
//              mp3file.id3v2Tag = tag
//
//              if (tag.title.isNullOrEmpty() || tag.artist.isNullOrEmpty()) {
//                print("fixing tags... ")
//                tag.artist = vkAudio.artist!!
//                tag.title = vkAudio.title!!
//              } else {
//                print("tags ok... ")
//              }
//
//              if (tag.lyrics.isNullOrEmpty()) {
//                val lyrics = lyricsRetriever.getLyrics(tag.artist, tag.title)
//                if (lyrics != null) {
//                  print("lyrics found... ")
//                  tag.lyrics = lyrics
//                }
//              } else {
//                print("lyrics ok... ")
//              }
//
//              if (tag.album.isNullOrEmpty()) {
//                val info = tagEnhancer.tryGetAlbumInfo(tag.artist, tag.title)
//                if (info != null) {
//                  tag.album = info.album
//                  tag.track = info.track
//                  print("album found... ")
//                  if (info.cover != null) {
//                    tag.setAlbumImage(info.cover, "image/png")
//                    print("cover found... ")
//                  }
//                }
//              } else {
//                print("album ok... ")
//              }
//
//              val fixedFileName = File(dirName, clearTitle + "-fixed.mp3")
//              mp3file.save(fixedFileName.absolutePath)
//
//              Files.move(
//                  fixedFileName.toPath(),
//                  fileName.toPath(),
//                  StandardCopyOption.REPLACE_EXISTING)
//
//              if (!tag.album.isNullOrEmpty()) {
//                val albumDir = File(dirName.absolutePath, clearString(tag.album))
//                albumDir.mkdir()
//                val newFileName = File(albumDir, clearString(tag.title) + ".mp3")
//                Files.move(
//                    fileName.toPath(),
//                    newFileName.toPath(),
//                    StandardCopyOption.REPLACE_EXISTING)
//              }
//
//              println("done ")
//
//              //to avoid api breakings
//              Thread.sleep(1500)
//            } catch (e: Exception) {
//              println(" error $e")
//            }
//          }
//    }
//  }
//}