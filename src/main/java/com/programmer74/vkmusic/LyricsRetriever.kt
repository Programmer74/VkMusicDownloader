package com.programmer74.vkmusic

import org.jsoup.Jsoup

class LyricsRetriever(
        val restApi: RestApi
) {

    private val API_PATH = "https://www.azlyrics.com/lyrics"

    private val regex = Regex.fromLiteral("[^a-z]")

    fun clearString(x: String): String {
        return x.toLowerCase().replace(regex, "").replace(" ", "")
    }

    fun getLyrics(rawArtist: String, rawTitle: String): String? {
        val artist = clearString(rawArtist)
        if (artist.isEmpty()) return null

        val title = clearString(rawTitle)
        if (title.isEmpty()) return null

        val url = "$API_PATH/$artist/$title.html"

        var ans = ""
        try {
            ans = restApi.sendGet(url, "185.248.161.164", 8888)
        } catch (e: Exception) {
            return null
        }

        val findBegin = "<div class=\"lyricsh\">"
        val findEnd = "<div class=\"noprint\">"

        val from = ans.indexOf(findBegin)
        if (from == -1) return null

        val to = ans.indexOf(findEnd, from)
        if (to == -1) return null

        val lyricsRaw = ans.substring(from..to - 1).replace("<br>", "\\n")
        val lyrics = Jsoup.parse(lyricsRaw).text().replace("\\n", System.lineSeparator())

        return lyrics
    }
}