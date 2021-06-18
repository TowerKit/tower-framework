package com.tower_kit.framework.command;

import com.tower_kit.framework.message.MessageProcessor;
import com.tower_kit.framework.message.MessageProcessorsFinder;
import io.vavr.collection.List;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:zjk@jianzhimao.com">Zhang Jiankun</a>
 * @since 0.1.0
 */
class CommandGatewayTest {

	static class C1_Classes {

		@Retention(RetentionPolicy.RUNTIME)
		@Target(ElementType.METHOD)
		@interface CommandHandler {  }

		static class CommandA implements Serializable {}
		static class CommandB implements Serializable {}

		static class Foo {

			@Getter
			private Class<?> handledCommandType;

			@CommandHandler
			public void handle(CommandA command) {
				this.handledCommandType = command.getClass();
			}

			@CommandHandler
			public void handle(CommandB command) {
				this.handledCommandType = command.getClass();
			}
		}

		@RequiredArgsConstructor
		static class CommandProcessor implements MessageProcessor {

			@Getter
			private final Object target;

			@Getter
			private final Method method;

			@SuppressWarnings("unchecked")
			@Override
			public <I extends Serializable> Class<I> getSupportedMessageType() {
				return (Class<I>) method.getParameterTypes()[0];
			}

			@SuppressWarnings("unchecked")
			@Override
			public <I extends Serializable, O extends Serializable> Try<O> process(I message) {
				return Try.of(() -> (O) method.invoke(target, message));
			}
		}

		@Log
		static class CommandProcessorsFinder implements MessageProcessorsFinder {

			@Override
			public List<MessageProcessor> findProcessors(Object object) {
				return List.of(object.getClass().getMethods())
						.filter(method -> method.isAnnotationPresent(CommandHandler.class))
						.map(method -> new CommandProcessor(object, method));
			}
		}
	}



	@ParameterizedTest
	@ValueSource(classes = {
			C1_Classes.CommandA.class,
			C1_Classes.CommandB.class
	})
	void C1_should_call_the_Foo_handle_Command(Class<?> commandType)
			throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		CommandGateway gateway = new CommandGateway();
		gateway.setProcessorsFinder(new C1_Classes.CommandProcessorsFinder());
		C1_Classes.Foo foo = new C1_Classes.Foo();
		gateway.registerProcessors(foo);
		gateway.send((Serializable) commandType.getDeclaredConstructor().newInstance());
		assertThat(foo.getHandledCommandType()).isEqualTo(commandType);
	}

}