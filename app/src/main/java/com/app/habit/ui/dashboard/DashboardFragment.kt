package com.app.habit.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.habit.R
import com.app.habit.databinding.FragmentDashboardBinding
import com.app.habit.ui.HabitAdapter
import com.app.habit.ui.MainViewModel
import com.app.habit.ui.dialogs.AddEditHabitDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.app.habit.util.SessionManager

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: HabitAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        sessionManager = SessionManager(requireContext())

        setupUI()
        setupRecyclerView()
        observeData()
    }

    private fun setupUI() {
        val user = sessionManager.getUser()
        binding.tvGreeting.text = if (user != null) getString(R.string.welcome_back, user.name) else getString(R.string.daily_goal)

        val dateFormat = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
        binding.tvCurrentDate.text = dateFormat.format(Date())

        val quotes = listOf(
            R.string.motivation_quote_1,
            R.string.motivation_quote_2,
            R.string.motivation_quote_3,
            R.string.motivation_quote_4,
            R.string.motivation_quote_5,
            R.string.motivation_quote_6,
            R.string.motivation_quote_7
        )
        binding.tvMotivationQuote.setText(quotes.random())

        binding.fabAddHabit.setOnClickListener {
            AddEditHabitDialog.newInstance().show(parentFragmentManager, "add_habit")
        }
    }

    private fun setupRecyclerView() {
        adapter = HabitAdapter(
            habits = emptyList(),
            onCompleteClick = { viewModel.completeHabit(it) },
            onEditClick = { /* Handled in Habits screen */ },
            onDeleteClick = { /* Handled in Habits screen */ },
            onStreakReset = { viewModel.resetStreak(it) }
        )
        binding.rvPendingHabits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPendingHabits.adapter = adapter
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habits.collectLatest { habits ->
                val activeCount = habits.count { it.isActive }
                val totalStreaks = habits.sumOf { it.streak }
                
                binding.tvActiveHabits.text = activeCount.toString()
                binding.tvTotalStreaks.text = totalStreaks.toString()

                val totalToday = habits.count { it.isActive }
                val completedToday = habits.count { 
                    it.isActive && it.lastCompletedDate?.let { date -> isToday(date) } == true 
                }
                
                val progress = if (totalToday > 0) (completedToday * 100 / totalToday) else 0
                binding.cpTodayProgress.setProgress(progress, true)
                binding.tvTodayPercent.text = getString(R.string.completion_rate_format, progress)
                binding.tvProgressStatus.text = getString(R.string.daily_progress_status_format, completedToday, totalToday)

                val pendingHabits = habits.filter { 
                    it.isActive && (it.lastCompletedDate == null || !isToday(it.lastCompletedDate!!))
                }
                
                adapter.updateHabits(pendingHabits)
                binding.tvNoPending.visibility = if (pendingHabits.isEmpty()) View.VISIBLE else View.GONE
            }
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