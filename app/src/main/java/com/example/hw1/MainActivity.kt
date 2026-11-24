package com.example.hw1

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var seekBar: SeekBar
    private lateinit var tvCount: TextView
    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnSequential: Button
    private lateinit var btnParallel: Button
    private lateinit var btnCancel: Button

    private var job: Job? = null
    private var coroutineCount: Int = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        seekBar = findViewById(R.id.seekBar)
        tvCount = findViewById(R.id.tvCount)
        tvStatus = findViewById(R.id.tvStatus)
        progressBar = findViewById(R.id.progressBar)
        btnSequential = findViewById(R.id.btnSequential)
        btnParallel = findViewById(R.id.btnParallel)
        btnCancel = findViewById(R.id.btnCancel)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                coroutineCount = progress + 1
                tvCount.text = "Количество корутин: $coroutineCount"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        btnSequential.setOnClickListener { startSequential() }
        btnParallel.setOnClickListener { startParallel() }
        btnCancel.setOnClickListener { cancelAll() }
    }

    private fun startSequential() {
        val n = coroutineCount
        startUiForRunning()

        job = lifecycleScope.launch {
            try {
                val total = n * 100L
                var done = 0L
                for (i in 1..n) {
                    updateStatus("Последовательно: корутина $i из $n")
                    repeat(10) { step ->
                        delay(Random.nextLong(40, 120))
                        done++
                        withContext(Dispatchers.Main) {
                            progressBar.progress = ((done * 100) / total).toInt()
                        }
                    }
                }
                updateStatus("Готово (последовательно)")
            } catch (e: CancellationException) {
                updateStatus("Отменено")
                throw e
            } finally {
                if (isActive) resetUiAfterRun()
                else resetUiAfterRun()
            }
        }
    }

    private fun startParallel() {
        val n = coroutineCount
        startUiForRunning()

        job = lifecycleScope.launch {
            try {
                updateStatus("Параллельно: $n корутин")
                val total = n * 10L
                var done = 0L
                val mutex = kotlinx.coroutines.sync.Mutex()

                coroutineScope {
                    repeat(n) { index ->
                        launch {
                            repeat(10) {
                                delay(Random.nextLong(40, 120))
                                mutex.lock()
                                try {
                                    done++
                                    progressBar.progress = ((done * 100) / total).toInt()
                                } finally {
                                    mutex.unlock()
                                }
                            }
                        }
                    }
                }
                updateStatus("Готово (параллельно)")
            } catch (e: CancellationException) {
                updateStatus("Отменено")
                throw e
            } finally {
                resetUiAfterRun()
            }
        }
    }

    private fun cancelAll() {
        job?.cancel()
        job = null
    }

    private fun startUiForRunning() {
        progressBar.progress = 0
        seekBar.isEnabled = false
        btnSequential.isEnabled = false
        btnParallel.isEnabled = false
        btnCancel.isEnabled = true
    }

    private fun resetUiAfterRun() {
        seekBar.isEnabled = true
        btnSequential.isEnabled = true
        btnParallel.isEnabled = true
        btnCancel.isEnabled = false
        job = null
    }

    private fun updateStatus(text: String) {
        tvStatus.text = text
    }
}