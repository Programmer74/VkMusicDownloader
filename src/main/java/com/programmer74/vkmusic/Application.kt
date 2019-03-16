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

            println("Retrieving audios...")
            val audios = api.getAudios(155436363)

            println("Got ${audios.size} tracks")

            audios.forEachIndexed { index, vkAudio ->

                val dirName = File(parentDestination, vkAudio.artist!!)
                dirName.mkdir()

                val fileName = File(dirName, vkAudio.title!! + ".mp3")

                api.downloadFile(vkAudio.url!!, fileName)
                print("[${index.toString().padStart(4, ' ')}/${audios.size}] Downloaded $fileName... ")

                val mp3file = Mp3File(fileName)

                if (!mp3file.hasId3v2Tag() || (mp3file.id3v2Tag.title.isNullOrEmpty())) {
                    print("fixing tags... ")
                    val tag = ID3v24Tag()
                    mp3file.id3v2Tag = tag

                    tag.artist = vkAudio.artist!!
                    tag.title = vkAudio.title!!

                    val fixedFileName = File(dirName, vkAudio.title!! + "-fixed.mp3")
                    mp3file.save(fixedFileName.absolutePath)

                    Files.move(fixedFileName.toPath(), fileName.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    println("done ")
                } else {
                    println("contains tags")
                }
            }
        }
    }
}