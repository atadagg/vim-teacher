package com.example.vimteacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.vimteacher.databinding.FragmentLoginBinding
import com.example.vimteacher.viewmodel.AuthState
import com.example.vimteacher.viewmodel.AuthViewModel


class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        setStatusBarColor()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLoginButton()
        setupNavigateToRegister()
        observeViewModel()
    }

    private fun setupLoginButton() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            viewModel.login(email, password)
        }
    }

    private fun setupNavigateToRegister() {
        binding.textViewRegister.setOnClickListener {
            findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            )
        }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Success -> {
                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToMainFragment()
                    )
                }
                is AuthState.Error -> {
                    // Show error message
                    binding.textViewError.text = state.message
                    binding.textViewError.visibility = View.VISIBLE
                }
                is AuthState.Loading -> {
                    // Show loading state
                    binding.progressBar.visibility = View.VISIBLE
                    binding.buttonLogin.isEnabled = false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setStatusBarColor() {
        val colorPrimary = resources.getColor(R.color.primary_color, null)
        activity?.window?.statusBarColor = colorPrimary
        WindowInsetsControllerCompat(activity?.window!!, activity?.window?.decorView!!)
            .isAppearanceLightStatusBars = false
    }
}