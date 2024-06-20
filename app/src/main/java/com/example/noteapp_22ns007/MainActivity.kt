package com.example.noteapp_22ns007

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.noteapp_22ns007.fragments.LabelFragment
import com.example.noteapp_22ns007.fragments.MainFragment
import com.example.noteapp_22ns007.databinding.ActivityMainBinding
import com.example.noteapp_22ns007.fragments.EditNoteFragment
import com.example.noteapp_22ns007.fragments.ManageLabelFragment
import com.example.noteapp_22ns007.model.database.AppDatabase
import com.example.noteapp_22ns007.model.database.daos.ImageDao
import com.example.noteapp_22ns007.model.database.daos.LabelDao
import com.example.noteapp_22ns007.model.database.daos.NoteDao
import com.example.noteapp_22ns007.model.viewmodels.ImageViewModel
import com.example.noteapp_22ns007.model.viewmodels.LabelViewModel
import com.example.noteapp_22ns007.model.viewmodels.NoteViewModel
import com.example.noteapp_22ns007.model.viewmodels.SearchViewModel
import com.google.android.material.navigation.NavigationView

class MainActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding

    // View elements
    private lateinit var mainFragment: MainFragment
    private lateinit var labelFragment: LabelFragment
    private lateinit var manageLabelFragment: ManageLabelFragment
    private lateinit var searchBar: EditText
    private lateinit var cardView: CardView

    // View models
    lateinit var noteViewModel: NoteViewModel
    lateinit var labelViewModel: LabelViewModel
    lateinit var imageViewModel: ImageViewModel
    lateinit var searchViewModel: SearchViewModel

    // Database
    private lateinit var appDatabase: AppDatabase
    private lateinit var noteDao: NoteDao
    private lateinit var labelDao: LabelDao
    private lateinit var imageDao: ImageDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        config()
        initializeViewModelsAndDb()
        initializeViewElements(binding)
        initializeFragments(savedInstanceState)
        initializeNav()
    }

    private fun config() {
        supportActionBar?.hide() // Hide Action Bar
        window.statusBarColor = Color.rgb(0, 0,0) // Set color for status bar
    }

    private fun initializeViewModelsAndDb() {
        appDatabase = AppDatabase.getDatabase(application)
        noteDao = appDatabase.noteDao()
        labelDao = appDatabase.labelDao()
        imageDao = appDatabase.imageDao()

        val noteViewModelFactory = NoteViewModel.NoteViewModelFactory(noteDao)
        val labelViewModelFactory = LabelViewModel.LabelViewModelFactory(labelDao)
        val imageViewModelFactory = ImageViewModel.ImageViewModelFactory(imageDao)

        noteViewModel = ViewModelProvider(this, noteViewModelFactory)[NoteViewModel::class.java]
        labelViewModel = ViewModelProvider(this, labelViewModelFactory)[LabelViewModel::class.java]
        imageViewModel = ViewModelProvider(this, imageViewModelFactory)[ImageViewModel::class.java]
        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        noteViewModel.deleteEmptyNotes()
    }

    private fun initializeViewElements(binding: ActivityMainBinding) {
        searchBar = binding.searchBar
        cardView = binding.bigFragmentContainer

        searchBar.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus && searchBar.text.toString().isBlank()) {
                displayLabelFragment()
            }
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()

                if(query.isBlank()) {
                    displayLabelFragment()
                }

                displayMainFragment()
                searchViewModel.setSearchQuery(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun initializeFragments(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            mainFragment = MainFragment()
            labelFragment = LabelFragment()

            supportFragmentManager.beginTransaction()
                .add(binding.fragmentContainer.id, mainFragment, "MainFragment")
                .add(binding.fragmentContainer.id, labelFragment, "LabelFragment")
                .hide(labelFragment)
                .commit()
        } else {
            mainFragment = supportFragmentManager.findFragmentByTag("MainFragment") as MainFragment
            labelFragment = supportFragmentManager.findFragmentByTag("LabelFragment") as LabelFragment

            supportFragmentManager.beginTransaction()
                .hide(labelFragment)
                .show(mainFragment)
                .commit()
        }

        manageLabelFragment = ManageLabelFragment()
    }

    private fun initializeNav() {
        binding.navView.setNavigationItemSelectedListener(this)
        binding.btnOpenNav.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val editingFragment = supportFragmentManager.findFragmentById(binding.bigFragmentContainer.id)

        if(editingFragment is EditNoteFragment) {
            editingFragment.saveDataAndHide()
        } else if(editingFragment is ManageLabelFragment) {
            if(editingFragment.clearFocus()) return
            hideManageLabelFragment()
        } else {
            if (binding.searchBar.isFocused) {
                binding.searchBar.text = null
                binding.searchBar.clearFocus()
                displayMainFragment()
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
            .hide(labelFragment)
            .show(mainFragment)
            .commit()
    }

    fun displayManageLabelFragment() {
        cardView.isClickable = true
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top)
            .replace(binding.bigFragmentContainer.id, manageLabelFragment, "ManageLabelFragment")
            .commit()
    }
    fun hideManageLabelFragment() {
        cardView.isClickable = false
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom)
            .remove(manageLabelFragment)
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_trash -> {
                // Handle the home action
            }
            R.id.nav_archive -> {
                // Handle the gallery action
            }
            R.id.nav_label -> {
                displayManageLabelFragment()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    @SuppressLint("SetTextI18n")
    fun searchLabel(labelName: String) {
        val editingFragment = supportFragmentManager.findFragmentById(binding.bigFragmentContainer.id)

        if(editingFragment is EditNoteFragment) {
            hideEditNoteFragment(editingFragment)
            searchBar.setText("$:$labelName")
            searchBar.requestFocus()
        }

        if(labelFragment.isVisibleInActivity()) {
            displayMainFragment()
            searchBar.setText("$:$labelName")
        }
    }
}

enum class AnimationType {
    SLIDE_IN_TOP,
    SLIDE_IN_BOTTOM,
    SLIDE_IN_RIGHT,
    SLIDE_IN_LEFT,
    FADE,
    NONE
}