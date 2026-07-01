package com.app.habit.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.app.habit.R
import com.app.habit.data.Habit
import com.app.habit.databinding.DialogAddEditHabitBinding
import com.app.habit.ui.MainViewModel

class AddEditHabitDialog : DialogFragment() {

    private var _binding: DialogAddEditHabitBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private var habitToEdit: Habit? = null
    private val categories = listOf("General", "Health", "Work", "Personal", "Finance", "Social", "Hobby")

    companion object {
        private const val ARG_HABIT = "arg_habit"

        fun newInstance(habit: Habit? = null): AddEditHabitDialog {
            val args = Bundle()
            habit?.let { args.putSerializable(ARG_HABIT, it) }
            val fragment = AddEditHabitDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        habitToEdit = arguments?.getSerializable(ARG_HABIT) as? Habit
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddEditHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        setupUI()
    }

    private fun setupUI() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
        binding.actvCategory.setText(categories[0], false)

        habitToEdit?.let { habit ->
            binding.tvDialogTitle.text = getString(R.string.edit_habit)
            binding.etHabitName.setText(habit.name)
            binding.etHabitDescription.setText(habit.description)
            binding.actvCategory.setText(habit.category, false)
            when (habit.frequency) {
                "Daily" -> binding.toggleGroupFrequency.check(R.id.btnDaily)
                "Weekly" -> binding.toggleGroupFrequency.check(R.id.btnWeekly)
                "Monthly" -> binding.toggleGroupFrequency.check(R.id.btnMonthly)
            }
        }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener { saveHabit() }
    }

    private fun saveHabit() {
        val name = binding.etHabitName.text.toString().trim()
        val description = binding.etHabitDescription.text.toString().trim()
        val category = binding.actvCategory.text.toString()
        val frequency = when (binding.toggleGroupFrequency.checkedButtonId) {
            R.id.btnWeekly -> "Weekly"
            R.id.btnMonthly -> "Monthly"
            else -> "Daily"
        }

        if (name.isEmpty()) {
            binding.etHabitName.error = getString(R.string.name_required_error)
            return
        }

        val habit = habitToEdit?.copy(
            name = name,
            description = description,
            category = category,
            frequency = frequency
        ) ?: Habit(name = name, description = description, category = category, frequency = frequency)

        if (habitToEdit == null) {
            viewModel.addHabit(habit)
        } else {
            viewModel.updateHabit(habit)
        }

        dismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}