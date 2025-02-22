package dk.itu.moapd.copenhagenbuzz.ceel.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dk.itu.moapd.copenhagenbuzz.ceel.R
import dk.itu.moapd.copenhagenbuzz.ceel.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setup ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Login button is clicked
        binding.loginToAccount.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("isLoggedIn", true) //user is logged in is passed on with intent
            startActivity(intent)
            finish()
        }

        //Guest User button is clicked
        binding.useGuestUser.setOnClickListener(){
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("isLoggedIn", false) //user is not logged in is passed on with intent
            startActivity(intent)
            finish()
        }

    }
}