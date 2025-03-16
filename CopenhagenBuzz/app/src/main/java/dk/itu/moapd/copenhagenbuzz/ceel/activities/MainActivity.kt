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
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
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
    private var isLoggedIn = false

    /**
     * Called when the activity is first created.
     * This method sets up the user interface.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        initializeViews()
    }

    /**
     * Initializes the user interface and sets up event listeners.
     */
    private fun initializeViews(){
        // Migrate from Kotlin synthetics to Jetpack view binding.
        mainBinding = ActivityMainBinding.inflate(layoutInflater)

        // Inflate the user interface into the current activity.
        setContentView(mainBinding.root)

        //get login status from intent
        isLoggedIn = intent.getBooleanExtra("isLoggedIn", false)



        //Search the view hierarchy and fragment for the `NavController` and return it to you.
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        navController.graph = navGraph

        //set up the top app bar for landscape and portrait mode
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setSupportActionBar(mainBinding.topAppBar)
            val appBarConfiguration = AppBarConfiguration(navController.graph)
            setupActionBarWithNavController(navController, appBarConfiguration)
        } else if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            mainBinding.topAppBarLand?.setupWithNavController(navController)
        }

        //setup bottomnavigation for landscape and portrait mode
        mainBinding.bottomNavigation?.setupWithNavController(navController)
        mainBinding.bottomNavigationRail?.setupWithNavController(navController)
    }

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

    /**
     * Handles selections from the options menu.
     * This method is used to navigate between fragments.
     *
     * @param item The selected menu item.
     * @return True if the item is handled, otherwise calls the superclass implementation.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
        val navController = navHostFragment.navController
        return when (item.itemId) {
            R.id.menu_login -> {
                // redirect to LoginActivity when logging out.
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }

            R.id.menu_logout -> {
                // redirect to LoginActivity when logging out.
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}