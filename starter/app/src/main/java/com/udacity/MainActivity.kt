package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.udacity.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: com.udacity.databinding.ActivityMainBinding

    private var downloadID: Long = 0

    private val NOTIFICATION_ID = 0

    private var fileName: String = ""

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        val radioGroup = binding.contentMain.include.radioGroup

        val loadingButton = binding.contentMain.customButton

        notificationManager = getSystemService(
            NotificationManager::class.java
        ) as NotificationManager

        createChannel(CHANNEL_ID, "Download")

        loadingButton.setOnClickListener {
            val selectedUrl = when (radioGroup.checkedRadioButtonId) {
                R.id.glide_button -> GlideUrl
                R.id.load_app_button -> UdacityUrl
                R.id.retrofit_button -> RetrofitUrl
                else -> null
            }

            fileName = when (radioGroup.checkedRadioButtonId) {
                R.id.glide_button -> getString(R.string.glide_name)
                R.id.load_app_button -> getString(R.string.load_app_name)
                R.id.retrofit_button -> getString(R.string.retrofit_name)
                else -> ""
            }
            if (selectedUrl != null) {
                download(selectedUrl)
            } else {
                val handler = Handler()
                val delayInMillis = 1000L

                handler.postDelayed({
                    binding.contentMain.customButton.completedDownload()
                }, delayInMillis)
                Toast.makeText(this, getString(R.string.warning), Toast.LENGTH_SHORT).show()
            }
        }


    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id != null && id != -1L) {
                val downloadManager =
                    context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query().setFilterById(id)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val status =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        notificationManager.sendNotification(
                            fileName,
                            "Success",
                            context
                        )
                    } else {
                        notificationManager.sendNotification(
                            fileName,
                            "Fail",
                            context
                        )
                    }
                }
                cursor.close()
            }
            binding.contentMain.customButton.completedDownload()
        }
    }

    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_description)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    fun NotificationManager.sendNotification(
        fileName: String,
        status: String,
        applicationContext: Context
    ) {
        val contentIntent = Intent(applicationContext, DetailActivity::class.java)
        contentIntent.putExtra("fileName", fileName)
        contentIntent.putExtra("status", status)

        pendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(
                getString(R.string.notification_title)
            ).setContentText(getString(R.string.notification_description))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                applicationContext.getString(R.string.check_status),
                pendingIntent
            )
        notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        private const val GlideUrl =
            "https://github.com/bumptech/glide/archive/refs/heads/master.zip"
        private const val UdacityUrl =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val RetrofitUrl =
            "https://github.com/square/retrofit/archive/refs/heads/master.zip"
        private const val CHANNEL_ID = "channelId"
    }
}