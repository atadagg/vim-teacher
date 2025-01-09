package com.example.vimteacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.vimteacher.adapter.QuestionAdapter
import com.example.vimteacher.databinding.FragmentMainBinding
import com.example.vimteacher.services.FirebaseService
import com.example.vimteacher.viewmodel.AuthViewModel
import com.example.vimteacher.viewmodel.AuthViewModelFactory
import com.example.vimteacher.viewmodel.QuestionsViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class MainFragment : Fragment(){
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private var authStateListener: FirebaseAuth.AuthStateListener? = null


    private val adapter = QuestionAdapter { question ->
        findNavController().navigate(MainFragmentDirections.actionMainFragmentToQuestionFragment(id = question.questionId))
    }
    private val viewModel: QuestionsViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels { AuthViewModelFactory() }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        setStatusBarColor()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        observeAuthState()
        setupButtons()
    }

    private fun setupButtons() {
        binding.buttonCheatSheet.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToCheatSheetFragment())
        }

        // Update login/logout button based on auth state
        binding.buttonLogin.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser != null) {
                FirebaseAuth.getInstance().signOut()
                updateLoginButton(false)
                Snackbar.make(
                    binding.root,
                    "Successfully logged out",
                    Snackbar.LENGTH_SHORT
                ).apply {
                    setBackgroundTint(resources.getColor(R.color.primary_color, null))
                    setTextColor(resources.getColor(android.R.color.white, null))
                    setAction("Dismiss") { dismiss() }
                    show()
                }
            } else {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToLoginFragment())
            }
        }
    }

    private fun observeAuthState() {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (_binding != null) {
                updateLoginButton(firebaseAuth.currentUser != null)
            }
        }
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener!!)
    }

    private fun updateLoginButton(isLoggedIn: Boolean) {
        binding.buttonLogin.apply {
            text = if (isLoggedIn) "Logout" else "Login"
        }
    }


    private fun setupRecyclerView(){
        binding.recyclerView.adapter = adapter

    }


    private fun observeViewModel(){
        viewModel.questions.observe(viewLifecycleOwner) { questions ->
            adapter.submitList(questions)
        }
    }

    override fun onDestroyView() {
        authStateListener?.let {
            FirebaseAuth.getInstance().removeAuthStateListener(it)
        }
        super.onDestroyView()
        _binding = null
    }

    private fun setStatusBarColor() {
        val colorPrimary = resources.getColor(R.color.primary_color, null)
        activity?.window?.statusBarColor = colorPrimary
        WindowInsetsControllerCompat(activity?.window!!, activity?.window?.decorView!!).isAppearanceLightStatusBars = false
    }
}