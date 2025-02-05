package com.stop.data.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.stop.data.BuildConfig
import com.stop.data.remote.ResultCallAdapter
import com.stop.domain.model.route.tmap.custom.Route
import com.stop.domain.model.route.tmap.custom.TransportRoute
import com.stop.domain.model.route.tmap.custom.WalkRoute
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
internal object NetworkModule {

    private const val T_MAP_APP_KEY_NAME = "appKey"
    private const val OPEN_API_SEOUL_KEY_NAME = "KEY"
    private const val SW_OPEN_API_SEOUL_KEY_NAME = "KEY"
    private const val APIS_KEY_NAME = "ServiceKey"
    private const val WS_KEY_NAME = "ServiceKey"

    @Provides
    @Singleton
    fun provideOkHttpClient(
        customInterceptor: CustomInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(customInterceptor)
            .build()
    }


    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(PolymorphicJsonAdapterFactory
                .of(Route::class.java,"Route")
                .withSubtype(TransportRoute::class.java, "TransportRoute")
                .withSubtype(WalkRoute::class.java, "WalkRoute")
            )
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideTikXmlConverterFactory(): TikXmlConverterFactory {
        return TikXmlConverterFactory.create(TikXml.Builder().exceptionOnUnreadXml(false).build())
    }

    @Provides
    fun provideCustomInterceptor(): CustomInterceptor {
        return CustomInterceptor()
    }

    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @Provides
    fun provideResultCallAdapter(): ResultCallAdapter.Factory {
        return ResultCallAdapter.Factory()
    }

    @Provides
    @Named("Tmap")
    fun provideTmapRetrofitInstance(
        okHttpClient: OkHttpClient,
        moshi: Moshi,
        resultCallAdapter: ResultCallAdapter.Factory,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.T_MAP_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(resultCallAdapter)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Named("OpenApiSeoul")
    fun provideOpenApiSeoulRetrofitInstance(
        okHttpClient: OkHttpClient,
        moshi: Moshi,
        resultCallAdapter: ResultCallAdapter.Factory,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.OPEN_API_SEOUL_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(resultCallAdapter)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Named("WsBus")
    fun provideWsBusRetrofitInstance(
        okHttpClient: OkHttpClient,
        moshi: Moshi,
        resultCallAdapter: ResultCallAdapter.Factory,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.WS_BUS_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(resultCallAdapter)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Named("ApisData")
    fun provideApisDataRetrofitInstance(
        okHttpClient: OkHttpClient,
        tikXmlConverterFactory: TikXmlConverterFactory,
        resultCallAdapter: ResultCallAdapter.Factory,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.APIS_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(resultCallAdapter)
            .addConverterFactory(tikXmlConverterFactory)
            .build()
    }

    @Provides
    @Named("SwOpenApiSeoul")
    fun provideSwOpenApiSeoulRetrofitInstance(
        okHttpClient: OkHttpClient,
        moshi: Moshi,
        resultCallAdapter: ResultCallAdapter.Factory,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.SW_OPEN_API_SEOUL_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(resultCallAdapter)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    class CustomInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val url = chain.request().url.toUri().toString()

            val (name: String, key: String) = when {
//                url.contains("transit/routes") -> {
//                    val response = readJson("response.json")
//                    return chain.proceed(chain.request())
//                        .newBuilder()
//                        .code(200)
//                        .protocol(Protocol.HTTP_2)
//                        .message("success")
//                        .body(
//                            response.toByteArray()
//                                .toResponseBody("application/json".toMediaTypeOrNull())
//                        ).addHeader("content-type", "application/json")
//                        .build()
//                }
                url.contains(BuildConfig.OPEN_API_SEOUL_URL) -> Pair(
                    OPEN_API_SEOUL_KEY_NAME,
                    BuildConfig.BUS_KEY
                )
                url.contains(BuildConfig.SW_OPEN_API_SEOUL_URL) -> Pair(
                    SW_OPEN_API_SEOUL_KEY_NAME,
                    BuildConfig.SUBWAY_KEY
                )
                url.contains(BuildConfig.T_MAP_URL) -> Pair(
                    T_MAP_APP_KEY_NAME,
                    BuildConfig.T_MAP_APP_KEY
                )
                url.contains(BuildConfig.APIS_URL) -> Pair(APIS_KEY_NAME, BuildConfig.BUS_KEY)
                url.contains(BuildConfig.WS_BUS_URL) -> Pair(WS_KEY_NAME, BuildConfig.BUS_KEY)
                else -> {
                    return chain.proceed(chain.request())
                }
            }

            return with(chain) {
                val newRequest = request().newBuilder()
                    .addHeader(name, key)
                    .build()
                proceed(newRequest)
            }
        }
        private fun readJson(fileName: String): String {
            return Thread.currentThread().contextClassLoader?.getResource(fileName)
                ?.readText() ?: ""
        }
    }
}