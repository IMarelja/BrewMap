package hr.algebra.mobileapp.service.category

import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.models.category.Category
import hr.algebra.mobileapp.service.HardCodeData

/**
 * **Test / offline** categoryService service — no network required.
 *
 * All seed data lives in [HardCodeData], which is created once by
 * [hr.algebra.mobileapp.service.ServiceProvider] and shared across every
 * hard-code stub.  This class only contains query logic.
 */
class CategoryServiceHardCode(private val data: HardCodeData) : ICategoryService {

    override suspend fun getAll(): ServiceResult<List<Category>> =
        ServiceResult.success(data.categories)

    override suspend fun getByTag(tag: String): ServiceResult<Category?> =
        ServiceResult.success(data.categories.find { it.tag.equals(tag, ignoreCase = true) })
}
