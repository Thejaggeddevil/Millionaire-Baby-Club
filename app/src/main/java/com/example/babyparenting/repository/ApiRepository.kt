package com.example.babyparenting.data.repository

import com.example.babyparenting.data.model.UiState
import com.example.babyparenting.network.api.BabyApi
import com.example.babyparenting.network.model.AdviceRequest
import com.example.babyparenting.network.model.AdviceResponse

class ApiRepository(private val api: BabyApi) {

    suspend fun fetchAdvice(model: String, query: String): UiState<AdviceResponse> {
        return try {
            val response = api.getAdvice(AdviceRequest(model = model, text = query))
            when {
                response.isSuccessful && response.body() != null ->
                    UiState.Success(response.body()!!)
                response.isSuccessful ->
                    UiState.Error("Server returned empty response.", retryable = false)
                else ->
                    UiState.Error("Server error ${response.code()}: ${response.message()}",
                        retryable = response.code() >= 500)
            }
        } catch (e: java.net.SocketTimeoutException) {
            UiState.Error("Request timed out. Check your connection.", retryable = true)
        } catch (e: java.io.IOException) {
            UiState.Error("No internet connection.", retryable = true)
        } catch (e: Exception) {
            UiState.Error(e.localizedMessage ?: "Unexpected error.", retryable = false)
        }
    }
}