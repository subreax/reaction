package com.subreax.reaction.data

import android.content.Context
import android.util.Log
import com.subreax.reaction.api.BackendService
import com.subreax.reaction.data.auth.AuthRepository
import com.subreax.reaction.data.auth.LocalAuthDataSource
import com.subreax.reaction.data.auth.impl.AuthRepositoryImpl
import com.subreax.reaction.data.chat.ChatRepository
import com.subreax.reaction.data.chat.impl.ChatRepositoryImpl
import com.subreax.reaction.data.user.UserRepository
import com.subreax.reaction.data.user.impl.UserRepositoryImpl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val authRepository: AuthRepository
    val userRepository: UserRepository
    val chatRepository: ChatRepository
    val socketService: SocketService
}

class AppContainerImpl(private val appContext: Context) : AppContainer {
    private val _client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(LoggingInterceptor())
            .build()
    }

    private val _api by lazy {
        val retrofit = Retrofit.Builder()
            .client(_client)
            .baseUrl("$BASE_URL/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(BackendService::class.java)
    }

    private val authSharedPrefs = appContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val localAuthDataSource = LocalAuthDataSource(authSharedPrefs)

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(_api, localAuthDataSource)
    }

    override val userRepository: UserRepository by lazy {
        UserRepositoryImpl(_api, authRepository)
    }

    override val chatRepository: ChatRepository by lazy {
        ChatRepositoryImpl(_api, authRepository, userRepository, socketService)
    }

    override val socketService: SocketService by lazy {
        WebSocketIoService(BASE_URL, authRepository, userRepository)
    }

    companion object {
        private const val BASE_URL = "http://37.18.110.82:3000"
    }
}

private class LoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        Log.d("LoggingInterceptor", "request: ${chain.request().url.toUrl()}")
        return chain.proceed(chain.request())
    }
}
