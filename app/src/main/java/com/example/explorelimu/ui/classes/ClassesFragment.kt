package com.example.explorelimu.ui.classes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.explorelimu.R
import com.example.explorelimu.data.model.Model
import com.example.explorelimu.data.session.SessionsAdapter
import com.example.explorelimu.data.session.SessionsRepository
import com.example.explorelimu.ui.members.MembersViewModel
import com.example.explorelimu.util.STUDENT
import com.example.explorelimu.util.TEACHER
import com.example.explorelimu.util.USER_TYPE

class ClassesFragment : Fragment() {

    private lateinit var sessionsRecyclerView: RecyclerView
    private lateinit var sessionsAdapter: SessionsAdapter

    private lateinit var sessionsRepository: SessionsRepository
    private lateinit var classesViewModel: ClassesViewModel
    var mStackLevel = 0
    val DIALOG_FRAGMENT = 1

    val SESSION_NAME = "session_name"
    val MODEL = "model"
    val MODEL_ID = "model_id"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sessionsRepository = SessionsRepository(requireContext()).getInstance()!!
        classesViewModel =
            ViewModelProvider(this, ClassesViewModel.FACTORY(sessionsRepository)).get(
                ClassesViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_classes, container, false)
        val textView: TextView = root.findViewById(R.id.text_sessions)
        val progressBar: ProgressBar = root.findViewById(R.id.loading_pb)
        sessionsRecyclerView = root.findViewById(R.id.session_list_rv)
        sessionsAdapter = SessionsAdapter(requireContext())
        sessionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        sessionsRecyclerView.adapter = sessionsAdapter

        classesViewModel.sessions.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()){
                textView.visibility = View.GONE
                sessionsRecyclerView.visibility = View.VISIBLE
                sessionsAdapter.setData(it)
            } else {
                textView.visibility = View.VISIBLE
                sessionsRecyclerView.visibility = View.GONE
            }
        })

        classesViewModel.loading.observe(viewLifecycleOwner, {
            if (it) progressBar.visibility = View.VISIBLE else progressBar.visibility = View.GONE
        })

//        classesViewModel.noSessions.observe(viewLifecycleOwner, {
//            if (it) {
//                textView.visibility = View.VISIBLE
//            } else {
//                textView.visibility = View.GONE
//            }
//        })

        classesViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        classesViewModel.refreshSessionsList()

        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(
                requireContext()
        )

        if (prefs.getString(USER_TYPE, STUDENT) == TEACHER) setHasOptionsMenu(true)

        return root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.classes_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_class -> {
                showDialog(DIALOG_FRAGMENT)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mStackLevel = savedInstanceState.getInt("level");
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("level", mStackLevel)
    }

    fun showDialog(type: Int) {
        mStackLevel++
        val ft: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        val prev: Fragment? = requireActivity().supportFragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        when (type) {
            DIALOG_FRAGMENT -> {
                val dialogFrag: DialogFragment = AddSessionDialog()
                dialogFrag.setTargetFragment(this, DIALOG_FRAGMENT)
                dialogFrag.show(parentFragmentManager.beginTransaction(), "dialog")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            DIALOG_FRAGMENT -> if (resultCode == Activity.RESULT_OK) {
                classesViewModel.addSession(data!!.getStringExtra(SESSION_NAME)!!,
                    data.extras!!.getParcelable(MODEL)!!
                )
            }
        }
    }
}