package com.tower_kit.framework.command;

import com.tower_kit.framework.message.MessageProcessor;
import com.tower_kit.framework.message.MessageProcessorsFinder;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author <a href="mailto:zjk@jianzhimao.com">Zhang Jiankun</a>
 * @since 0.1.0
 */
public class CommandGateway {

	@Setter
	private MessageProcessorsFinder processorsFinder;

	private Map<Class<?>, MessageProcessor> messageProcessors = HashMap.empty();

	public void registerProcessors(Object object) {
		processorsFinder.findProcessors(object)
				.forEach(messageProcessor -> messageProcessors = messageProcessors.put(messageProcessor.getSupportedMessageType(), messageProcessor));
	}

	@SuppressWarnings("unchecked")
	public <R extends Serializable> R send(Serializable command) {
		return (R) messageProcessors.get(command.getClass())
				.map(processor -> processor.process(command))
				.get()
				.onFailure(throwable -> {
					throw new IllegalArgumentException("", throwable);
				})
				.get();
	}
}
