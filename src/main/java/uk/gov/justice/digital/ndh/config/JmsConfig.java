package uk.gov.justice.digital.ndh.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import static javax.jms.DeliveryMode.NON_PERSISTENT;

@Configuration
@Slf4j
public class JmsConfig implements BeanPostProcessor {

    public static final String OASYS_MESSAGES = "OASYS_MESSAGES";
    public static final String LAST_POLLED = "LAST_POLLED";

    @Value("${activemq.delivery.mode:2}")
    private int amqDeliveryMode;

    @Bean
    public Queue oasysMessageQueue() {
        return new ActiveMQQueue(OASYS_MESSAGES);
    }

    @Bean
    public Queue lastPolledQueue() {
        return new ActiveMQQueue(LAST_POLLED);
    }

    @Bean(name = "activeMqConnectionFactory")
    @Primary
    public ConnectionFactory activeMqConnectionFactory(@Value("${spring.activemq.broker-url}") String brokerUrl) {
        return new ActiveMQConnectionFactory(brokerUrl);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ActiveMQConnectionFactory) {
            final ActiveMQConnectionFactory activeMQConnectionFactory = (ActiveMQConnectionFactory) bean;
            final RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
            redeliveryPolicy.setMaximumRedeliveries(RedeliveryPolicy.NO_MAXIMUM_REDELIVERIES);
            activeMQConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);
        }

        if (bean instanceof JmsTemplate) {
            if (amqDeliveryMode == NON_PERSISTENT) {
                ((JmsTemplate) bean).setExplicitQosEnabled(true);
                ((JmsTemplate) bean).setDeliveryMode(amqDeliveryMode);
            }
        }

        return bean;
    }
}
