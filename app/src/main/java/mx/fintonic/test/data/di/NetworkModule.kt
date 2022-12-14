package mx.fintonic.test.data.di

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import mx.fintonic.test.BuildConfig
import mx.fintonic.test.data.remote.services.ApiServices
import mx.fintonic.test.data.remote.api.ApiServiceFactory
import mx.fintonic.test.data.remote.models.HeadersInterceptor
import mx.fintonic.test.data.remote.models.ResponseCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class NetworkModule {

    private val READ_TIMEOUT: Long = 120
    private val CONNECT_TIMEOUT: Long = 120

    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .disableHtmlEscaping()
        .create()

    @Singleton
    @Provides
    fun provideOkHttpClient(
        headersInterceptor: HeadersInterceptor,
    ): OkHttpClient =
        OkHttpClient().newBuilder().apply {
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor { message ->
                        Log.d(this.javaClass.name, "provideOkHttpClient: $message")
                    }.apply { level = HttpLoggingInterceptor.Level.BODY }
                )
            }
            addInterceptor(headersInterceptor)
        }.build()

    // region (Own)
    @FintonicRetrofit
    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        responseCallAdapterFactory: ResponseCallAdapterFactory,
        gson: Gson,
    ): Retrofit =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BuildConfig.BASE_URL)
            .addCallAdapterFactory(responseCallAdapterFactory)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Singleton
    @Provides
    fun provideApiServices(
        apiServiceFactory: ApiServiceFactory,
    ): ApiServices = apiServiceFactory.createApiService(
       ApiServices::class.java,
    )
    // endregion
}