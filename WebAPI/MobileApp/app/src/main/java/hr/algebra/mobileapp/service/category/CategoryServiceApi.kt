package hr.algebra.mobileapp.service.category

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.models.Category

/**
 * **Production** category service — delegates every call to the BrewMap REST API.
 *
 * All endpoints require a valid JWT; the token is injected automatically by
 * [hr.algebra.mobileapp.api.API.createClient] via [hr.algebra.mobileapp.auth.TokenManager].
 *
 * Base URL is read from `res/values/strings.xml` → `api_base_url`.
 */
class CategoryServiceApi : ICategoryService {

    // ── GET api/Category ──────────────────────────────────────────────────────

    override suspend fun getAll(): List<Category> {
        val client = API.createClient()
        val res = client.request(
            endpoint     = "Category",
            method       = HttpMethod.GET,
            responseType = object : TypeToken<List<Category>>() {}
        ).getOrThrow()
        Log.d("CategoryServiceApi", "getAll() → ${res.size} categories")
        return res
    }

    // ── GET api/Category/{tag} ────────────────────────────────────────────────

    override suspend fun getByTag(tag: String): Category? {
        return try {
            val client = API.createClient()
            val res = client.request(
                endpoint     = "Category/$tag",
                method       = HttpMethod.GET,
                responseType = object : TypeToken<Category>() {}
            ).getOrThrow()
            Log.d("CategoryServiceApi", "getByTag($tag) → $res")
            res
        } catch (e: Exception) {
            // 404 — tag not found
            Log.d("CategoryServiceApi", "getByTag($tag) → null (${e.message})")
            null
        }
    }
}
