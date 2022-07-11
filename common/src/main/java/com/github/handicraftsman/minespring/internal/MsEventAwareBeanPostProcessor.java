package com.github.handicraftsman.minespring.internal;

import com.github.handicraftsman.minespring.behaviour.MsEventAware;
import com.github.handicraftsman.minespring.behaviour.MsEventHandler;
import dev.architectury.event.Event;
import lombok.Builder;
import lombok.Data;
import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class MsEventAwareBeanPostProcessor implements BeanPostProcessor {
    @Data
    @Builder
    private static class EventHandlerDescriptor {
        private Method method;
        private Event<?> event;
        private Class<?> eventHolder;
        private String fieldName;
    }

    @Data
    @Builder
    private static class EventAwareDescriptor {
        private Object object;
        private Map<String, EventHandlerDescriptor> handlerMap;
    }

    private Map<String, EventAwareDescriptor> eventAwareMap = new HashMap<>();

    private EventAwareDescriptor makeDescriptor(Object object, Class<?> klass) throws NoSuchFieldException, IllegalAccessException {
        Method[] methods = klass.getMethods();
        Map<String, EventHandlerDescriptor> handlerMap = new HashMap<>();
        for (Method method : methods) {
            if (method.isAnnotationPresent(MsEventHandler.class)) {
                MsEventHandler annotation = method.getAnnotation(MsEventHandler.class);
                Field eventField = annotation.klass().getField(annotation.event());
                if (!Event.class.isAssignableFrom(eventField.getType())) {
                    throw new RuntimeException(
                            String.format("%s does not refer to a correct event", method)
                    );
                }
                Event<?> event = (Event<?>) eventField.get(null);
                handlerMap.put(method.getName(),
                    EventHandlerDescriptor.builder()
                            .method(method)
                            .event(event)
                            .eventHolder(annotation.klass())
                            .fieldName(annotation.event())
                            .build());
            }
        }
        return EventAwareDescriptor.builder()
                .object(object)
                .handlerMap(handlerMap)
                .build();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> klass = bean.getClass();
        if (klass.isAnnotationPresent(MsEventAware.class)) {
            try {
                eventAwareMap.put(beanName, makeDescriptor(bean, klass));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        val descriptor = eventAwareMap.get(beanName);

        descriptor.handlerMap.forEach((String methodName, EventHandlerDescriptor handlerDescriptor) -> {
            Event<?> event = handlerDescriptor.getEvent();
            val interfaces = event.invoker().getClass().getInterfaces();
            if (interfaces.length != 1) {
                throw new RuntimeException(
                    String.format(
                        "Event %s.%s invoker does not implement exactly 1 interface, screaming at %s.%s (bean %s)",
                        handlerDescriptor.getEventHolder().toString(),
                        handlerDescriptor.getFieldName(),
                        descriptor.getObject().getClass().toString(),
                        methodName,
                        beanName
                    )
                );
            }
            val interfaceMethods = interfaces[0].getMethods();
            if (interfaceMethods.length != 1) {
                throw new RuntimeException(
                    String.format(
                        "Interface implemented by %s.%s invoker is not a functional interface, screaming at %s.%s (bean %s)",
                        handlerDescriptor.getEventHolder().toString(),
                        handlerDescriptor.getFieldName(),
                        descriptor.getObject().getClass().toString(),
                        methodName,
                        beanName
                    )
                );
            }
            if (!Arrays.equals(
                interfaceMethods[0].getParameters(),
                handlerDescriptor.getMethod().getParameters(),
                (a, b) -> a.getType().equals(b.getType()) ? 0 : 1)) {
                throw new RuntimeException(
                    String.format(
                        "Interface implemented by %s.%s invoker does not have a method with same arguments as %s.%s has (bean %s)",
                        handlerDescriptor.getEventHolder().toString(),
                        handlerDescriptor.getFieldName(),
                        descriptor.getObject().getClass().toString(),
                        methodName,
                        beanName
                    )
                );
            }
            val proxy = Proxy.newProxyInstance(
                bean.getClass().getClassLoader(),
                event.invoker().getClass().getInterfaces(),
                (_proxy, method, args) -> handlerDescriptor.getMethod().invoke(descriptor.getObject(), args)
            );
            try {
                val registerMethod = Event.class.getDeclaredMethod("register", new Class[] { Object.class });
                registerMethod.invoke(event, proxy);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(e);
            }
        });

        return bean;
    }
}
