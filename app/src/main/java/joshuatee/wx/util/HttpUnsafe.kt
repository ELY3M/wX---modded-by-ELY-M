package joshuatee.wx.util

//import okhttp3.Interceptor
//import okhttp3.OkHttpClient
//import java.security.SecureRandom
//import java.security.cert.X509Certificate
//import java.util.concurrent.TimeUnit
//import javax.net.ssl.HostnameVerifier
//import javax.net.ssl.SSLContext
//import javax.net.ssl.SSLSocketFactory
//import javax.net.ssl.TrustManager
//import javax.net.ssl.X509TrustManager
//import javax.security.cert.CertificateException

//
// create an OkHttpClient that does not check SSL certs
// On 2024-08-23 connections to TGFTP broke likely due to an incomplete cert renewal
// since radar data is not sensitive data this temporary workaround is fine
//

@Suppress("unused")
object HttpUnsafe {

//    fun getUnsafeOkHttpClient(): OkHttpClient {
//        try {
//            // Create a trust manager that does not validate certificate chains
//            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
//                @Throws(CertificateException::class)
//                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
//                }
//
//                @Throws(CertificateException::class)
//                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
//                }
//
//                override fun getAcceptedIssuers(): Array<X509Certificate> {
//                    return arrayOf()
//                }
//            }
//            )
//
//            // Install the all-trusting trust manager
//            val sslContext = SSLContext.getInstance("SSL")
//            sslContext.init(null, trustAllCerts, SecureRandom())
//            // Create an ssl socket factory with our all-trusting manager
//            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
//
//            val okHttp3Interceptor = Interceptor { chain ->
//                val request = chain.request()
//                var response = chain.proceed(request)
//                var tryCount = 0
//                while (!response.isSuccessful && tryCount < 3) {
//                    tryCount += 1
//                    response.close()
//                    response = chain.proceed(request)
//                }
//                response
//            }
//
//            val builder = OkHttpClient.Builder()
//            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
//            builder.hostnameVerifier(HostnameVerifier { hostname, session -> true })
//            builder.connectTimeout(15, TimeUnit.SECONDS)
//            builder.readTimeout(15, TimeUnit.SECONDS)
//            builder.addInterceptor(okHttp3Interceptor)
//
//            val okHttpClient: OkHttpClient = builder.build()
//            return okHttpClient
//        } catch (e: Exception) {
//            throw RuntimeException(e)
//        }
//    }
}
