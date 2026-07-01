package com.app.habit.ui.performance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.habit.R
import com.app.habit.databinding.FragmentPerformanceBinding
import com.app.habit.ui.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class PerformanceFragment : Fragment() {

    private var _binding: FragmentPerformanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerformanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        observeData()
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habits.collectLatest { habits ->
                if (habits.isEmpty()) {
                    updateEmptyState()
                    return@collectLatest
                }

                val totalHabits = habits.size
                val completedToday = habits.count { it.lastCompletedDate?.let { date -> isToday(date) } == true }
                
                val completionRate = if (totalHabits > 0) (completedToday.toFloat() / totalHabits.toFloat() * 100).toInt() else 0
                val bestStreak = habits.maxOfOrNull { it.streak } ?: 0

                binding.progressCompletion.setProgress(completionRate, true)
                binding.tvCompletionPercent.text = getString(R.string.completion_rate_format, completionRate)
                binding.tvBestStreak.text = bestStreak.toString()
                binding.tvTotalCompleted.text = habits.sumOf { it.streak }.toString()

                val mostConsistent = habits.maxByOrNull { it.streak }?.name ?: getString(R.string.none)
                binding.tvMostConsistent.text = getString(R.string.most_consistent_format, mostConsistent)
                
                val weeklyAvg = (habits.sumOf { it.streak }.toFloat() / 4.0).toInt() // Simplified weekly avg mock
                binding.tvWeeklyAverage.text = getString(R.string.weekly_average_format, weeklyAvg)

                setupChart(habits)
            }
        }
    }

    private fun updateEmptyState() {
        binding.progressCompletion.setProgress(0, true)
        binding.tvCompletionPercent.text = getString(R.string.zero_percent)
        binding.tvBestStreak.text = "0"
        binding.tvTotalCompleted.text = "0"
        binding.tvMostConsistent.text = getString(R.string.most_consistent_format, getString(R.string.none))
        binding.tvWeeklyAverage.text = getString(R.string.weekly_average_format, 0)
        binding.layoutChart.removeAllViews()
    }

    private fun setupChart(habits: List<com.app.habit.data.Habit>) {
        binding.layoutChart.removeAllViews()
        val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        
        // Mocking some data for the chart based on current streaks for visual effect
        for (day in days) {
            val barView = layoutInflater.inflate(R.layout.item_bar, binding.layoutChart, false)
            val bar = barView.findViewById<View>(R.id.viewBar)
            val label = barView.findViewById<TextView>(R.id.tvDayLabel)
            
            label.text = day
            val height = (30..150).random() // Mock height
            val params = bar.layoutParams
            params.height = height
            bar.layoutParams = params
            
            binding.layoutChart.addView(barView)
        }
    }

    private fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val other = Calendar.getInstance().apply { time = date }
        return today.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}