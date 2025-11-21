package com.pedro.task.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.pedro.task.R
import com.pedro.task.databinding.FragmentRecoverAccountBinding
import com.pedro.task.util.initToolbar
import com.pedro.task.util.showBottomSheet
import com.google.firebase.auth.FirebaseAuth

class RecoverAccountFragment : Fragment() {

    private var _binding: FragmentRecoverAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecoverAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)
        auth = FirebaseAuth.getInstance()

        initListener()
    }
    private fun initListener(){
        binding.buttonEnviar.setOnClickListener{
            valideData()
        }
    }
    private fun valideData(){
        val email = binding.editTextEmail.text.toString().trim()
        if(email.isNotBlank()){
            binding.progressbar.isVisible = true
            recoverAccountUser(email)
        }else{
            showBottomSheet(message = getString(R.string.email_empty))
        }
    }

    private fun recoverAccountUser(email: String){
        try {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    binding.progressbar.isVisible = false
                    if (task.isSuccessful){
                        showBottomSheet(message = getString(R.string.text_button_recover_account_fragment))
                    }else{
                        Toast.makeText(requireContext(),task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }catch (e: Exception){
            Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}