package com.example.demo;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.example.demo.consumer.ConsumerSink;
import com.example.demo.schema.Teacher;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import com.example.demo.DemoApplication.MessageRequestProducer;

@SpringBootTest
@EmbeddedKafka(brokerProperties = "log.dir=target/${random.uuid}/embedded-kafka")
@TestPropertySource(
        properties = {
                "spring.cloud.stream.kafka.binder.brokers=${spring.embedded.kafka.brokers}",
                "spring.autoconfigure.exclude=org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
                "spring.cloud.stream.bindings.messageRequestInput.group=group-teacher",
                "spring.cloud.stream.bindings.messageRequestInput.destination=teacher-details",
                "spring.cloud.stream.bindings.messageRequestOutput.destination=teacher-details"
        })
public class DemoApplicationTests {

    @TestConfiguration
    static class Config {

        @Bean
        public BeanPostProcessor messageRequestListenerPostProcessor() {
            return new ProxiedMockPostProcessor(ConsumerSink.class);
        }

        static class ProxiedMockPostProcessor implements BeanPostProcessor {
            private final Class<?> mockedClass;

            public ProxiedMockPostProcessor(Class<?> mockedClass) {
                this.mockedClass = mockedClass;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName)
                    throws BeansException {
                if (mockedClass.isInstance(bean)) {
                    return Mockito.mock(mockedClass, AdditionalAnswers.delegatesTo(bean));
                }
                return bean;
            }
        }
    }

    @Autowired
    private ConsumerSink listener;

    @Autowired
    private MessageRequestProducer producer;

    @Test
    public void messageIsReceived() {
        Teacher req = new Teacher(11, "Robert", 40, "History");
        producer.messageRequestOutput().send(MessageBuilder
                .withPayload(req)
                .build());
        verify(listener, timeout(5000))
                .handle(argThat(m -> m.getId().equals(req.getId())));

    }

}
