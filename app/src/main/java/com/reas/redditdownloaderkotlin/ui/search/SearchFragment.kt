package com.reas.redditdownloaderkotlin.ui.search

import android.app.Activity
import android.app.SearchManager
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import com.reas.redditdownloaderkotlin.databinding.FragmentSearchBinding
import com.reas.redditdownloaderkotlin.util.search.SearchRecentProvider

private const val TAG = "SearchFragment"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val view = binding.root

        with(binding.searchInput) searchView@{
            val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

            suggestionsAdapter = SearchRecentAdapter(requireContext(), null, 0)

            setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    SearchRecentSuggestions(requireContext(), SearchRecentProvider.AUTHORITY, SearchRecentProvider.MODE)
                        .saveRecentQuery(query, null)

                    view.hideKeyboard()

                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    val cursor = getRecentSuggestions(newText)
                    suggestionsAdapter.swapCursor(cursor)
                    return false
                }
            })

            setOnSuggestionListener(object: SearchView.OnSuggestionListener {
                override fun onSuggestionSelect(position: Int): Boolean {
                    return false
                }

                override fun onSuggestionClick(position: Int): Boolean {
                    this@searchView.setQuery((suggestionsAdapter as SearchRecentAdapter).getSuggestionText(position), true)
                    return true
                }

            })

        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getRecentSuggestions(newText: String?): Cursor? {
        val uri = Uri.Builder().run {
            scheme(ContentResolver.SCHEME_CONTENT)
            authority(SearchRecentProvider.AUTHORITY)

            appendPath(SearchManager.SUGGEST_URI_PATH_QUERY)
            build()
        }

        val selection = " ?"
        val selArgs = arrayOf(newText)

        return requireContext().contentResolver.query(uri, null, selection, selArgs, null)

    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        val instance = SearchFragment()
    }
}