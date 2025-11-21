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
import androidx.navigation.fragment.navArgs
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.pedro.task.R
import com.pedro.task.data.model.Status
import com.pedro.task.data.model.Task
import com.pedro.task.databinding.FragmentFormTaskBinding
import com.pedro.task.util.initToolbar
import com.pedro.task.util.showBottomSheet

class FormTaskFragment : Fragment() {

    private var _binding: FragmentFormTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var task: Task
    private var newTask: Boolean = true
    private var status: Status = Status.TODO

    private lateinit var reference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val args: FormTaskFragmentArgs by navArgs()
    private val viewModel: TaskViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reference = Firebase.database.reference
        auth = Firebase.auth

        initToolbar(binding.toolbar)

        if (args.task == null) {
            binding.toolbar.title = "Nova tarefa"

            // aplica initialStatus APENAS na criação
            val initialStatus = args.initialStatus ?: "todo"
            when (initialStatus) {
                "todo" -> binding.rbTodo.isChecked = true
                "doing" -> binding.rbDoing.isChecked = true
                "done" -> binding.rbDone.isChecked = true
            }

        } else {
            task = args.task!!
            newTask = false
            binding.toolbar.title = "Editar"

            // → na edição, NÃO usa initialStatus
            configTask()
        }

        initListener()
    }

    private fun configTask() {
        status = task.status
        binding.editTextDescricao.setText(task.description)
        setStatus()
    }

    private fun setStatus() {
        val id = when (task.status) {
            Status.TODO -> R.id.rbTodo
            Status.DOING -> R.id.rbDoing
            Status.DONE -> R.id.rbDone
        }
        binding.radioGroup.check(id)
    }

    private fun initListener() {

        binding.buttonSave.setOnClickListener {

            status = when (binding.radioGroup.checkedRadioButtonId) {
                binding.rbTodo.id -> Status.TODO
                binding.rbDoing.id -> Status.DOING
                binding.rbDone.id -> Status.DONE
                else -> Status.TODO
            }

            validateData()
        }
    }

    private fun validateData() {
        val description = binding.editTextDescricao.text.toString().trim()

        if (description.isEmpty()) {
            showBottomSheet(message = getString(R.string.description_empty_form_task_fragment))
            return
        }

        binding.progressBar.isVisible = true

        if (newTask) {
            task = Task()
            task.id = reference.push().key ?: ""
        }

        task.description = description
        task.status = status

        saveTask()
    }

    private fun saveTask() {
        reference.child("task")
            .child(auth.currentUser?.uid ?: "")
            .child(task.id)
            .setValue(task)
            .addOnCompleteListener { result ->
                binding.progressBar.isVisible = false

                if (result.isSuccessful) {

                    if (!newTask) viewModel.setUpdateTask(task)

                    Toast.makeText(
                        requireContext(),
                        R.string.text_save_sucess_form_task_fragment,
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().popBackStack()
                } else {
                    showBottomSheet(message = getString(R.string.error_generic))
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}