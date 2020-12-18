package bf.fasobizness.bafatech.activities.user

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import bf.fasobizness.bafatech.MainActivity
import bf.fasobizness.bafatech.R
import bf.fasobizness.bafatech.helper.RetrofitClient
import bf.fasobizness.bafatech.interfaces.API
import bf.fasobizness.bafatech.models.User
import bf.fasobizness.bafatech.utils.MySharedManager
import com.auth0.android.jwt.JWT
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LoginActivity : AppCompatActivity() {
    private lateinit var password: TextInputLayout
    private lateinit var email: TextInputLayout
    private lateinit var failedLoginError: TextView
    private lateinit var txtLogin: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnLogin: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.left_white)
        toolbar.setNavigationOnClickListener { finish() }
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        btnLogin = findViewById(R.id.layout_login)
        progressBar = findViewById(R.id.progress_bar_login)
        val forgotPassword = findViewById<Button>(R.id.forgot_password)
        val btnRegister = findViewById<TextView>(R.id.btn_register)
        txtLogin = findViewById(R.id.txt_login)
        initBtn()
        failedLoginError = findViewById(R.id.failed_login_error)
        forgotPassword.setOnClickListener { startActivity(Intent(this@LoginActivity, ActivityForgetPasswd::class.java)) }
        btnRegister.setOnClickListener { action() }
        btnLogin.isClickable = true
        btnLogin.setOnClickListener {
            if (checkEmail() or checkPassword()) {
                Toast.makeText(this@LoginActivity, R.string.verifier_les_champs, Toast.LENGTH_SHORT).show()
            } else {
                failedLoginError.visibility = View.GONE
                txtLogin.setText(R.string.chargement_en_cours)
                progressBar.visibility = View.VISIBLE
                btnLogin.isClickable = false
                logIn()
            }
        }
    }

    private fun logIn() {
        val txtEmail = email.editText?.text.toString()
        val txtPassword = password.editText?.text.toString()
        val api = RetrofitClient.createService(API::class.java, txtEmail, txtPassword)
        val call = api.login()
        call.enqueue(object : Callback<User?> {
            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                Log.d(TAG, response.toString())
                initBtn()
                if (response.isSuccessful) {
                    if (response.code() == 200) {
                        val user = response.body()
                        if (user != null) {
                            val jwt = JWT(user.authorization)
                            val shared = MySharedManager(this@LoginActivity)
                            shared.username = jwt.getClaim("username").asString()
                            shared.user = jwt.getClaim("sub").asString()
                            shared.photo = jwt.getClaim("photo").asString()
                            shared.email = jwt.getClaim("email").asString()
                            shared.token = user.authorization
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }
                } else {
                    if (response.code() == 400) {
                        val message = getString(R.string.nom_d_utilisateur_ou_mot_de_passe_incorrect)
                        Log.v(TAG, message)
                        failedLoginError.text = message
                        failedLoginError.visibility = View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: Call<User?>, t: Throwable) {
                initBtn()
                Toast.makeText(this@LoginActivity, R.string.pas_d_acces_internet, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initBtn() {
        txtLogin.setText(R.string.se_connecter)
        progressBar.visibility = View.GONE
        btnLogin.isClickable = true
    }

    private fun checkEmail(): Boolean {
        val txtEmail = email.editText?.text.toString().trim { it <= ' ' }
        val isValid = Patterns.EMAIL_ADDRESS.matcher(txtEmail).matches()
        return if (txtEmail.isEmpty()) {
            email.error = getString(R.string.adresse_email_requise)
            true
        } else if (!isValid) {
            email.error = getString(R.string.adresse_email_invalide)
            true
        } else {
            email.error = null
            false
        }
    }

    private fun checkPassword(): Boolean {
        val txtMdp = password.editText?.text.toString().trim { it <= ' ' }
        return when {
            txtMdp.isEmpty() -> {
                password.error = getString(R.string.mot_de_passe_requis)
                true
            }
            txtMdp.length < 4 -> {
                password.error = getString(R.string.mot_de_passe_invalide)
                true
            }
            else -> {
                password.error = null
                false
            }
        }
    }

    private fun action() {
        val items = arrayOf<CharSequence>(getString(R.string.entreprise), getString(R.string.particulier))
        val builder = AlertDialog.Builder(this@LoginActivity)
        builder.setTitle("Type de compte")
        builder.setItems(items) { dialog: DialogInterface, i: Int ->
            when {
                items[i] == getString(R.string.entreprise) -> {
                    val intent = Intent(this, ActivitySignUp::class.java)
                    intent.putExtra("type", "entreprise")
                    startActivity(intent)
                }
                items[i] == getString(R.string.particulier) -> {
                    val intent = Intent(this, ActivitySignUp::class.java)
                    intent.putExtra("type", "particulier")
                    startActivity(intent)
                }
                else -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}