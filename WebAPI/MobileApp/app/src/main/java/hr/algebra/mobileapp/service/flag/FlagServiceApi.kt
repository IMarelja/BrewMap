package hr.algebra.mobileapp.service.flag

import android.util.Log
import com.google.gson.reflect.TypeToken
import hr.algebra.mobileapp.api.API
import hr.algebra.mobileapp.api.HttpMethod
import hr.algebra.mobileapp.api.ServiceResult
import hr.algebra.mobileapp.api.toServiceResult
import hr.algebra.mobileapp.models.flag.Flag

/**
 * **Production** flag service — delegates to `POST api/Flag`.
 */
class FlagServiceApi : IFlagService {

    override suspend fun create(
        targetType: String,
        targetId: String,
        reason: String,
        description: String?
    ): ServiceResult<Flag> {
        val client = API.createClient()
        val body = mapOf(
            "target"      to mapOf("type" to targetType, "id" to targetId),
            "reason"      to reason,
            "description" to description
        )
        val result = client.request(
            endpoint     = "Flag",
            method       = HttpMethod.POST,
            body         = body,
            responseType = object : TypeToken<Flag>() {}
        ).toServiceResult("Could not submit report.")
        Log.d("FlagServiceApi", "create → success=${result.isSuccess}")
        return result
    }
}
