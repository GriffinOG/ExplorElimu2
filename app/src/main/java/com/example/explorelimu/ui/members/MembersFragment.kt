package com.example.explorelimu.ui.members

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.explorelimu.MainActivity
import com.example.explorelimu.R
import com.example.explorelimu.data.member.learner.LearnersAdapter
import com.example.explorelimu.data.member.teacher.TeachersAdapter
import com.example.explorelimu.data.member.MembersRepository
import com.google.android.material.appbar.AppBarLayout

class MembersFragment : Fragment() {

    private lateinit var membersViewModel: MembersViewModel
    private lateinit var membersRepository: MembersRepository
    private lateinit var membersRecyclerView: RecyclerView
    private lateinit var teachersAdapter: TeachersAdapter
    private lateinit var learnersAdapter: LearnersAdapter

    private val LEARNER = "learner"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        membersViewModel =
//            ViewModelProvider(this).get(MembersViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_members, container, false)
        membersRecyclerView = root.findViewById(R.id.members_rv)
        membersRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        if ((context as MainActivity).userType != null) {
            val userType = (context as MainActivity).userType
            if (userType == LEARNER) initializeViewModelForLearner() else initializeViewModelForTeacher()
        }

        return root
    }

    fun initializeViewModelForLearner(){
        Log.d(javaClass.name, "initializeViewModel called")
        teachersAdapter = TeachersAdapter(requireContext())
        membersRecyclerView.adapter = teachersAdapter

        membersRepository = MembersRepository(requireContext()).getInstance()!!

        membersViewModel =
                ViewModelProvider(this, MembersViewModel.FACTORY(membersRepository)).get(MembersViewModel::class.java)
        membersViewModel.refreshTeachersList()
        membersViewModel._teachers.observe(requireActivity()){ value->
            value.let {
                Log.d(javaClass.name + " no. of teachers", it.size.toString())
                teachersAdapter.setData(it)
            }
        }
    }

    fun initializeViewModelForTeacher(){
        Log.d(javaClass.name, "initializeViewModel called")

        learnersAdapter = LearnersAdapter(requireContext())
        membersRecyclerView.adapter = learnersAdapter

        membersRepository = MembersRepository(requireContext()).getInstance()!!

        membersViewModel =
                ViewModelProvider(this, MembersViewModel.FACTORY(membersRepository)).get(MembersViewModel::class.java)
        membersViewModel.refreshLearnersList()
        membersViewModel._learners.observe(requireActivity()){ value->
            value.let {
                Log.d(javaClass.name + " no. of learners", it.size.toString())
                learnersAdapter.setData(it)
            }
        }
    }
}