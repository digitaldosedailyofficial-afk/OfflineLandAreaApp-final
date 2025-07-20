\
    package com.example.offlinelandarea

    import android.annotation.SuppressLint
    import android.hardware.Sensor
    import android.hardware.SensorEvent
    import android.hardware.SensorEventListener
    import android.hardware.SensorManager
    import androidx.appcompat.app.AppCompatActivity
    import android.os.Bundle
    import android.view.View
    import com.example.offlinelandarea.databinding.ActivityMainBinding
    import kotlin.math.*

    /**
     * Offline land area estimator using dead-reckoning of steps + heading.
     * Accuracy LIMITATIONS: Drift accumulates quickly. Use only for rough estimates.
     * No logs kept; minimal UI. Entirely offline; uses device sensors only.
     */
    class MainActivity : AppCompatActivity(), SensorEventListener {

        private lateinit var binding: ActivityMainBinding
        private lateinit var sensorManager: SensorManager
        private var stepCounterSensor: Sensor? = null
        private var stepDetectorSensor: Sensor? = null
        private var rotationVectorSensor: Sensor? = null

        private var collecting = false
        private var strideLength = 0.75f // meters default
        private var steps = 0

        private val points: MutableList<Pair<Double, Double>> = mutableListOf(Pair(0.0, 0.0))
        private var currentX = 0.0
        private var currentY = 0.0
        private var currentHeadingRad = 0.0

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

            binding.startButton.setOnClickListener { startCollection() }
            binding.stopButton.setOnClickListener { stopCollection() }
        }

        private fun registerSensors() {
            rotationVectorSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
            stepDetectorSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            } ?: run {
                stepCounterSensor?.let {
                    sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
                }
            }
        }

        private fun unregisterSensors() {
            sensorManager.unregisterListener(this)
        }

        @SuppressLint("SetTextI18n")
        private fun startCollection() {
            if (collecting) return
            strideLength = binding.strideInput.text.toString().toFloatOrNull() ?: 0.75f
            steps = 0
            currentX = 0.0
            currentY = 0.0
            points.clear()
            points.add(Pair(0.0, 0.0))
            updateDisplays()
            collecting = true
            binding.startButton.visibility = View.GONE
            binding.stopButton.visibility = View.VISIBLE
            registerSensors()
        }

        private fun stopCollection() {
            if (!collecting) return
            collecting = false
            unregisterSensors()
            if (points.size > 2) {
                val first = points.first()
                val last = points.last()
                val dist = hypot(first.first - last.first, first.second - last.second)
                if (dist > strideLength / 2) {
                    points.add(first)
                }
            }
            computeAndShowArea()
            binding.startButton.visibility = View.VISIBLE
            binding.stopButton.visibility = View.GONE
        }

        override fun onSensorChanged(event: SensorEvent?) {
            event ?: return
            when (event.sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    currentHeadingRad = orientation[0].toDouble()
                }
                Sensor.TYPE_STEP_DETECTOR -> {
                    if (collecting && event.values.isNotEmpty() && event.values[0] == 1.0f) {
                        recordStep()
                    }
                }
                Sensor.TYPE_STEP_COUNTER -> {
                    if (collecting) {
                        recordStep()
                    }
                }
            }
        }

        private fun recordStep() {
            steps += 1
            val dx = strideLength * cos(currentHeadingRad)
            val dy = strideLength * sin(currentHeadingRad)
            currentX += dx
            currentY += dy
            points.add(Pair(currentX, currentY))
            updateDisplays()
        }

        @SuppressLint("SetTextI18n")
        private fun updateDisplays() {
            binding.stepCountView.text = "Steps: $steps"
            val distance = steps * strideLength
            binding.distanceView.text = "Distance: %.2f m".format(distance)
            val sb = StringBuilder()
            points.takeLast(10).forEach {
                sb.append("(%.1f, %.1f)\\n".format(it.first, it.second))
            }
            binding.debugPolygon.text = sb.toString()
        }

        @SuppressLint("SetTextI18n")
        private fun computeAndShowArea() {
            if (points.size < 4) {
                binding.areaResult.text = "Walked path too small for area (need a closed loop)."
                return
            }
            val areaM2 = abs(shoelaceArea(points))
            val acres = areaM2 / 4046.8564224
            val guntha = acres * 40.0
            val hectare = areaM2 / 10000.0
            val sqFeet = areaM2 * 10.7639104167
            val result = "Sq. Meters: %.2f\\nSq. Feet: %.2f\\nGuntha: %.4f\\nAcres: %.5f\\nHectares: %.5f\\n(Approximate – sensor drift may cause error)".format(
                areaM2, sqFeet, guntha, acres, hectare
            )
            binding.areaResult.text = result
        }

        private fun shoelaceArea(poly: List<Pair<Double, Double>>): Double {
            var sum = 0.0
            for (i in 0 until poly.size - 1) {
                val (x1, y1) = poly[i]
                val (x2, y2) = poly[i + 1]
                sum += (x1 * y2 - x2 * y1)
            }
            return 0.5 * sum
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

        override fun onDestroy() {
            super.onDestroy()
            unregisterSensors()
        }
    }
