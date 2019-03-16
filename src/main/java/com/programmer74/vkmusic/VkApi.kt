package com.programmer74.vkmusic

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.URI
import org.apache.commons.io.IOUtils
import java.io.File

class VkApi(
        val token: String,
        val secret: String
) {
    private val USER_AGENT = "VKAndroidApp/4.38-849 (Android 6.0; SDK 23; x86; Google Nexus 5X; ru)"
    private val API_PATH = "https://api.vk.com"

    @Throws(Exception::class)
    private fun sendGet(url: String): String {
        val client = DefaultHttpClient()
        val request = HttpGet(url)

        request.addHeader("User-Agent", USER_AGENT)

        val response = client.execute(request)

        val reasonPhrase = response.statusLine.reasonPhrase
        val statusCode = response.statusLine.statusCode

        if (response.statusLine.statusCode != 200) {
            throw Exception("Status code $statusCode on url $url with reason $reasonPhrase")
        }

        val rd = BufferedReader(
                InputStreamReader(response.entity.content))

        val result = StringBuffer()
        var line: String? = ""
        while (line != null) {
            result.append(line)
            line = rd.readLine()
        }

        return result.toString()
    }

    fun downloadFile(url: String, path: File) {
        val uri = URI(url)
        val request = HttpGet(uri)
        request.addHeader("User-Agent", USER_AGENT)

        val httpclient = DefaultHttpClient()

        val response = httpclient.execute(request)

        val reasonPhrase = response.statusLine.reasonPhrase
        val statusCode = response.statusLine.statusCode

        if (response.statusLine.statusCode != 200) {
            throw Exception("Status code $statusCode on url $url with reason $reasonPhrase")
        }

        val entity = response.entity
        val content = entity.content

        val baos = ByteArrayOutputStream(1024 * 1024)

        // apache IO util
        try {
            IOUtils.copy(content, baos)
        } finally {
            // close http network connection
            content.close()
        }
        val bytes = baos.toByteArray()

        FileUtils.writeByteArrayToFile(path, bytes)
    }

    private fun doMethod(method: String, methodParams: Map<String, String>): String {
        val params = HashMap<String, String>()
        params["v"] = "5.64"
        params["lang"] = "ru"
        params["https"] = "1"
        params.putAll(methodParams)
        var rq = "/method/$method?"
        params.forEach {
            rq += it.key + "=" + it.value + "&"
        }
        rq += "access_token=$token"

        val sig = DigestUtils.md5Hex(rq + secret)

        val url = "$API_PATH$rq&sig=$sig"
        return sendGet(url)
    }

    fun getAudios(ownerId: Int): List<VkAudio> {
        val ans = doMethod("audio.get", mapOf("owner_id" to ownerId.toString()))
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val audios: VkApiResponseWrapper<VkAudio> = mapper.readValue(ans)
        return audios.response!!.items!!
    }
}