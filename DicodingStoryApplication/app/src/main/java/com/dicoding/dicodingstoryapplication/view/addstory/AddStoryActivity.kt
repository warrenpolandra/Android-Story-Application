package com.dicoding.dicodingstoryapplication.view.addstory

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.dicoding.dicodingstoryapplication.databinding.ActivityAddStoryBinding
import com.dicoding.dicodingstoryapplication.model.UserPreference
import com.dicoding.dicodingstoryapplication.view.ViewModelFactory
import com.dicoding.dicodingstoryapplication.view.liststory.ListStoryActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var addStoryViewModel: AddStoryViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Image variables
    private lateinit var currentPhotoPath: String
    private var getFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupViewModel()
        setupAction()
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
                    "Turn on the permission first.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    // Launch Gallery Intent
    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            selectedImg.let { uri ->
                val myFile = uriToFile(uri, this@AddStoryActivity)
                getFile = myFile
                rotateFile(myFile)
                binding.ivImagePreview.setImageURI(uri)
            }
        }
    }

    // Launch Camera Intent
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val newFile = File(currentPhotoPath)
            newFile.let { file ->
                getFile = file
                rotateFile(file)
                binding.ivImagePreview.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
        }
    }

    // Override back menu button
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // Basic view setup
    private fun setupView() {
        val actionBar = supportActionBar
        actionBar?.title = "Upload a New Story"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // ViewModel setup
    private fun setupViewModel() {
        addStoryViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), this)
        )[AddStoryViewModel::class.java]

        addStoryViewModel.isLoading.observe(this) {
            showLoading(it)
        }
        addStoryViewModel.finishActivity.observe(this) {
            if (it == true) {
                val intent = Intent(this, ListStoryActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    // Button & permission setup
    private fun setupAction() {
        if (!allPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        // Camera & gallery action
        binding.btnCamera.setOnClickListener { startCamera() }
        binding.btnGallery.setOnClickListener{ startGallery() }

        // Get current location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        addStoryViewModel.getLocation(this, fusedLocationClient)
        addStoryViewModel.location.observe(this) { location ->
            binding.buttonAdd.setOnClickListener {
                val desc = binding.edAddDescription
                desc.error = if (desc.text.toString().isEmpty()) "Enter the description" else null

                if (desc.error == null) uploadStory(location.latitude.toFloat(), location.longitude.toFloat())
            }
        }
    }

    // Start Camera
    private fun startCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        createTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@AddStoryActivity,
                "User",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    // Open Gallery
    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a picture")
        launcherIntentGallery.launch(chooser)
    }

    // Upload Story
    private fun uploadStory(lat: Float, lon: Float) {
        if (getFile != null) {
            val file = reduceFileImage(getFile as File)
            val description = binding.edAddDescription.text.toString()
            val requestImageFile = file.asRequestBody("image/jpg".toMediaType())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )
            addStoryViewModel.getUser().observe(this) { user ->
                val token = "Bearer " + user.token
                if (binding.cbLocation.isChecked){
                    addStoryViewModel.uploadStory(token, this, imageMultipart, description, lat, lon)
                } else
                    addStoryViewModel.uploadStory(token, this, imageMultipart, description, null, null)
            }
        } else {
            Toast.makeText(this, "Choose an image first!", Toast.LENGTH_SHORT).show()
        }
    }

    // Check if permissions is granted
    private fun allPermissionGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // Show loading status
    private fun showLoading(isLoading: Boolean) {
        val progressBar = binding.progressBar
        val addButton = binding.buttonAdd
        val cameraButton = binding.btnCamera
        val galleryButton = binding.btnGallery
        val descLayout = binding.edAddDescriptionLayout

        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            addButton.isEnabled = false
            cameraButton.isEnabled = false
            galleryButton.isEnabled = false
            descLayout.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            addButton.isEnabled = true
            cameraButton.isEnabled = true
            galleryButton.isEnabled = true
            descLayout.isEnabled = true
        }
    }

    // Companion object for permissions
    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}