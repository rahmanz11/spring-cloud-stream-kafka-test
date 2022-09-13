package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.schema.client.EnableSchemaRegistryClient;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

@SpringBootApplication
@EnableBinding({DemoApplication.MessageRequestProducer.class, DemoApplication.MessageRequestConsumer.class})
@EnableSchemaRegistryClient
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	public interface MessageRequestProducer {
		String CHANNEL = "messageRequestOutput";

		@Output(CHANNEL)
		MessageChannel messageRequestOutput();
	}

	public interface MessageRequestConsumer {
		String CHANNEL = "messageRequestInput";

		@Input(CHANNEL)
		SubscribableChannel messageRequestInput();
	}
}
