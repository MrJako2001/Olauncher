package app.olauncher

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_app.*


class AppListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = activity?.run {
            ViewModelProvider(this).get(MainViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        val onAppClicked: (appModel: AppModel) -> Unit = { appModel ->
            Toast.makeText(requireContext(), appModel.appLabel, Toast.LENGTH_SHORT).show()
        }

        val appAdapter = AppListAdapter(getAppsList(requireContext()), onAppClicked)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appAdapter
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            var isKeyboardDismissedByScroll = false

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        if (!isKeyboardDismissedByScroll) {
                            search.hideKeyboard()
                            isKeyboardDismissedByScroll = !isKeyboardDismissedByScroll
                        }
                    }
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        isKeyboardDismissedByScroll = false
                        if (!recyclerView.canScrollVertically(-1)) {
                            search.requestFocus()
                            search.showKeyboard()
                        }
                    }
                }
            }
        })

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                appAdapter.filter.filter(newText)
                return false
            }
        })
    }

    override fun onStart() {
        super.onStart()
        search.requestFocus()
        search.showKeyboard()
    }

    override fun onStop() {
        super.onStop()
        search.clearFocus()
        search.hideKeyboard()
        activity?.onBackPressed()
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }
}