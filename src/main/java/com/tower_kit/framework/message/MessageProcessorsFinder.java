package com.tower_kit.framework.message;


import io.vavr.collection.List;

/**
 * @author <a href="mailto:zjk@jianzhimao.com">Zhang Jiankun</a>
 * @since 0.1.0
 */
public interface MessageProcessorsFinder {

	List<MessageProcessor> findProcessors(Object object);

}
