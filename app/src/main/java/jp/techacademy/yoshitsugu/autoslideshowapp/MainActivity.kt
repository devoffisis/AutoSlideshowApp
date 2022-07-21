package jp.techacademy.yoshitsugu.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import java.util.*
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer: Timer? = null
    private var mTimerSec = 0.0
    private var mHandler = Handler()

    val imageUris = mutableListOf<Uri>()
    private var imageNumber = 0
    private var maxImageNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo()
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
        } else {
            // Android 5系以下の場合
            getContentsInfo()
        }

        val timer = findViewById<TextView>(R.id.timer)
        val nextButton = findViewById<Button>(R.id.nextButton)
        val backButton = findViewById<Button>(R.id.backButton)
        val playButton = findViewById<Button>(R.id.playButton)
        val imageView = findViewById<ImageView>(R.id.imageView)
        // val changeImageSecText = findViewById<EditText>(R.id.changeImageSecText)

        if (imageUris.isNotEmpty()) {
            imageView.setImageURI(imageUris[0])
            maxImageNumber = imageUris.size-1
        }

        nextButton.setOnClickListener{
            imageNumber += 1
            imageNumber = if (imageNumber > maxImageNumber) 0 else imageNumber
            imageView.setImageURI(imageUris[imageNumber])
        }
        backButton.setOnClickListener{
            imageNumber -= 1
            imageNumber = if (imageNumber == -1) maxImageNumber else imageNumber
            imageView.setImageURI(imageUris[imageNumber])
        }
        playButton.setOnClickListener{
            mTimerSec = 0.0
            var mImageSec = 0.1
            val changeImageSec = 2.0
            val error = mImageSec

            if (mTimer == null){
                nextButton.isClickable = false
                backButton.isClickable = false
                mTimer = Timer()
                mTimer!!.schedule(object: TimerTask(){
                    override fun run (){
                        // val changeImageSec = changeImageSecText.text.toString().toFloat()
                        mTimerSec += 0.1
                        mImageSec += 0.1
                        mHandler.post {
                            timer.text = String.format("%.1f", mTimerSec)
                            // Log.d("DEBUG!!!!!", abs(mImageSec.mod(changeImageSec)).toString())
                            if (abs(mImageSec.mod(changeImageSec)) < error) {
                                imageNumber += 1
                                imageNumber = if (imageNumber > maxImageNumber) 0 else imageNumber
                                Log.d("DEBUG: imageNumber", imageNumber.toString())
                                imageView.setImageURI(imageUris[imageNumber])
                                // imageView.setImageURI(imageUris[imageNumber])
                            }
                        }
                    }
                }, 100,100)
            } else {
                mTimer!!.cancel()
                mTimer = null
                nextButton.isClickable = true
                backButton.isClickable = true
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }
    private fun getContentsInfo() {
        // val imageView = findViewById<ImageView>(R.id.imageView)
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                Log.d("DEBUG: AutoSlideshowApp", "URI : " + imageUri.toString())
                imageUris.add(imageUri)
                // imageView.setImageURI(imageUri)
            } while (cursor.moveToNext())
        }
        cursor.close()
    }
}
