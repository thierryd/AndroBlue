package androblue.app.login

import androblue.app.HomeActivity
import androblue.app.R
import androblue.app.repository.AccountRepository
import androblue.app.service.DeveloperPropertiesService
import androblue.common.dagger.ScopeActivity
import androblue.common.ext.blockingOnClickListener
import androblue.common.ext.onCreateRuns
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ScopeActivity
class LoginActivityController @Inject constructor(private val loginActivity: LoginActivity,
                                                  private val developerPropertiesService: DeveloperPropertiesService,
                                                  private val accountRepository: AccountRepository) {

    init {
        loginActivity.onCreateRuns(::onActivityCreated)
    }

    private fun onActivityCreated() {
        loginActivity.binding.run {
            if (developerPropertiesService.developerOverrideUsername().isNotEmpty()) {
                loginUsername.setText(developerPropertiesService.developerOverrideUsername())
                loginPassword.setText(developerPropertiesService.developerOverridePassword())
                loginPin.setText(developerPropertiesService.developerOverridePin())
            }

            loginButton.blockingOnClickListener {
                loginLoadingbg.isVisible = true
                loginLoading.isVisible = true

                val username = loginUsername.text.toString()
                if (username == "demo") {
                    startHomeActivity()
                } else {
                    loginActivity.lifecycleScope.launch {
                        val result = accountRepository.login(username, loginPassword.text.toString(), loginPin.text.toString())

                        withContext(Dispatchers.Main) {
                            if (result.loggedIn) {
                                startHomeActivity()
                            } else {
                                loginLoadingbg.isVisible = false
                                loginLoading.isVisible = false

                                AlertDialog.Builder(loginActivity)
                                        .setMessage(R.string.login_invalidlogin)
                                        .setPositiveButton(android.R.string.ok, null)
                                        .show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startHomeActivity() {
        val intent = Intent(loginActivity, HomeActivity::class.java)
        loginActivity.startActivity(intent)
        loginActivity.finish()
    }
}