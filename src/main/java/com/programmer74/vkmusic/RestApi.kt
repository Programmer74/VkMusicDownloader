package com.programmer74.vkmusic

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStreamReader
import java.net.URI

class RestApi(
        val userAgent: String
) {
    @Throws(Exception::class)
    fun sendGet(url: String): String {
        val client = DefaultHttpClient()
        val request = HttpGet(url)

        request.addHeader("User-Agent", userAgent)

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
        request.addHeader("User-Agent", userAgent)

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
}