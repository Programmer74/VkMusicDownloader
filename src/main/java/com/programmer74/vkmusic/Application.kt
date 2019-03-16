package com.programmer74.vkmusic

import com.mpatric.mp3agic.ID3v24Tag
import com.mpatric.mp3agic.Mp3File
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.system.exitProcess

class Application {
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val token = args[0]
            val secret = args[1]
            val parentDestination = File(args[2])

            if (!parentDestination.isDirectory) {
                println("$parentDestination should be directory")
                exitProcess(-1)
            }

            parentDestination.mkdir()

            val api = VkApi(token, secret)
            val lyricsRetriever = LyricsRetriever(api.restApi)
            val tagEnhancer = TagEnhancer(api.restApi)

            println("Retrieving audios...")
            val audios = api.getAudios(155436363)

            println("Got ${audios.size} tracks")

            audios.forEachIndexed { index, vkAudio ->

                try {
                    val dirName = File(parentDestination, vkAudio.artist!!)
                    dirName.mkdir()

                    var fileName = File(dirName, vkAudio.title!! + ".mp3")

                    api.restApi.downloadFile(vkAudio.url!!, fileName)
                    print("[${index.toString().padStart(4, ' ')}/${audios.size}] Downloaded $fileName... ")

                    val mp3file = Mp3File(fileName)

                    val tag = if (!mp3file.hasId3v2Tag() || (mp3file.id3v2Tag.title.isNullOrEmpty())) ID3v24Tag()
                    else mp3file.id3v2Tag

                    mp3file.id3v2Tag = tag

                    if (tag.title.isNullOrEmpty() || tag.artist.isNullOrEmpty()) {
                        print("fixing tags... ")
                        tag.artist = vkAudio.artist!!
                        tag.title = vkAudio.title!!
                    } else {
                        print("tags ok... ")
                    }

                    if (tag.lyrics.isNullOrEmpty()) {
                        val lyrics = lyricsRetriever.getLyrics(tag.artist, tag.title)
                        if (lyrics != null) {
                            print("lyrics found... ")
                            tag.lyrics = lyrics
                        }
                    } else {
                        print("lyrics ok... ")
                    }

                    if (tag.album.isNullOrEmpty()) {
                        val info = tagEnhancer.tryGetAlbumInfo(tag.artist, tag.title)
                        if (info != null) {
                            tag.album = info.album
                            tag.track = info.track
                            print("album found... ")
                            if (info.cover != null) {
                                tag.setAlbumImage(info.cover, "image/png")
                                print("cover found... ")
                            }
                        }
                    } else {
                        print("album ok... ")
                    }

                    val fixedFileName = File(dirName, vkAudio.title!! + "-fixed.mp3")
                    mp3file.save(fixedFileName.absolutePath)

                    Files.move(fixedFileName.toPath(), fileName.toPath(), StandardCopyOption.REPLACE_EXISTING)

                    if (!tag.album.isNullOrEmpty()) {
                        val albumDir = File(dirName, tag.album)
                        albumDir.mkdir()
                        val newFileName = File(albumDir, tag.title + ".mp3")
                        Files.move(fileName.toPath(), newFileName.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    }

                    println("done ")
                } catch (e: Exception) {
                    println(" error $e")
                }
            }
        }
    }
}