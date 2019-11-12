package com.example.rabbitvhost.util;

import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.rabbit.connection.AbstractRoutingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleResourceHolder;

public class MyRoutingConnectionFactory extends AbstractRoutingConnectionFactory {

  private static final String LOOKUPKEY = "myRCF";


  public MyRoutingConnectionFactory(final ConnectionFactory apiConnectionFactory,
      final ConnectionFactory backendConnectionFactory) {
    final Map<Object, ConnectionFactory> vHostConnectionFactoryMap = new HashMap<>();
    vHostConnectionFactoryMap.put(apiConnectionFactory.getVirtualHost(), apiConnectionFactory);
    vHostConnectionFactoryMap.put(backendConnectionFactory.getVirtualHost(),
        backendConnectionFactory);
    setTargetConnectionFactories(vHostConnectionFactoryMap);
    setDefaultTargetConnectionFactory(backendConnectionFactory);
    setLenientFallback(true);
    setDefaultTargetConnectionFactory(backendConnectionFactory);
  }

  public static void select(final String vhost) {
    SimpleResourceHolder.bind(LOOKUPKEY, vhost);
  }

  public static void unselect() {
    SimpleResourceHolder.unbind(LOOKUPKEY);
  }

  @Override
  protected Object determineCurrentLookupKey() {
    return SimpleResourceHolder.get(LOOKUPKEY);
  }

}
