package aop.aoptest;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class ParameterLoggingAspect {

    @Before("execution(* aop.aoptest.service.*.*(..))")
    public void logParameters(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((CodeSignature) joinPoint.getSignature()).getParameterNames();

        StringBuilder sb = new StringBuilder();
        sb.append(className).append("[");

        for (int i = 0; i < args.length; i++) {
            sb.append(paramNames[i]).append("=")
                    // depth=0으로 시작 (최상위 레벨)
                    .append(convertValueToString(args[i], 0));
            if (i < args.length - 1) {
                sb.append(", ");
            }
        }

        sb.append("]");
        log.info("{}", sb);
    }

    private String convertValueToString(Object value, int depth) {
        if (value == null) {
            return "null";
        }

        // 단순 타입들
        if (value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Character) {
            return value.toString();
        }

        // Collection 처리
        if (value instanceof Collection) {
            Collection<?> col = (Collection<?>) value;
            return "Collection(size=" + col.size() + ")";
        }

        // Map 처리
        if (value instanceof Map) {
            Map<?,?> map = (Map<?,?>) value;
            return "Map(size=" + map.size() + ")";
        }

        // Array 처리
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            return "Array(length=" + length + ")";
        }

        // depth=0일 때만 필드 표시, depth>0일 경우 빈 객체로 표시
        if (depth == 0) {
            return convertObjectToString(value);
        } else {
            // 두 번째 레벨 이상의 객체는 빈 {}만 표시
            return value.getClass().getSimpleName() + "{}";
        }
    }

    // 최상위 레벨 객체만 필드 탐색
    private String convertObjectToString(Object obj) {
        Class<?> clazz = obj.getClass();
        StringBuilder sb = new StringBuilder();
        sb.append(clazz.getSimpleName()).append("{");

        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            sb.append(fields[i].getName()).append("=");
            try {
                Object fieldValue = fields[i].get(obj);
                // 필드값 변환 시 depth=1로 전달 -> 두 번째 레벨 객체는 {}만 표시
                sb.append(convertValueToString(fieldValue, 1));
            } catch (IllegalAccessException e) {
                sb.append("???");
            }
            if (i < fields.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}