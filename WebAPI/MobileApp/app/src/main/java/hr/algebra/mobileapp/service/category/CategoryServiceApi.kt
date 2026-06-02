package hr.algebra.mobileapp.service.category

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.api.toServiceResult
import hr.algebra.mobileapp.models.category.Category

/**
 * **Production** categoryService service — delegates every call to the BrewMap REST API.
 *
 * All endpoints require a valid JWT; the token is injected automatically by
 * [hr.algebra.mobileapp.api.API.createClient] via [hr.algebra.mobileapp.auth.TokenManager].
 */
class CategoryServiceApi : ICategoryService {

    // ── GET api/Category ──────────────────────────────────────────────────────

    override suspend fun getAll(): ServiceResult<List<Category>> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "Category",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Category>>() {}
        ).toServiceResult("Could not load categories.")
        Log.d("CategoryServiceApi", "getAll() → success=${result.isSuccess}")
        return result
    }

    // ── GET api/Category/{tag} ────────────────────────────────────────────────

    override suspend fun getByTag(tag: String): ServiceResult<Category?> {
        val client = API.createClient()
        val result = client.request(
            endpoint     = "Category/$tag",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<Category?>() {}
        ).toServiceResult(treatNotFoundAsEmpty = true)
        Log.d("CategoryServiceApi", "getByTag($tag) → data=${result.data}")
        return result
    }
}
