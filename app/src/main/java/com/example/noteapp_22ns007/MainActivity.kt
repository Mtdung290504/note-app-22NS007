package com.example.noteapp_22ns007

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.noteapp_22ns007.fragments.LabelFragment
import com.example.noteapp_22ns007.fragments.MainFragment
import com.example.noteapp_22ns007.databinding.ActivityMainBinding
import com.example.noteapp_22ns007.fragments.EditNoteFragment
import com.example.noteapp_22ns007.fragments.ManageLabelFragment
import com.example.noteapp_22ns007.model.database.AppDatabase
import com.example.noteapp_22ns007.model.database.daos.LabelDao
import com.example.noteapp_22ns007.model.database.daos.NoteDao
import com.example.noteapp_22ns007.model.viewmodels.LabelViewModel
import com.example.noteapp_22ns007.model.viewmodels.NoteViewModel

class MainActivity: AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // View elements
    private lateinit var mainFragment: Fragment
    private lateinit var labelFragment: LabelFragment
    lateinit var manageLabelFragment: ManageLabelFragment
    private lateinit var searchBar: EditText
    private lateinit var cardView: CardView

    // View models and dao
    lateinit var noteViewModel: NoteViewModel
    lateinit var labelViewModel: LabelViewModel
    private lateinit var noteDao: NoteDao
    private lateinit var labelDao: LabelDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        config()
        initializeViewModelsAndDao()
        initializeViewElements(binding)

        if (savedInstanceState == null) {
            mainFragment = MainFragment()
            labelFragment = LabelFragment()
            manageLabelFragment = ManageLabelFragment()

            supportFragmentManager.beginTransaction()
                .add(binding.fragmentContainer.id, mainFragment, "MainFragment")
                .add(binding.fragmentContainer.id, labelFragment, "LabelFragment")
                .add(binding.bigFragmentContainer.id, manageLabelFragment, "ManageLabelFragment")
                .hide(manageLabelFragment)
                .hide(labelFragment)
                .commit()
        } else {
            mainFragment = supportFragmentManager.findFragmentByTag("MainFragment") as MainFragment
            labelFragment = supportFragmentManager.findFragmentByTag("LabelFragment") as LabelFragment
            manageLabelFragment = supportFragmentManager.findFragmentByTag("ManageLabelFragment") as ManageLabelFragment
        }
    }

    private fun config() {
        supportActionBar?.hide() // Hide Action Bar
        window.statusBarColor = Color.rgb(0, 0,0) // Set color for status bar
    }

    private fun initializeViewModelsAndDao() {
        noteDao = AppDatabase.getDatabase(application).noteDao()
        labelDao = AppDatabase.getDatabase(application).labelDao()

        val noteViewModelFactory = NoteViewModel.NoteViewModelFactory(noteDao)
        val labelViewModelFactory = LabelViewModel.LabelViewModelFactory(labelDao)
        noteViewModel = ViewModelProvider(this, noteViewModelFactory)[NoteViewModel::class.java]
        labelViewModel = ViewModelProvider(this, labelViewModelFactory)[LabelViewModel::class.java]

        noteViewModel.deleteEmptyNotes()
    }

    private fun initializeViewElements(binding: ActivityMainBinding) {
        searchBar = binding.searchBar
        cardView = binding.bigFragmentContainer
//        mainFragment = MainFragment()
//        labelFragment = LabelFragment()

        searchBar.setOnFocusChangeListener { _, hasFocus ->
            val valueToSearch: String = searchBar.text.toString()

            if (hasFocus) {
                // changeFragment(labelFragment, AnimationType.SLIDE_IN_TOP, "LabelFragment")
                if(valueToSearch.isBlank())
                    displayLabelFragment()
                else
                    (mainFragment as MainFragment).filterNotes(valueToSearch)
            } else {
                // changeFragment(mainFragment, AnimationType.SLIDE_IN_TOP, "MainFragment")
                displayMainFragment()
                (mainFragment as MainFragment).filterNotes(valueToSearch)
            }
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if(query.isEmpty()) {
                    // changeFragment(labelFragment, AnimationType.SLIDE_IN_TOP, "LabelFragment")
                    displayLabelFragment()
                    (mainFragment as MainFragment).filterNotes(s.toString())
                }
                else {
                    // changeFragment(mainFragment, AnimationType.SLIDE_IN_TOP, "MainFragment")
                    displayMainFragment()
                    (mainFragment as MainFragment).filterNotes(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val editingFragment = supportFragmentManager.findFragmentById(binding.bigFragmentContainer.id)

        if(editingFragment is EditNoteFragment
            || editingFragment is ManageLabelFragment && manageLabelFragment.isVisibleInActivity()) {
            val searchValue = binding.searchBar.text.toString()

            if (editingFragment is EditNoteFragment) {
                editingFragment.saveDataAndHide()
                (mainFragment as MainFragment).filterNotes(searchValue)
            } else {
                hideManageLabelFragment()
                (mainFragment as MainFragment).filterNotes(searchValue)
            }
        } else {
            if (binding.searchBar.isFocused) {
                // Hide the keyboard
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)

                // Clear focus from searchBar
                binding.searchBar.clearFocus()
                return
            }

            if (mainFragment.isVisibleInActivity()) {
                finish()
            } else if (labelFragment.isVisibleInActivity()) {
                displayMainFragment()
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun Fragment.isVisibleInActivity(): Boolean {
        return isVisible && activity != null && (view?.visibility == View.VISIBLE)
    }

    fun hideKeyBoardForSearchBar() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)
    }

    fun displayEditNoteFragment(f: EditNoteFragment, animationType: AnimationType? = AnimationType.SLIDE_IN_RIGHT) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        cardView.isClickable = true

        if(animationType == AnimationType.SLIDE_IN_RIGHT)
            fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_right, R.anim.slide_out_left
            )
        else if(animationType == AnimationType.FADE)
            fragmentTransaction.setCustomAnimations(
                R.anim.fade_in, R.anim.fade_out
            )

        fragmentTransaction.replace(binding.bigFragmentContainer.id, f, "EditNoteFragment")
        fragmentTransaction.commit()
    }
    fun hideEditNoteFragment(f: EditNoteFragment) {
        cardView.isClickable = false
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
            .remove(f)
            .commit()
    }

    fun displayManageLabelFragment() {
        cardView.isClickable = true
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top)
//            .replace(binding.bigFragmentContainer.id, f, "ManageLabelFragment")
            .show(manageLabelFragment)
            .commit()
    }
    fun hideManageLabelFragment() {
        cardView.isClickable = false
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom)
//            .remove(f)
            .hide(manageLabelFragment)
            .commit()
    }

    fun displayMainFragment() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top)
            .show(mainFragment)
            .hide(labelFragment)
            .commit()
    }
    fun displayLabelFragment() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom)
            .show(labelFragment)
            .hide(mainFragment)
            .commit()
    }

    /*
    fun changeFragment(f: Fragment, animationType: AnimationType, tag: String, callback: (() -> Unit)? = null) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // Find the current fragment
        val currentFragment = fragmentManager.findFragmentById(binding.fragmentContainer.id)

        // Logic to decide animation based on currentFragment and new fragment
        val actualAnimationType = when {
            currentFragment is MainFragment && f is LabelFragment -> AnimationType.SLIDE_IN_TOP
            currentFragment is LabelFragment && f is MainFragment -> AnimationType.SLIDE_IN_BOTTOM
            currentFragment is EditNoteFragment && f is MainFragment -> AnimationType.SLIDE_IN_RIGHT
            else -> animationType // Default animation
        }

        // Add custom animations based on the type
        when (actualAnimationType) {
            AnimationType.SLIDE_IN_TOP -> fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_top, R.anim.slide_out_bottom
            )
            AnimationType.SLIDE_IN_BOTTOM -> fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_bottom, R.anim.slide_out_top
            )
            AnimationType.SLIDE_IN_RIGHT -> fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_right, R.anim.slide_out_left
            )
            AnimationType.SLIDE_IN_LEFT -> fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_left, R.anim.slide_out_right
            )
            AnimationType.FADE -> fragmentTransaction.setCustomAnimations(
                R.anim.fade_in, R.anim.fade_out
            )
            AnimationType.NONE -> {} // No animation
        }

        fragmentTransaction.replace(binding.fragmentContainer.id, f, tag)

        // Add to back stack only if the new fragment is not MainFragment
        if (f !is MainFragment) {
            fragmentTransaction.addToBackStack(tag)
        }
        if(callback != null) {
            callback()
        }

        fragmentTransaction.commit()
    }*/
    /*
    fun changeFragment(f: Fragment, animationType: AnimationType, tag: String, callback: (() -> Unit)? = null) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // Find the current fragment
        val currentFragment = fragmentManager.findFragmentById(binding.fragmentContainer.id)

        // Logic to decide animation based on currentFragment and new fragment
        val actualAnimationType = when {
            currentFragment is MainFragment && f is LabelFragment -> AnimationType.SLIDE_IN_TOP
            currentFragment is LabelFragment && f is MainFragment -> AnimationType.SLIDE_IN_BOTTOM
            currentFragment is EditNoteFragment && f is MainFragment -> AnimationType.SLIDE_IN_RIGHT
            else -> animationType // Default animation
        }

        // Add custom animations based on the type
        when (actualAnimationType) {
            AnimationType.SLIDE_IN_TOP -> fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_top, R.anim.slide_out_bottom
            )
            AnimationType.SLIDE_IN_BOTTOM -> fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_bottom, R.anim.slide_out_top
            )
            AnimationType.SLIDE_IN_RIGHT -> fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_right, R.anim.slide_out_left
            )
            AnimationType.SLIDE_IN_LEFT -> fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_left, R.anim.slide_out_right
            )
            AnimationType.FADE -> fragmentTransaction.setCustomAnimations(
                R.anim.fade_in, R.anim.fade_out
            )
            AnimationType.NONE -> {} // No animation
        }

        // Check if the fragment is already added
        val fragmentInManager = fragmentManager.findFragmentByTag(tag)
        if (fragmentInManager != null) {
            // Hide the current fragment
            currentFragment?.let {
                fragmentTransaction.hide(it)
            }
            // Show the new fragment
            fragmentTransaction.show(fragmentInManager)
        } else {
            // Hide the current fragment
            currentFragment?.let {
                fragmentTransaction.hide(it)
            }
            // Add the new fragment
            fragmentTransaction.add(binding.fragmentContainer.id, f, tag)
        }

        // Add to back stack only if the new fragment is not MainFragment
        if (f !is MainFragment) {
            fragmentTransaction.addToBackStack("MainFragment")
        }

        if (callback != null) {
            callback()
        }

        fragmentTransaction.commit()
    }
    */
}

enum class AnimationType {
    SLIDE_IN_TOP,
    SLIDE_IN_BOTTOM,
    SLIDE_IN_RIGHT,
    SLIDE_IN_LEFT,
    FADE,
    NONE
}