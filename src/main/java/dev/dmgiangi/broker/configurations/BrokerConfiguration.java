package dev.dmgiangi.broker.configurations;

import io.netty.handler.ssl.SslProtocols;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class BrokerConfiguration {
    public static final String PATH_VARIABLE_NAME = "name";
    @Value("${mosquitto.uri}")
    private String host;
    @Value("${mosquitto.username}")
    private String username;
    @Value("${mosquitto.password}")
    private String password;
    @Value("${mosquitto.truststorePath}")
    private String truststorePassword;
    @Value("${mosquitto.truststorePassword}")
    private String truststorePath;

    @Bean
    MqttPahoClientFactory mqttPahoClientFactory() {
        final var factory = new DefaultMqttPahoClientFactory();
        final var options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setServerURIs(new String[]{host});
        options.setSocketFactory(getSocketFactory());

        factory.setConnectionOptions(options);
        return factory;
    }

    public SSLSocketFactory getSocketFactory() {

        SSLSocketFactory result = null;

        try (InputStream in = new FileInputStream(truststorePath)){
            KeyStore keystoreTrust = KeyStore.getInstance("JKS");
            keystoreTrust.load(in, truststorePassword.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keystoreTrust);
            SSLContext sslContext = SSLContext.getInstance(SslProtocols.TLS_v1_3);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            result = sslContext.getSocketFactory();
        }
        catch ( Exception ex ) {
            // log exception
        }

        return result;
    }
    @Bean
    MqttPahoMessageHandler outboundAdapter(MqttPahoClientFactory factory) {
        var mh = new MqttPahoMessageHandler("producer", factory);
        mh.setDefaultTopic("topic");
        return mh;
    }

    @Bean
    RouterFunction<ServerResponse> outbound(MessageChannel out) {
        return route()
                .GET("/send/{name}", request -> {
                    final var name = request.pathVariable(PATH_VARIABLE_NAME);
                    final var message = MessageBuilder.withPayload("Hi mqtt " + name).build();
                    out.send(message);
                    return ServerResponse.ok().build();
                })
                .build();
    }

    @Bean
    MessageChannel out() {
        return MessageChannels.direct().get();
    }

    @Bean
    IntegrationFlow outboundFlow(MessageChannel out, MqttPahoMessageHandler outboundAdapter) {
        return IntegrationFlow.from(out).handle(outboundAdapter).get();
    }

    @Bean
    IntegrationFlow inboundFlow(MqttPahoMessageDrivenChannelAdapter inboundAdapter) {
        return IntegrationFlow.from(inboundAdapter).handle((payload, headers) -> {
            System.out.println("Incoming message: " + payload);
            headers.forEach((s, o) -> System.out.println(s + " -> " + o));
            return null;
        }).get();
    }

    @Bean
    MqttPahoMessageDrivenChannelAdapter inboundAdapter(MqttPahoClientFactory factory) {
        return new MqttPahoMessageDrivenChannelAdapter("consumer", factory, "topic");
    }
}
