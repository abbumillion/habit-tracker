package com.app.habit.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.habit.R
import com.app.habit.databinding.FragmentHabitsBinding
import com.app.habit.ui.HabitAdapter
import com.app.habit.ui.MainViewModel
import com.app.habit.ui.dialogs.AddEditHabitDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HabitsFragment : Fragment() {

    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: HabitAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        
        setupRecyclerView()
        observeHabits()

        binding.fabAdd.setOnClickListener {
            AddEditHabitDialog.newInstance().show(childFragmentManager, "add_habit")
        }
    }

    private fun setupRecyclerView() {
        adapter = HabitAdapter(
            habits = emptyList(),
            onCompleteClick = { habit ->
                viewModel.completeHabit(habit)
            },
            onEditClick = { habit ->
                AddEditHabitDialog.newInstance(habit).show(childFragmentManager, "edit_habit")
            },
            onDeleteClick = { habit ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.delete_habit_title)
                    .setMessage(getString(R.string.delete_habit_message, habit.name))
                    .setPositiveButton(R.string.delete_button) { _, _ -> viewModel.deleteHabit(habit) }
                    .setNegativeButton(R.string.cancel_button, null)
                    .show()
            },
            onStreakReset = { habit ->
                viewModel.resetStreak(habit)
                Toast.makeText(requireContext(), getString(R.string.streak_reset_toast, habit.name), Toast.LENGTH_SHORT).show()
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun observeHabits() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.habits.collectLatest { habits ->
                adapter.updateHabits(habits)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}