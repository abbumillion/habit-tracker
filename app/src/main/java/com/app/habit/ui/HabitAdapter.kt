package com.app.habit.ui


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.habit.R
import com.app.habit.data.Habit
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HabitAdapter(
    private var habits: List<Habit>,
    private val onCompleteClick: (Habit) -> Unit,
    private val onEditClick: (Habit) -> Unit,
    private val onDeleteClick: (Habit) -> Unit,
    private val onStreakReset: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvHabitName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvHabitDescription)
        val tvFrequency: TextView = itemView.findViewById(R.id.tvFrequency)
        val tvStreak: TextView = itemView.findViewById(R.id.tvStreak)
        val tvLastCompleted: TextView = itemView.findViewById(R.id.tvLastCompleted)
        val cbComplete: MaterialCheckBox = itemView.findViewById(R.id.cbComplete)
        val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)
        val btnResetStreak: MaterialButton = itemView.findViewById(R.id.btnResetStreak)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        val context = holder.itemView.context

        holder.tvName.text = habit.name
        holder.tvDescription.text = habit.description.ifEmpty { context.getString(R.string.no_description) }
        holder.tvFrequency.text = habit.frequency
        holder.tvStreak.text = habit.streak.toString()

        val dateFormat = SimpleDateFormat("MMM dd", Locale.US)
        holder.tvLastCompleted.text = habit.lastCompletedDate?.let {
            context.getString(R.string.last_completed_format, dateFormat.format(it))
        } ?: context.getString(R.string.not_started_yet)

        val completedToday = habit.lastCompletedDate?.let { isToday(it) } ?: false
        
        holder.cbComplete.setOnCheckedChangeListener(null)
        holder.cbComplete.isChecked = completedToday
        
        holder.cbComplete.isEnabled = !completedToday

        holder.cbComplete.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !completedToday) {
                onCompleteClick(habit)
            }
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(habit)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(habit)
        }

        holder.btnResetStreak.setOnClickListener {
            onStreakReset(habit)
        }
    }

    override fun getItemCount(): Int = habits.size

    fun updateHabits(newHabits: List<Habit>) {
        val diffCallback = HabitDiffCallback(habits, newHabits)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        habits = newHabits
        diffResult.dispatchUpdatesTo(this)
    }

    class HabitDiffCallback(
        private val oldList: List<Habit>,
        private val newList: List<Habit>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    private fun isToday(date: java.util.Date): Boolean {
        val today = Calendar.getInstance()
        val other = Calendar.getInstance().apply { time = date }
        return today.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
    }
}