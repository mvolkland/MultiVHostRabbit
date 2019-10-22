package de.mvolkland.rabbitwebclient;

import java.util.concurrent.Executors;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

@Configuration
@ComponentScan
@ConfigurationProperties(prefix = "app")
public class RabbitwebclientConfig {
    public static final String Q_API_QUERY = "q.api.query";

    private String vhostApi;

    public void setVhostApi(final String vhostApi) {
        this.vhostApi = vhostApi;
      }

    @Bean
    public String vHostApi() {
      return vhostApi;
    }

    @Bean
    public ConnectionFactory apiConnectionFactory() {
      final CachingConnectionFactory ccf = new CachingConnectionFactory();
      ccf.setHost("localhost");
      ccf.setVirtualHost(vHostApi());
      final CustomizableThreadFactory tf = new CustomizableThreadFactory();
      tf.setThreadNamePrefix("api-conn-");
      ccf.setExecutor(Executors.newCachedThreadPool(tf));
      return ccf;
    }
    
    @Bean
    public RabbitAdmin apiAdmin() {
      return new RabbitAdmin(apiConnectionFactory());
    }

	@Bean
    public Queue queryQueue() {
        var q = new Queue(Q_API_QUERY, false);
        q.setAdminsThatShouldDeclare(apiAdmin());
        return q;
    }
}
