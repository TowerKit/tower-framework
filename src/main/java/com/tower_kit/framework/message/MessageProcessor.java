package com.tower_kit.framework.message;

import io.vavr.control.Try;

import java.io.Serializable;

/**
 * @author <a href="mailto:zjk@jianzhimao.com">Zhang Jiankun</a>
 * @since 0.1.0
 */
public interface MessageProcessor {

	<I extends Serializable> Class<I> getSupportedMessageType();

	Object getTarget();

	<I extends Serializable, O extends Serializable> Try<O> process(I message);

}
