package com.example.demo;

import com.example.demo.DemoApplication.MessageRequestProducer;
import com.example.demo.consumer.ConsumerSink;
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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(brokerProperties = "log.dir=target/${random.uuid}/embedded-kafka")
@TestPropertySource(
        properties = {
                "spring.cloud.stream.kafka.binder.brokers=${spring.embedded.kafka.brokers}",
                "spring.autoconfigure.exclude=org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
                "spring.cloud.stream.bindings.messageRequestInput.group=group-demo",
                "spring.cloud.stream.bindings.messageRequestInput.destination=demo-topic",
                "spring.cloud.stream.bindings.messageRequestOutput.destination=demo-topic"
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
    public void messageIsConsumed() {
        String data = "{\n" +
                "   \"eventAttributes\":{\n" +
                "      \"abb_process_timestamp\":\"2022-09-15, 15:49:00.738Z\",\n" +
                "      \"abb_event_id\":\"c52eujrs-af68-40htd\",\n" +
                "      \"stage\":{\n" +
                "         \"stageDisplayName\":\"Scan - project name\",\n" +
                "         \"stageNumber\":2,\n" +
                "         \"startTime\":\"2022-09-02 15:00:51 EST\",\n" +
                "         \"stageMetadata\":{\n" +
                "            \"workSpace\":\"sample-python-aws\",\n" +
                "            \"imageName\":\"sample-python-aws-pipelinename\",\n" +
                "            \"secure\":\"pass\",\n" +
                "            \"aquaInstance\":\"onPrem\",\n" +
                "            \"url\":\"https:\\/\\/docker-image\"\n" +
                "         },\n" +
                "         \"pipelineMetadata\":{\n" +
                "            \n" +
                "         },\n" +
                "         \"status\":\"success\"\n" +
                "      },\n" +
                "      \"abb_src_event_timestamp\":\"2022-09-15 15:00:51 EST\",\n" +
                "      \"abb_event_timestamp_utc\":\"2022-09-15T15:48:59Z\",\n" +
                "      \"pipeline_id\":\"Zafdkjvahjk64\",\n" +
                "      \"abb_endpoint_type\":\"abb_internal\"\n" +
                "   },\n" +
                "   \"eventHeader\":{\n" +
                "      \"event+activity_type\":\"Scan Result\",\n" +
                "      \"source_event_id\":\"TEST1\",\n" +
                "      \"source_system_name\":\"ABCD - APP CONTROLS\",\n" +
                "      \"event_timestamp\":\"2022-09-15T15:48:59Z\",\n" +
                "      \"event_channel_type\":\"Development Lifecycle Events\",\n" +
                "      \"source_application_code\":\"ABCD\"\n" +
                "   }\n" +
                "}";
        producer.messageRequestOutput().send(MessageBuilder
                .withPayload(data)
                .build());
        verify(listener, timeout(5000))
                .consume(argThat(d -> d.contains("c52eujrs-af68-40htd")));
    }

}
