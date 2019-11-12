package com.example.rabbitvhost;

import java.util.concurrent.Executors;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import com.example.rabbitvhost.util.MyRoutingConnectionFactory;

@Configuration
@ComponentScan
@ConfigurationProperties(prefix = "app")
public class RabbitvhostConfiguration {
  private String vhostApi;
  private String vhostBackend;


  public void setVhostApi(final String vhostApi) {
    this.vhostApi = vhostApi;
  }

  public void setVhostBackend(final String vhostBackend) {
    this.vhostBackend = vhostBackend;
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
  public RabbitListenerContainerFactory apiContainerFactory() {
    final DirectRabbitListenerContainerFactory f = new DirectRabbitListenerContainerFactory();
    f.setConnectionFactory(apiConnectionFactory());
    return f;
  }

  @Bean
  public ConnectionFactory backendConnectionFactory() {
    final CachingConnectionFactory ccf = new CachingConnectionFactory();
    ccf.setHost("localhost");
    ccf.setVirtualHost(vHostBackend());
    final CustomizableThreadFactory tf = new CustomizableThreadFactory();
    tf.setThreadNamePrefix("backend-conn-");
    ccf.setExecutor(Executors.newCachedThreadPool(tf));
    return ccf;
  }

  @Bean
  public RabbitListenerContainerFactory backendContainerFactory() {
    final DirectRabbitListenerContainerFactory f = new DirectRabbitListenerContainerFactory();
    f.setConnectionFactory(backendConnectionFactory());
    return f;
  }

  @Bean
  public RabbitAdmin apiAdmin() {
    return new RabbitAdmin(apiConnectionFactory());
  }

  @Bean
  public RabbitAdmin backendAdmin() {
    return new RabbitAdmin(backendConnectionFactory());
  }

  @Bean
  @Primary
  public ConnectionFactory defaultConnectionFactory(final ConnectionFactory apiConnectionFactory,
      final ConnectionFactory backendConnectionFactory) {
    return new MyRoutingConnectionFactory(apiConnectionFactory, backendConnectionFactory);
  }

  @Bean
  public String vHostApi() {
    return vhostApi;
  }

  @Bean
  public String vHostBackend() {
    return vhostBackend;
  }

  @Bean
  public Queue queryReplyQueueApi() {
    return createQueue("q.api.query.reply", apiAdmin());
  }

  @Bean
  public Queue queryReplyQueueBackend() {
    return createQueue("q.backend.query.reply", backendAdmin());
  }


  private Queue createQueue(final String qname, final RabbitAdmin admin) {
    final Queue q = new Queue(qname, false);
    q.setAdminsThatShouldDeclare(admin);
    return q;
  }



}
