package com.example.explorelimu

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.explorelimu.render.ModelActivity
import com.example.explorelimu.ui.members.MembersFragment
import com.example.explorelimu.util.USER_TYPE
import com.example.explorelimu.util.launchModelRendererActivity
import com.example.explorelimu.xmpp.RoosterConnection
import com.example.explorelimu.xmpp.RoosterConnectionService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import org.andresoviedo.util.android.AndroidURLStreamHandlerFactory
import org.andresoviedo.util.android.AndroidUtils
import org.andresoviedo.util.android.ContentUtils
import org.andresoviedo.util.android.FileUtils
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smackx.iqregister.AccountManager
import java.io.File
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity() {
    private var boundService: RoosterConnectionService? = null

    //boolean variable to keep a check on service bind and unbind event
    var isBound = false

    lateinit var mConnection: XMPPTCPConnection
    internal lateinit var mRoosterConnection: RoosterConnection

    val context = this

    lateinit var boundServiceConnection: ServiceConnection

    lateinit var outputDirectory: File

    private val REQUEST_READ_EXTERNAL_STORAGE = 1000
    private val SUPPORTED_FILE_TYPES_REGEX = "(?i).*\\.(obj|stl|dae)"
    private val loadModelParameters: Map<String, Any> = HashMap()

    private lateinit var addModelFloatingActionButton: ExtendedFloatingActionButton
    private lateinit var navHost: Fragment
    private lateinit var firstFrag: Fragment
    var userType: String? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(System.getProperty("java.protocol.handler.pkgs") == null){
            System.setProperty("java.protocol.handler.pkgs", "org.andresoviedo.util.android")
            URL.setURLStreamHandlerFactory(AndroidURLStreamHandlerFactory())
        }

        setContentView(R.layout.activity_main)

        addModelFloatingActionButton = findViewById(R.id.add_model_fab)

        boundServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val binderBridge = service as RoosterConnectionService.MyBinder
                boundService = binderBridge.service
                isBound = true

                mConnection = boundService?.getmConnection()!!

                if (mConnection == null) {
                    Log.d(localClassName, "mConnection is null")
                }
                Log.d(localClassName, "We got here")
                mRoosterConnection = boundService!!.getmRoosterConnection()
                if (firstFrag is MembersFragment){
                    val userType = AccountManager.getInstance(mConnection).getAccountAttribute("name").substringAfterLast("101").substringBeforeLast("404")
                    Log.d("$localClassName user type", userType)
                    if (userType == "student"){
                        (firstFrag as MembersFragment).initializeViewModelForLearner()
                    } else
                        (firstFrag as MembersFragment).initializeViewModelForTeacher()

                    val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(
                            this@MainActivity
                    )
                    prefs.edit().putString(USER_TYPE, userType).apply()
                } else
                    Log.d(localClassName, "currentFragment is NOT MembersFragment")
            }

            override fun onServiceDisconnected(name: ComponentName) {
                isBound = false
                boundService = null
            }
        }


        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
                setOf(
                        R.id.navigation_members, R.id.navigation_categories, R.id.navigation_classes
                )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!
        firstFrag = navHost.childFragmentManager.fragments[0]

        outputDirectory = getMyOutputDirectory()
        addModelFloatingActionButton.setOnClickListener { loadModelFromSdCard() }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, RoosterConnectionService::class.java).also { intent ->
            bindService(intent, boundServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(boundServiceConnection)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getMyOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun loadModelFromSdCard() {
        Log.d(localClassName, "loadModelFromSdCard function called")
        // check permission starting from android API 23 - Marshmallow
        if (AndroidUtils.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_EXTERNAL_STORAGE)) {
            FileUtils.createChooserDialog(this, "Select file", null, null, SUPPORTED_FILE_TYPES_REGEX
            ) { file: File? ->
                if (file != null) {
                    ContentUtils.setCurrentDir(file.parentFile)
                    Log.d("$localClassName uri", file.absolutePath)
                    launchModelRendererActivity(this, Uri.parse("file://" + file.absolutePath),)
                }
            }
        }
    }
}