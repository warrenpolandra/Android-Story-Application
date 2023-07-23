package com.dicoding.dicodingstoryapplication.view.liststory

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.dicodingstoryapplication.R
import com.dicoding.dicodingstoryapplication.adapter.LoadingStateAdapter
import com.dicoding.dicodingstoryapplication.adapter.StoryAdapter
import com.dicoding.dicodingstoryapplication.databinding.ActivityListStoryBinding
import com.dicoding.dicodingstoryapplication.model.UserPreference
import com.dicoding.dicodingstoryapplication.view.ViewModelFactory
import com.dicoding.dicodingstoryapplication.view.addstory.AddStoryActivity
import com.dicoding.dicodingstoryapplication.view.authentication.AuthenticationActivity
import com.dicoding.dicodingstoryapplication.view.detailstory.DetailStoryActivity
import com.dicoding.dicodingstoryapplication.view.map.MapsActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ListStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListStoryBinding
    private lateinit var listStoryViewModel: ListStoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupViewModel()
        setupAction()
    }

    // Create Menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }

    // Menu Select
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.logout -> {
                AlertDialog.Builder(this).apply {
                    setTitle("Logout Confirmation")
                    setMessage("Are you sure you want to log out?")
                    setPositiveButton("yes") { _, _ ->
                        listStoryViewModel.logout()
                        val intent = Intent(context, AuthenticationActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                    setNegativeButton("No") {_, _ ->
                        // Do Nothing
                    }
                    create()
                    show()
                }
            }
            R.id.map -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Overrides back button
    override fun onBackPressed() {
        AlertDialog.Builder(this).apply {
            setTitle("Confirm Exit")
            setMessage("Are you sure you want to exit from the app?")
            setPositiveButton("Yes") { _, _ ->
                super.onBackPressed()
                finishAffinity()
            }
            setNegativeButton("No") {_, _ ->
                // Do Nothing
            }
            create()
            show()
        }
    }

    // Basic View Setup
    private fun setupView() {
        val recyclerView = binding.rvListStory
        val layoutManager = LinearLayoutManager(this)
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(itemDecoration)

    }

    // ViewModel Setup
    private fun setupViewModel() {
        listStoryViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), this)
        )[ListStoryViewModel::class.java]

        listStoryViewModel.isLoading.observe(this) {
            showLoading(it)
        }
    }

    // Setup Button
    private fun setupAction() {
        setStoriesData()
        if (!allPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
        binding.fabAdd.setOnClickListener{ view ->
            if (view.id == R.id.fab_add) {
                val intent = Intent(this, AddStoryActivity::class.java)
                startActivity(intent)
            }
        }
    }

    // Set Stories Data With Paging
    private fun setStoriesData() {
        val adapter = StoryAdapter()
        binding.rvListStory.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )

        listStoryViewModel.getUser()?.observe(this) { user ->
            listStoryViewModel.setToken("Bearer " + user.token)
            listStoryViewModel.getAllStories().observe(this) {
                adapter.submitData(lifecycle, it)
            }
        }

        adapter.onItemClick = {
            val intent = Intent(this, DetailStoryActivity::class.java)
            intent.putExtra("name", it.name)
            intent.putExtra("url", it.photoUrl)
            intent.putExtra("description", it.description)
            intent.putExtra("date", it.createdAt.take(10))
            startActivity(intent)
        }
    }

    // Show loading status
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    // Permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionGranted()) {
                Toast.makeText(
                    this,
                    "Turn on all permission first, use precise location.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    // Check if permissions is granted
    private fun allPermissionGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // Companion object for permissions
    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}