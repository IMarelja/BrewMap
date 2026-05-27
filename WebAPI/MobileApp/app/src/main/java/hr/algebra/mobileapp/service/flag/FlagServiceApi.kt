package hr.algebra.mobileapp.service.flag

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.api.toServiceResult
import hr.algebra.mobileapp.models.flag.CreateFlagRequest
import hr.algebra.mobileapp.models.flag.Flag
import hr.algebra.mobileapp.models.flag.ReportTarget
import hr.algebra.mobileapp.service.RequestBodyValidator

/**
 * **Production** flagService service — delegates to `POST api/Flag`.
 */
class FlagServiceApi : IFlagService {

    override suspend fun create(
        targetType: String,
        targetId: String,
        reason: String,
        description: String?
    ): ServiceResult<Flag> {
        val request = CreateFlagRequest(
            target = ReportTarget(type = targetType, id = targetId),
            reason = reason,
            description = description
        )
        val validationErrors = RequestBodyValidator.validateCreateFlag(request)
        if (validationErrors.isNotEmpty()) {
            return ServiceResult.failure(validationErrors)
        }

        val client = API.createClient()
        val result = client.request(
            endpoint     = "Flag",
            method       = HttpMethod.POST,
            body         = request,
            responseType = object : TypeToken<Flag>() {}
        ).toServiceResult("Could not submit report.")
        Log.d("FlagServiceApi", "create → success=${result.isSuccess}")
        return result
    }
}
