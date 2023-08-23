import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BirdsViewModel : ViewModel() {

    data class UiState(
        val images: List<BirdImage> = emptyList(),
        val selectedCategory: String? = null,
        val selectedImages: List<BirdImage> = emptyList()
    ) {
        val categories = images.map { it.category }.toSet()
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    init {
        updateImages()
    }

    override fun onCleared() {
        httpClient.close()
    }

    fun selectCategory(category: String) {
        _uiState.update { oldState ->
            oldState.copy(
                selectedCategory = category,
                selectedImages = oldState.images
                    .filter { it.category == category })
        }
    }

    private fun updateImages() {
        viewModelScope.launch {
            val images = getImages()
            _uiState.update {
                it.copy(
                    images = images,
                    selectedImages = images,
                    selectedCategory = null
                )
            }
        }
    }

    private suspend fun getImages(): List<BirdImage> {
        return httpClient.get("http://sebi.io/demo-image-api/pictures.json")
            .body()
    }

}