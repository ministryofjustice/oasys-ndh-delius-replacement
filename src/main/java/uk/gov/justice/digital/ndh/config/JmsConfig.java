package uk.gov.justice.digital.ndh.config;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import static javax.jms.Session.CLIENT_ACKNOWLEDGE;

@Configuration
public class JmsConfig {

    public static final String OASYS_MESSAGES = "OASYS_MESSAGES";

    @Bean
    public Queue oasysMessageQueue() {
        return new ActiveMQQueue(OASYS_MESSAGES);
    }

    @Bean
    public DefaultJmsListenerContainerFactory myFactory(
            DefaultJmsListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory =
                new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setSessionAcknowledgeMode(CLIENT_ACKNOWLEDGE);
        return factory;
    }

}
