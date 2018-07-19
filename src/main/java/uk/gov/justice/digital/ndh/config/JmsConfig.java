package uk.gov.justice.digital.ndh.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.Queue;

@Configuration
@Slf4j
public class JmsConfig implements BeanPostProcessor {

    public static final String OASYS_MESSAGES = "OASYS_MESSAGES";

    @Bean
    public Queue oasysMessageQueue() {
        return new ActiveMQQueue(OASYS_MESSAGES);
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
        return bean;
    }
}
