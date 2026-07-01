package com.app.habit.ui.milestones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.habit.R
import com.app.habit.databinding.FragmentMilestonesBinding
import com.app.habit.ui.MainViewModel
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MilestonesFragment : Fragment() {

    private var _binding: FragmentMilestonesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: MilestonesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMilestonesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        
        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = MilestonesAdapter()
        binding.rvMilestones.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMilestones.adapter = adapter
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habits.collectLatest { habits ->
                val totalStreaks = habits.sumOf { it.streak }
                val activeHabits = habits.count { it.isActive }
                val maxStreak = habits.maxOfOrNull { it.streak } ?: 0
                val totalHabitsCount = habits.size

                val milestones = listOf(
                    Milestone("Getting Started", "Add your first habit", 1, totalHabitsCount),
                    Milestone("Consistency Starter", "Reach 10 total completions", 10, totalStreaks),
                    Milestone("Halfway to 100", "Reach 50 total completions", 50, totalStreaks),
                    Milestone("Centurion", "Reach 100 total completions", 100, totalStreaks),
                    Milestone("Habit Collector", "Have 5 active habits", 5, activeHabits),
                    Milestone("Procrastination Killer", "Have 10 active habits", 10, activeHabits),
                    Milestone("Monthly Streak", "Reach a 30-day streak on any habit", 30, maxStreak),
                    Milestone("Bi-Monthly Streak", "Reach a 60-day streak on any habit", 60, maxStreak),
                    Milestone("Quarterly Streak", "Reach a 90-day streak on any habit", 90, maxStreak),
                    Milestone("Century Streak", "Reach a 100-day streak on any habit", 100, maxStreak),
                    Milestone("Half-Year Streak", "Reach a 180-day streak on any habit", 180, maxStreak),
                    Milestone("Yearly Streak", "Reach a 365-day streak on any habit", 365, maxStreak),
                    Milestone("Habit Titan", "Reach 500 total completions", 500, totalStreaks),
                    Milestone("Habit Legend", "Reach 1000 total completions", 1000, totalStreaks),
                    Milestone("Habit Master", "Have 20 active habits", 20, activeHabits),
                    Milestone("Habit Collector Elite", "Add 100 total habits", 100, totalHabitsCount)
                )
                adapter.submitList(milestones)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class Milestone(
        val title: String,
        val description: String,
        val target: Int,
        val current: Int
    ) {
        val progress: Int = if (target > 0) (current.coerceAtMost(target) * 100 / target) else 0
        val isUnlocked: Boolean = current >= target
    }

    class MilestonesAdapter : RecyclerView.Adapter<MilestonesAdapter.ViewHolder>() {
        private var items = listOf<Milestone>()

        fun submitList(newList: List<Milestone>) {
            items = newList
            notifyDataSetChanged()
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.ivMilestoneIcon)
            val title: TextView = view.findViewById(R.id.tvMilestoneTitle)
            val description: TextView = view.findViewById(R.id.tvMilestoneDescription)
            val progress: LinearProgressIndicator = view.findViewById(R.id.progressMilestone)
            val status: TextView = view.findViewById(R.id.tvMilestoneStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_milestone, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val context = holder.itemView.context
            holder.title.text = item.title
            holder.description.text = item.description
            holder.progress.progress = item.progress
            
            if (item.isUnlocked) {
                holder.status.text = context.getString(R.string.unlocked)
                holder.icon.alpha = 1.0f
                holder.status.setTextColor(context.getColor(android.R.color.holo_green_dark))
            } else {
                holder.status.text = context.getString(R.string.milestone_progress_format, item.current, item.target)
                holder.icon.alpha = 0.3f
                holder.status.setTextColor(context.getColor(R.color.fb_blue))
            }
        }

        override fun getItemCount() = items.size
    }
}