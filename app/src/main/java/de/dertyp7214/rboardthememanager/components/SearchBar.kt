package de.dertyp7214.rboardthememanager.components

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import de.dertyp7214.rboardthememanager.R


class SearchBar(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var focus = false
    private var searchListener: (text: String) -> Unit = {}
    private var closeListener: () -> Unit = {}
    private var focusListener: () -> Unit = {}

    init {
        inflate(context, R.layout.search_bar, this)

        val searchBar = findViewById<CardView>(R.id.search_bar)

        val searchButton = findViewById<ImageButton>(R.id.search_button)
        val backButton = findViewById<ImageButton>(R.id.back_button)
        val moreButton = findViewById<ImageButton>(R.id.more_button)

        val searchText = findViewById<TextView>(R.id.search_text)
        val searchEdit = findViewById<EditText>(R.id.search)

        searchBar.setOnClickListener {
            if (!focus) {
                focus = true
                searchButton.visibility = GONE
                backButton.visibility = VISIBLE

                searchText.visibility = GONE
                searchEdit.visibility = VISIBLE

                searchEdit.requestFocus()
                val imm: InputMethodManager =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(searchEdit, InputMethodManager.SHOW_IMPLICIT)
                focusListener()
            }
        }

        backButton.setOnClickListener {
            if (focus) {
                focus = false
                searchButton.visibility = VISIBLE
                backButton.visibility = GONE

                searchText.visibility = VISIBLE
                searchEdit.visibility = GONE

                searchEdit.setText("")
                clearFocus(searchEdit)
                closeListener()
            }
        }

        moreButton.setOnClickListener {

        }

        searchEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                clearFocus(searchEdit)
                searchListener(searchEdit.text.toString())
                true
            } else false
        }
    }

    fun setOnSearchListener(listener: (text: String) -> Unit) {
        searchListener = listener
    }

    fun setOnCloseListener(listener: () -> Unit) {
        closeListener = listener
    }

    fun setOnFocusListener(listener: () -> Unit) {
        focusListener = listener
    }

    private fun clearFocus(editText: EditText) {
        editText.clearFocus()
        val imm: InputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }
}