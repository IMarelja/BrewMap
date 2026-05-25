package hr.algebra.mobileapp.service.category

import hr.algebra.mobileapp.models.Category
import hr.algebra.mobileapp.service.HardCodeData

/**
 * **Test / offline** category service — no network required.
 *
 * All seed data lives in [HardCodeData], which is created once by
 * [hr.algebra.mobileapp.service.ServiceProvider] and shared across every
 * hard-code stub.  This class only contains query logic.
 */
class CategoryServiceHardCode(private val data: HardCodeData) : ICategoryService {

    // ── ICategoryService ──────────────────────────────────────────────────────

    override suspend fun getAll(): List<Category> = data.categories

    override suspend fun getByTag(tag: String): Category? =
        data.categories.find { it.tag.equals(tag, ignoreCase = true) }
}
