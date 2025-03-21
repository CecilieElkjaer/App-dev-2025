/**
 * MIT License
 *
 * Copyright (c) 2025 Cecilie Amalie Wall Elkjær
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dk.itu.moapd.copenhagenbuzz.ceel.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.ui.navigateUp
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.copenhagenbuzz.ceel.R
import dk.itu.moapd.copenhagenbuzz.ceel.databinding.ActivityMainBinding

/**
 * The main entry point of the application. This activity initializes the user interface
 * and handles user interactions.
 *
 * @constructor Creates an instance of MainActivity.
 */
class MainActivity : AppCompatActivity() {

    // View binding for the activity layout
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var appBarConfiguration: AppBarConfiguration

    /**
     * Called when the activity is first created.
     * This method sets up the user interface.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        // Migrate from Kotlin synthetics to Jetpack view binding.
        mainBinding = ActivityMainBinding.inflate(layoutInflater)

        // Inflate the user interface into the current activity.
        setContentView(mainBinding.root)

        // Initialize Firebase Auth.
        auth = FirebaseAuth.getInstance()

        setupNavigation()
        updateNavigationHeader()
        controlMenuVisibility()
    }

    override fun onStart() {
        super.onStart()
        // Redirect the user to the LoginActivity if they are not logged in.
        auth.currentUser ?: startLoginActivity()
    }

    private fun startLoginActivity() {
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.let(::startActivity)
    }

    /**
     * Sets up the navigations for the app.
     */
    private fun setupNavigation(){
        //Search the view hierarchy and fragment for the `NavController` and return it to you.
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        navController.graph = navGraph

        val drawerLayout = mainBinding.drawerLayout
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)

        //set up the top app bar for landscape and portrait mode
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setSupportActionBar(mainBinding.topAppBar)
            setupActionBarWithNavController(navController, appBarConfiguration)
        } else if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            mainBinding.topAppBarLand?.setupWithNavController(navController)
        }

        //setup bottomnavigation for landscape and portrait mode
        mainBinding.bottomNavigation?.setupWithNavController(navController)
        mainBinding.bottomNavigationRail?.setupWithNavController(navController)

        //setup NavigationView/Drawer
        mainBinding.navigationView.setupWithNavController(navController)
    }

    private fun updateNavigationHeader(){
        //Setup of NavigationView and populate the Header in the NavigationView dynamically.
        val headerView = mainBinding.navigationView?.getHeaderView(0)
        val profileImage = headerView?.findViewById<ImageView>(R.id.profile_image)
        val profileName = headerView?.findViewById<TextView>(R.id.profile_name)
        val profileEmail = headerView?.findViewById<TextView>(R.id.profile_email)
        val authButton = headerView?.findViewById<Button>(R.id.header_auth_button)
        val user = auth.currentUser

        if (user != null && !user.isAnonymous) {
            profileName?.text = user.displayName ?: "User"
            profileEmail?.text = user.email ?: "No Email"
            profileImage?.setImageResource(R.drawable.baseline_account_circle_24)
            authButton?.text = "Sign Out"

            authButton?.setOnClickListener {
                auth.signOut()
                startLoginActivity()
            }
        } else {
            profileName?.text = "Guest Account"
            profileEmail?.text = "guest@email.com"
            profileImage?.setImageResource(R.drawable.baseline_account_circle_24)
            authButton?.text = "Sign In"

            authButton?.setOnClickListener {
                startLoginActivity()
            }
        }
    }

    /*
        Sets what is needed to be shown for each user. A guest user should not be able to add events.
        Therefore the fragment add event, account and event history will be hidden.
     */
    private fun controlMenuVisibility(){
        val navMenu = mainBinding.navigationView.menu
        val isLoggedIn = auth.currentUser != null && !auth.currentUser!!.isAnonymous

        navMenu.findItem(R.id.nav_add_event)?.isVisible = isLoggedIn
        navMenu.findItem(R.id.nav_account)?.isVisible = isLoggedIn
        navMenu.findItem(R.id.nav_event_history)?.isVisible = isLoggedIn

        val bottomNavigation = mainBinding.bottomNavigation?.menu
        bottomNavigation?.findItem(R.id.fragment_add_event)?.isVisible = isLoggedIn
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = (supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment).navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    /*
    /**
     * Creates the options menu in the top app bar.
     * This menu includes login/logout options, which change visibility
     * based on the user's authentication status.
     *
     * @param menu The options menu in which items are placed.
     * @return True if the menu is successfully created.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)

        menu?.findItem(R.id.menu_login)?.isVisible = !isLoggedIn
        menu?.findItem(R.id.menu_logout)?.isVisible = isLoggedIn
        return true
    }

     */
}