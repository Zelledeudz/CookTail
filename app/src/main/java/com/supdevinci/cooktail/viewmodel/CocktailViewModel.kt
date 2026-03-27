import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supdevinci.cooktail.data.remote.RetrofitInstance
import com.supdevinci.cooktail.viewmodel.CocktailState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CocktailViewModel : ViewModel() {

    private val _etat = MutableStateFlow<CocktailState>(CocktailState.Loading)
    val etat: StateFlow<CocktailState> = _etat

    fun fetchCocktail() {
        viewModelScope.launch {
            _etat.value = CocktailState.Loading
            try {
                val response = RetrofitInstance.api.getCocktail()
                _etat.value = CocktailState.Success(response.drinks)

            } catch (e: Exception) {
                _etat.value = CocktailState.Error(
                    message = e.message ?: "Erreur inconnue"
                )
            }
        }
    }
}