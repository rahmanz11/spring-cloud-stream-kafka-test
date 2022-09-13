package com.example.demo.consumer;

import com.example.demo.DemoApplication;
import com.example.demo.schema.Teacher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumerSink {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerSink.class);

    @StreamListener(DemoApplication.MessageRequestConsumer.CHANNEL)
    public void handle(Teacher teacher) {
        LOGGER.info("Consumed message: {}", teacher);
    }
}
