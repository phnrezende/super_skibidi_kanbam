package com.pedro.task.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.*
import com.pedro.task.R
import com.pedro.task.data.model.Status
import com.pedro.task.data.model.Task
import com.pedro.task.databinding.FragmentDoingBinding
import com.pedro.task.ui.adapter.TaskAdapter
import com.pedro.task.util.showBottomSheet

class DoingFragment : Fragment() {

    private var _binding: FragmentDoingBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var reference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val viewModel: TaskViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reference = Firebase.database.reference
        auth = Firebase.auth

        initListeners()
        initRecyclerViewTask()
        getTask()
    }

    private fun initListeners() {
        binding.floatingActionButton2.setOnClickListener {
            val action = HomeFragmentDirections
                .actionHomeFragmentToFormTaskFragment(null, "doing")
            findNavController().navigate(action)
        }

        observerViewModel()
    }

    private fun observerViewModel() {
        viewModel.taskList.observe(viewLifecycleOwner) { fullList ->
            val filtered = fullList.filter { it.status == Status.DOING }
            taskAdapter.submitList(filtered)
            listEmpty(filtered)
        }
    }

    private fun initRecyclerViewTask() {
        taskAdapter = TaskAdapter(requireContext()) { task, option ->
            optionSelected(task, option)
        }

        binding.recyclerViewTask.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewTask.adapter = taskAdapter
    }

    private fun optionSelected(task: Task, option: Int) {
        when (option) {
            TaskAdapter.SELECT_REMOVER -> {
                showBottomSheet(
                    titleDialog = R.string.text_title_dialog_delete,
                    message = getString(R.string.text_message_dialog_delete),
                    titleButton = R.string.text_button_dialog_confirm
                ) {
                    deleteTask(task)
                }
            }

            TaskAdapter.SELECT_EDIT -> {
                val action = HomeFragmentDirections.actionHomeFragmentToFormTaskFragment(task)
                findNavController().navigate(action)
            }

            TaskAdapter.SELECT_DETAILS -> {
                Toast.makeText(requireContext(), "Detalhes ${task.description}", Toast.LENGTH_SHORT).show()
            }

            TaskAdapter.SELECT_NEXT -> {
                moveTaskToNextStatus(task)
            }

            TaskAdapter.SELECT_BACK -> {
                moveTaskToPreviousStatus(task)
            }
        }
    }

    private fun moveTaskToNextStatus(task: Task) {
        val nextStatus = when (task.status) {
            Status.TODO -> Status.DOING
            Status.DOING -> Status.DONE
            Status.DONE -> Status.TODO
        }

        updateTaskStatus(task, nextStatus)
    }

    private fun moveTaskToPreviousStatus(task: Task) {
        val previousStatus = when (task.status) {
            Status.TODO -> Status.DONE
            Status.DOING -> Status.TODO
            Status.DONE -> Status.DOING
        }

        updateTaskStatus(task, previousStatus)
    }



    private fun updateTaskStatus(task: Task, status: Status) {
        reference
            .child("task")
            .child(auth.currentUser!!.uid)
            .child(task.id)
            .child("status")
            .setValue(status)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Movido para ${status.name.capitalize()}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Erro ao atualizar o status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteTask(task: Task) {
        reference.child("task")
            .child(auth.currentUser!!.uid)
            .child(task.id)
            .removeValue()
    }

    private fun getTask() {
        reference.child("task")
            .child(auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Task>()

                    for (ds in snapshot.children) {
                        val task = ds.getValue(Task::class.java)
                        if (task != null) list.add(task)
                    }

                    binding.progressBar.isVisible = false
                    list.reverse()
                    viewModel.setAllTasks(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Erro ao carregar", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun listEmpty(list: List<Task>) {
        binding.textInfo.text = if (list.isEmpty()) {
            getString(R.string.text_list_task_empty)
        } else ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}