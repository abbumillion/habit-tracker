package com.app.habit.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.app.habit.R
import com.app.habit.data.HabitDatabase
import com.app.habit.data.User
import com.app.habit.databinding.FragmentSignupBinding
import com.app.habit.util.SessionManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var selectedImageUrl: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        val userDao = HabitDatabase.getInstance(requireContext()).userDao()

        binding.ivProfileSelect.setOnClickListener {
            // Mocking image selection with a random avatar
            selectedImageUrl = "https://i.pravatar.cc/150?u=${System.currentTimeMillis()}"
            Glide.with(this).load(selectedImageUrl).circleCrop().into(binding.ivProfileSelect)
            binding.ivProfileSelect.setPadding(0, 0, 0, 0)
        }

        binding.btnSignUp.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val existingUser = userDao.getUserByEmail(email)
                if (existingUser != null) {
                    Toast.makeText(context, "User already exists", Toast.LENGTH_SHORT).show()
                } else {
                    val newUser = User(email, name, selectedImageUrl, password)
                    userDao.insertUser(newUser)
                    sessionManager.saveUser(newUser)
                    findNavController().navigate(R.id.nav_dashboard)
                }
            }
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.nav_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}