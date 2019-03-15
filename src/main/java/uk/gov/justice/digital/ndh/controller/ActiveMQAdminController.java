package uk.gov.justice.digital.ndh.controller;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

@RestController
@Slf4j
public class ActiveMQAdminController {

    public static final String MBEAN_PATH = "org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=%s";

    @Autowired
    private MBeanServer mBeanServer;

    @RequestMapping(path = "/activemq/queues/{queueName}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Object> getStatus(@PathVariable("queueName") String queueName) throws MalformedObjectNameException, ReflectionException, AttributeNotFoundException, MBeanException {

        log.info("Received GET queue status for queue {}", queueName);

        final ImmutableMap<String, Object> infos;
        try {
            infos = ImmutableMap.<String, Object>builder()
                    .put("QueueSize", mBeanServer.getAttribute(ObjectName.getInstance(String.format(MBEAN_PATH, queueName)), "QueueSize"))
                    .put("ConsumerCount", mBeanServer.getAttribute(ObjectName.getInstance(String.format(MBEAN_PATH, queueName)), "ConsumerCount"))
                    .put("ProducerCount", mBeanServer.getAttribute(ObjectName.getInstance(String.format(MBEAN_PATH, queueName)), "ProducerCount"))
                    .build();
        } catch (InstanceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(infos, HttpStatus.OK);
    }
}
