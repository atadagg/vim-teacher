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
import com.example.vimteacher.viewmodel.QuestionsViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class MainFragment : Fragment(){
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private var authStateListener: FirebaseAuth.AuthStateListener? = null


    private val adapter = QuestionAdapter(
        onItemClick = { question ->
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToQuestionFragment(id = question.questionId))
        },
        solvedQuestionIds = setOf()
    )
    private val viewModel: QuestionsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        FirebaseAuth.getInstance().currentUser?.let { user ->
            viewModel.observeSolvedQuestions(user.uid)
        }
    }

    private fun setupButtons() {
        binding.buttonCheatSheet.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToCheatSheetFragment())
        }

        binding.buttonLeaderboard.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToLeaderboardFragment())
        }

        // Update login/logout button based on auth state
        binding.buttonLogin.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser != null) {
                FirebaseAuth.getInstance().signOut()
                updateLoginButton(false)
                adapter.setSolvedQuestions(emptySet())
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


    private fun setupRecyclerView() {
        binding.recyclerView.adapter = adapter
    }


    private fun observeViewModel() {
        viewModel.questions.observe(viewLifecycleOwner) { questions ->
            adapter.submitList(questions)
        }

        viewModel.solvedQuestions.observe(viewLifecycleOwner) { solvedQuestions ->
            adapter.setSolvedQuestions(solvedQuestions)
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