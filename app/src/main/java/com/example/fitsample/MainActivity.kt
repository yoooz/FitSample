package com.example.fitsample

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.fitsample.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        binding.button.setOnClickListener {
            readFitData()
        }
        binding.data.setOnClickListener {
            readHistory()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                REQUEST_CODE_ACTIVITY_RECOGNITION
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                REQUEST_CODE_GOOGLE_FIT_PERMISSION -> {
                    Toast.makeText(this, "onActivityResult", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getGoogleAccount() = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

    private fun readFitData() {
        val account = getGoogleAccount()
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this,
                REQUEST_CODE_GOOGLE_FIT_PERMISSION,
                account,
                fitnessOptions
            )
        } else {
            Toast.makeText(this, "readFitData", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readHistory() {
        GlobalScope.launch {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, -30)
            val start = cal.time
            val end = Date()
            val response = Fitness.getHistoryClient(this@MainActivity, getGoogleAccount())
                .readData(DataReadRequest.Builder()
                    .read(DataType.TYPE_STEP_COUNT_DELTA)
                    .setTimeRange(start.time, end.time, TimeUnit.MILLISECONDS)
                    .build())

            val result = Tasks.await(response)
            val dataSet = result.getDataSet(DataType.TYPE_STEP_COUNT_DELTA)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, dataSet.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_ACTIVITY_RECOGNITION = 12345
        private const val REQUEST_CODE_GOOGLE_FIT_PERMISSION = 23456
    }
}