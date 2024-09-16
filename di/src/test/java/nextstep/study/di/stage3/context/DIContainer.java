package nextstep.study.di.stage3.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 스프링의 BeanFactory, ApplicationContext에 해당되는 클래스
 */
class DIContainer {

    private final Set<Object> beans;

    public DIContainer(final Set<Class<?>> classes) {
        this.beans = createBeans(classes);
        for (Object bean : this.beans) {
            setFields(bean);
        }
    }

    private Set<Object> createBeans(final Set<Class<?>> classes) {
        return classes.stream()
            .map(this::instantiate)
            .collect(Collectors.toCollection(HashSet::new));
    }

    private Object instantiate(final Class<?> aClass) {
        try {
            final Constructor<?> constructor = aClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("instantiate failed: " + aClass.getName(), e.getCause());
        }
    }

    private void setFields(final Object bean) {
        for (final Object element : beans) {
            final Field[] fields = element.getClass().getDeclaredFields();
            for (final Field field : fields) {
                setField(field, element, bean);
            }
        }
    }

    private void setField(final Field field, final Object element, final Object bean) {
        try {
            if (!isFieldInjectable(field, bean)) {
                return;
            }
            field.setAccessible(true);
            field.set(element, bean);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("set field failed");
        }
    }

    private boolean isFieldInjectable(final Field field, final Object bean) throws
        IllegalAccessException {
        return field.getType().isInstance(bean);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(final Class<T> aClass) {
        for (final Object bean : beans) {
            if (bean.getClass().isAssignableFrom(aClass)) {
                return (T)bean;
            }
        }
        throw new IllegalArgumentException("get bean failed");
    }
}
