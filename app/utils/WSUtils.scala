package utils

import play.api.libs.ws.{WSRequestHolder, DefaultWSClientConfig}
import play.api.libs.ws.ning.{NingWSClient, NingAsyncHttpClientConfigBuilder}
import com.ning.http.client.AsyncHttpClientConfig

object WSUtils {

    def url(url:String):WSRequestHolder = {
        WSClient.url(url)
    }

    val WSClient = {
        val builder: NingAsyncHttpClientConfigBuilder = new NingAsyncHttpClientConfigBuilder(new DefaultWSClientConfig, new AsyncHttpClientConfig.Builder)
        val httpClientConfig: AsyncHttpClientConfig = builder.build
        new NingWSClient(httpClientConfig)
    }
}
