package pe.pixelcollage.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pe.pixelcollage.app.data.repository.AuthRepository
import pe.pixelcollage.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CreateAccountState {
    object Idle : CreateAccountState()
    object Loading : CreateAccountState()
    object Success : CreateAccountState()
    data class Error(val message: String) : CreateAccountState()
}

class CreateAccountViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _createAccountState = MutableStateFlow<CreateAccountState>(CreateAccountState.Idle)
    val createAccountState: StateFlow<CreateAccountState> = _createAccountState

import android.content.Context
import pe.pixelcollage.app.R

    fun createAccount(context: Context, nick: String, email: String, password: String) {
        viewModelScope.launch {
            _createAccountState.value = CreateAccountState.Loading
            try {
                val firebaseUser = authRepository.createAccount(email, password)
                if (firebaseUser != null) {
                    userRepository.createUser(firebaseUser.uid, nick, email)
                    _createAccountState.value = CreateAccountState.Success
                } else {
                    _createAccountState.value = CreateAccountState.Error(context.getString(R.string.create_account_error))
                }
            } catch (e: Exception) {
                _createAccountState.value = CreateAccountState.Error(e.message ?: context.getString(R.string.unknown_error))
            }
        }
    }
}
