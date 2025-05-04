package com.example.pawfectplanner.ui.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pawfectplanner.data.model.Task
import com.example.pawfectplanner.databinding.ItemTaskBinding
import org.threeten.bp.format.DateTimeFormatter

class TaskAdapter(
    private val onClick: (Int) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DIFF) {

    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TaskViewHolder(
            ItemTaskBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class TaskViewHolder(private val b: ItemTaskBinding) :
        RecyclerView.ViewHolder(b.root) {
        init {
            b.root.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onClick(getItem(pos).id)
                }
            }
        }
        fun bind(task: Task) {
            b.tvTaskTitle.text = task.title
            b.tvTaskDateTime.text = task.dateTime.format(fmt)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(a: Task, b: Task) = a.id == b.id
            override fun areContentsTheSame(a: Task, b: Task) = a == b
        }
    }
}
