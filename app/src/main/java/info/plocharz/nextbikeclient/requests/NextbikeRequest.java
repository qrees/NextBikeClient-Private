package info.plocharz.nextbikeclient.requests;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.xml.SimpleXmlHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import info.plocharz.nextbikeclient.Application;
import info.plocharz.nextbikeclient.BuildConfig;
import info.plocharz.nextbikeclient.Logger;

public abstract class NextbikeRequest<T> extends SpringAndroidSpiceRequest<T> {

    Class clazz;
    public NextbikeRequest(Class clazz) {
        super(clazz);
        this.clazz = clazz;
    }
    
    abstract protected String getUrl();

    protected MultiValueMap<String, String> getData(MultiValueMap<String, String> data){
        return data;
    };

    @Override
    public T loadDataFromNetwork() throws Exception {
        String url = this.getUrl();
        RestTemplate template = getRestTemplate();
//        List<HttpMessageConverter<?>> converters = template.getMessageConverters();
//        if (BuildConfig.DEBUG && false) {
//            template.getMessageConverters().add(new DebugMessageConverter());
//            converters = new ArrayList<>();
//            converters.add(new DebugMessageConverter());
//            template.setMessageConverters(converters);
//        }
        HttpHeaders requestHeaders = new HttpHeaders();

        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("apikey", Application.API_KEY);
        data = this.getData(data);
        HttpEntity<?> requestEntity = new HttpEntity<>(data, requestHeaders);

        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new FormHttpMessageConverter());
        converters.add(new SimpleXmlHttpMessageConverter());
        template.setMessageConverters(converters);
        ClientHttpRequestFactory factory = template.getRequestFactory();
        if (factory instanceof HttpComponentsClientHttpRequestFactory) {
            HttpComponentsClientHttpRequestFactory advancedFactory = (HttpComponentsClientHttpRequestFactory) factory;
            advancedFactory.setConnectTimeout(10 * 1000);
            advancedFactory.setReadTimeout(30 * 1000);
        } else if (factory instanceof SimpleClientHttpRequestFactory) {
            SimpleClientHttpRequestFactory advancedFactory = (SimpleClientHttpRequestFactory) factory;
            advancedFactory.setConnectTimeout(10 * 1000);
            advancedFactory.setReadTimeout(30 * 1000);
        }
        if(this.usePost())
            return (T) template.postForObject(url, requestEntity, this.clazz);
        else
            return (T) template.getForObject(url, this.clazz);
    }

    protected boolean usePost() {
        return true;
    }

    class DebugMessageConverter extends SimpleXmlHttpMessageConverter {

        @Override
        protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage)
                throws IOException, HttpMessageNotReadableException {
            DebugHttpInputMessage debug_input_message = new DebugHttpInputMessage(inputMessage);
            Object res = super.readInternal(clazz, debug_input_message);
            Logger.d(String.format("Response: %s", new String(debug_input_message.getBodyBytes())));
            return res;
        }

    }

    class DebugHttpInputMessage implements HttpInputMessage {
        HttpInputMessage message;
        byte[] buffer;

        public DebugHttpInputMessage(HttpInputMessage message_){
            message = message_;
            buffer = null;
        }

        public byte[] getBodyBytes(){
            return buffer;
        }

        @Override
        public InputStream getBody() throws IOException {
            InputStream body_ = message.getBody();
            if (buffer == null) {
                buffer = IOUtils.toByteArray(body_);
                return new ByteArrayInputStream(buffer);
            } else {
                return body_;
            }
        }

        @Override
        public HttpHeaders getHeaders() {
            return message.getHeaders();
        }
    }
}
