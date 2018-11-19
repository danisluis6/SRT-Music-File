package recipe.android.srtmusicdemo

import android.annotation.SuppressLint
import android.app.Activity
import android.media.MediaPlayer
import android.media.MediaPlayer.OnTimedTextListener
import android.media.MediaPlayer.TrackInfo
import android.media.TimedText
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import java.io.*
import java.util.*


@Suppress("JAVA_CLASS_ON_COMPANION")
class MainActivity : Activity(), OnTimedTextListener {

    private var txtDisplay: TextView? = null

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtDisplay = findViewById(R.id.txtDisplay)

        val player = MediaPlayer.create(this, R.raw.video)
        try {
            player.addTimedTextSource(getSubtitleFile(R.raw.sub),
                    MediaPlayer.MEDIA_MIMETYPE_TEXT_SUBRIP)
            val textTrackIndex = findTrackIndexFor(
                    TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT, player.getTrackInfo())
            if (textTrackIndex >= 0) {
                player.selectTrack(textTrackIndex)
            } else {
                Log.w(TAG, "Cannot find text track!")
            }
            player.setOnTimedTextListener(this)
            player.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun findTrackIndexFor(mediaTrackType: Int, trackInfo: Array<TrackInfo>): Int {
        val index = -1
        return trackInfo.indices.firstOrNull { trackInfo[it].trackType == mediaTrackType }
                ?: index
    }

    private fun getSubtitleFile(resId: Int): String {
        val fileName = resources.getResourceEntryName(resId)
        val subtitleFile = getFileStreamPath(fileName)
        if (subtitleFile.exists()) {
            Log.d(TAG, "Subtitle already exists")
            return subtitleFile.absolutePath
        }
        Log.d(TAG, "Subtitle does not exists, copy it from res/raw")

        // Copy the file from the res/raw folder to your app folder on the device
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = resources.openRawResource(resId)
            outputStream = FileOutputStream(subtitleFile, false)
            copyFile(inputStream, outputStream)
            return subtitleFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            closeStreams(inputStream!!, outputStream!!)
        }
        return ""
    }

    @Throws(IOException::class)
    private fun copyFile(inputStream: InputStream?, outputStream: OutputStream) {
        val buffer = ByteArray(1024)
        var length = inputStream!!.read(buffer)
        while (length > 0) {
            outputStream.write(buffer, 0, length)
            length = inputStream.read(buffer)
        }
    }

    // A handy method I use to close all the streams
    private fun closeStreams(vararg closeables: Closeable) {
        closeables
                .forEach {
                    try {
                        it.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
    }

    override fun onTimedText(mp: MediaPlayer, text: TimedText?) {
        if (text != null) {
            handler.post({
                val seconds = mp.currentPosition / 1000

                txtDisplay!!.text = ("[" + secondsToDuration(seconds) + "] "
                        + text.text)
            })
        }
    }

    private fun secondsToDuration(seconds: Int): String {
        return String.format("%02d:%02d:%02d", seconds / 3600,
                seconds % 3600 / 60, seconds % 60, Locale.US)
    }

    companion object {
        private val TAG = MainActivity.javaClass.simpleName
        private val handler = Handler()
    }
}
