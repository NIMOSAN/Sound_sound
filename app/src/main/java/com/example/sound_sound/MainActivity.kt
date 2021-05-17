package com.example.sound_sound


import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.view.get
import java.io.IOException

private const val LOG_TAG = "AudioRecordTest"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class MainActivity : AppCompatActivity() {
    private var _player: MediaPlayer? = null
    private var recorder: MediaRecorder? = null
    private var fileName: String = ""


    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }

    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }

    private fun onPlay(start: Boolean) = if (start) {
        startPlaying()
    } else {
        stopPlaying()
    }

    private fun startPlaying() {
        _player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        _player?.release()
        _player = null
    }





    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Sound_sound)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        val record = findViewById<Button>(R.id.btRecord) //録音オブジェクト取得
        val stop = findViewById<Button>(R.id.btStop) //録音停止オブジェクト取得
        val playback = findViewById<Button>(R.id.btPlay2) //再生オブジェクト取得

        val listener = RecordButton() //レコードボタンリスナのインスタンス生成

        record.setOnClickListener(listener) //レコードボタンリスナの設定
        stop.setOnClickListener(listener)
        playback.setOnClickListener(listener)

        val lvSound = findViewById<ListView>(R.id.lvSound)
        lvSound.onItemClickListener = ListItemClickListener()
        val loopSwitch = findViewById<Switch>(R.id.swLoop)
        loopSwitch.setOnCheckedChangeListener(LoopSwitchChangedListener())
    }

    private inner class PlayerPreparedListener : MediaPlayer.OnPreparedListener {
        override fun onPrepared(mp: MediaPlayer) {
            val btPlay = findViewById<Button>(R.id.btPlay)
            btPlay.isEnabled = true
            val btBack = findViewById<Button>(R.id.btBack)
            btBack.isEnabled = true
            val btForward = findViewById<Button>(R.id.btForward)
            btForward.isEnabled = true
        }
    }

    private inner class PlayerCompletionListener : MediaPlayer.OnCompletionListener {
        override fun onCompletion(mp: MediaPlayer) {
            _player?.let {
                if(!it.isLooping) {
                    val btPlay = findViewById<Button>(R.id.btPlay)
                    btPlay.setText(R.string.bt_play_play)
                }
            }
        }
    }

    private inner class LoopSwitchChangedListener : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            _player?.isLooping = isChecked
        }
    }

    fun onPlayButtonClick(view: View) {
        _player?.let {
            val btPlay = findViewById<Button>(R.id.btPlay)
            if(it.isPlaying) {
                it.pause()
                btPlay.setText(R.string.bt_play_play)
            }
            else {
                it.start()
                btPlay.setText(R.string.bt_play_pause)
            }
        }
    }

    fun onBackButtonClick(view: View) {
        _player?.seekTo(0)
    }

    fun onForwardButtonClick(view: View) {
        _player?.let {
            val duration = it.duration
            it.seekTo(duration)
            if(!it.isPlaying) {
                it.start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _player?.let {
            if(it.isPlaying) {
                it.stop()
            }
            //プレーヤーを解放。
            it.release()
            _player = null
        }
    }

    fun onCompletion(mp: MediaPlayer){
        _player?.let{
            if(!it.isLooping){
                val btPlay = findViewById<Button>(R.id.btPlay)
                btPlay.setText(R.string.bt_play_play)
            }
        }
    }

    private inner class ListItemClickListener : AdapterView.OnItemClickListener{
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            _player = MediaPlayer()
            val item = parent?.getItemAtPosition(position) as String
            var mediaFileUriStr = "android.resource://${packageName}/${R.raw.misskuzu}"
            when(position){
                0->
                    mediaFileUriStr = "android.resource://${packageName}/${R.raw.kanbenshitekureyo}"
                1->
                    mediaFileUriStr = "android.resource://${packageName}/${R.raw.kuntenkime}"
                2->
                    mediaFileUriStr = "android.resource://${packageName}/${R.raw.misskuzu}"
                3->
                    mediaFileUriStr = "android.resource://${packageName}/${R.raw.misskun}"
                4->
                    mediaFileUriStr = "android.resource://${packageName}/${R.raw.makekun}"
            }
            var mediaFileUri = Uri.parse(mediaFileUriStr)
            try {
                _player?.setDataSource(applicationContext, mediaFileUri)
                _player?.setOnPreparedListener(PlayerPreparedListener())
                _player?.setOnCompletionListener(PlayerCompletionListener())
                _player?.prepareAsync()
            }
            catch(ex: IllegalArgumentException) {
                Log.e("MediaSample", "メディアプレーヤー準備時の例外発生", ex)
                Toast.makeText(applicationContext, "メディアプレイヤー準備時にエラー.もう一度試すにも.", Toast.LENGTH_SHORT).show()
            }
            catch(ex: IOException) {
                Log.e("MediaSample", "メディアプレーヤー準備時の例外発生", ex)
                Toast.makeText(applicationContext, "メディアプレイヤー準備時にエラー.もう一度試すにも.", Toast.LENGTH_SHORT).show()
            }
            Toast.makeText(applicationContext,item,Toast.LENGTH_SHORT).show()
        }
    }
    private inner class RecordButton : View.OnClickListener {
        override fun onClick(v: View?) {
            Log.i(LOG_TAG, "クリック成功")
            Log.i(LOG_TAG, fileName)

            if(v != null){
                when(v.id){
                    //録音開始ボタン
                    R.id.btRecord -> {
                        onRecord(true)
                        Log.i(LOG_TAG, "録音開始")
                    }
                    //録音停止ボタン
                    R.id.btStop -> {
                        onRecord(false)
                        Log.i(LOG_TAG, "録音終了")
                    }

                    R.id.btPlay2 -> {
                        onPlay(true)
                        Log.i(LOG_TAG, "再生中")
                    }
                }
            }
        }
    }
}