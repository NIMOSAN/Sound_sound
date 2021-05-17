package com.example.sound_sound


import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.view.get
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var _player: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Sound_sound)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
}