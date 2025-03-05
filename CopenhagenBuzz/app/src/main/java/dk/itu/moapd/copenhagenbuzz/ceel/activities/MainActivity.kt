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
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dk.itu.moapd.copenhagenbuzz.ceel.R
import dk.itu.moapd.copenhagenbuzz.ceel.databinding.ActivityMainBinding
import dk.itu.moapd.copenhagenbuzz.ceel.fragments.AddEventFragment
import dk.itu.moapd.copenhagenbuzz.ceel.fragments.CalendarFragment
import dk.itu.moapd.copenhagenbuzz.ceel.fragments.FavoritesFragment
import dk.itu.moapd.copenhagenbuzz.ceel.fragments.MapsFragment
import dk.itu.moapd.copenhagenbuzz.ceel.fragments.TimelineFragment

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

        //set up the top app bar
        setSupportActionBar(mainBinding.topAppBar)

        //Search the view hierarchy and fragment for the `NavController` and return it to you.
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
        val navController = navHostFragment.navController
        mainBinding.bottomNavigation.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)

        menu?.findItem(R.id.menu_login)?.isVisible = !isLoggedIn
        menu?.findItem(R.id.menu_logout)?.isVisible = isLoggedIn
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
            R.id.menu_add_event -> {
                // Open the AddEventFragment when the button is clicked
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container_view, AddEventFragment())
                transaction.addToBackStack(null) // Allows user to navigate back
                transaction.commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}