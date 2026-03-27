import com.supdevinci.cooktail.model.CocktailResponse
import com.supdevinci.cooktail.model.IngredientResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CocktailApiService {

    @GET("random.php")
    suspend fun getCocktail(): CocktailResponse

    @GET("list.php?i=list")
    suspend fun getIngredient(): IngredientResponse

    @GET("filter.php")
    suspend fun getCocktailsByIngredient(@Query("i") ingredient: String): CocktailResponse

    @GET("lookup.php")
    suspend fun getCocktailById(@Query("i") id: String): CocktailResponse

    @GET("lookup.php")
    suspend fun getIngredientById(@Query("iid") id: String): IngredientResponse
}