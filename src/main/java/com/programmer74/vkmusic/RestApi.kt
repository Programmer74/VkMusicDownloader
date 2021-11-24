package com.programmer74.vkmusic

import feign.Util.CONTENT_LENGTH
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.*
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.*

class RestApi(
  private val userAgent: String = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36"
) {

  fun sendGet(url: String, proxyHost: String? = null, proxyPort: Int = 0): String {
    val client = if (proxyHost != null) {
      OkHttpClient.Builder()
          .proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyHost, proxyPort)))
          .build()
    } else OkHttpClient()
    val request = Request.Builder().url(url).header("User-Agent", userAgent).build()

    val response: Response = client.newCall(request).execute()

    if (response.code != 200) {
      error("got ${response.code} on $url; ${response.body}")
    }

    val responseBody: ResponseBody = response.body ?: error("request error")
    return responseBody.string()
  }

  fun downloadFile(url: String, path: File) {
    val client = OkHttpClient()
    val writer = BinaryFileWriter(FileOutputStream(path)) { }
    BinaryFileDownloader(client, writer).download(url)
  }

  fun downloadFile(url: String): ByteArray {
    val client = OkHttpClient()
    val arr = ByteArrayOutputStream()
    val writer = BinaryFileWriter(arr) { }
    BinaryFileDownloader(client, writer).download(url)
    return arr.toByteArray()
  }

  class BinaryFileDownloader(
    private val client: OkHttpClient,
    private val writer: BinaryFileWriter
  ) {

    fun download(url: String): Long {
      val request = Request.Builder().url(url).build()
      val response = client.newCall(request).execute()
      val responseBody = response.body
        ?: throw IllegalStateException("Response doesn't contain a file")
      val length = Objects.requireNonNull(response.header(CONTENT_LENGTH, "1"))?.toLong()
        ?: error("cannot parse length")
      return writer.write(responseBody.byteStream(), length)
    }
  }

  class BinaryFileWriter(
    private val outputStream: OutputStream,
    private val progressCallback: (Double) -> Unit
  ) {

    fun write(inputStream: InputStream, length: Long): Long {
      BufferedInputStream(inputStream).use { input ->
        val dataBuffer = ByteArray(1024)
        var readBytes: Int
        var totalBytes: Long = 0
        while (input.read(dataBuffer).also { readBytes = it } != -1) {
          totalBytes += readBytes.toLong()
          outputStream.write(dataBuffer, 0, readBytes)
          progressCallback.invoke(totalBytes / length * 100.0);
        }
        return totalBytes.also {
          outputStream.close()
        }
      }
    }
  }
}