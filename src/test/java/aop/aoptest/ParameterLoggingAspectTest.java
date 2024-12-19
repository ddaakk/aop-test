package aop.aoptest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
class ParameterLoggingAspectTest {

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private CodeSignature codeSignature;

    @InjectMocks
    private ParameterLoggingAspect aspect;

    @BeforeEach
    void setUp() {
        when(joinPoint.getTarget()).thenReturn(new TestService());
        when(joinPoint.getSignature()).thenReturn(codeSignature);
    }

    @Test
    void logParameters_simpleTypes(CapturedOutput output) {
        when(codeSignature.getParameterNames()).thenReturn(new String[]{"id", "name"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{123, "Hello"});

        aspect.logParameters(joinPoint);

        String logged = output.getOut().trim();
        // 로그 전체를 비교하지 않고, 메시지 포함 여부 확인
        assertTrue(logged.contains("TestService[id=123, name=Hello]"));
    }

    @Test
    void logParameters_collectionAndMap(CapturedOutput output) {
        when(codeSignature.getParameterNames()).thenReturn(new String[]{"list", "map"});
        List<String> list = Arrays.asList("A", "B", "C");
        Map<String, Integer> map = new HashMap<>();
        map.put("key", 1);
        when(joinPoint.getArgs()).thenReturn(new Object[]{list, map});

        aspect.logParameters(joinPoint);

        String logged = output.getOut().trim();
        assertTrue(logged.contains("TestService[list=Collection(size=3), map=Map(size=1)]"));
    }

    @Test
    void logParameters_array(CapturedOutput output) {
        when(codeSignature.getParameterNames()).thenReturn(new String[]{"arr"});
        String[] arr = {"X", "Y"};
        when(joinPoint.getArgs()).thenReturn(new Object[]{arr});

        aspect.logParameters(joinPoint);

        String logged = output.getOut().trim();
        assertTrue(logged.contains("TestService[arr=Array(length=2)]"));
    }

    @Test
    void logParameters_customObject(CapturedOutput output) {
        when(codeSignature.getParameterNames()).thenReturn(new String[]{"person"});
        Person person = new Person("John", 30);
        when(joinPoint.getArgs()).thenReturn(new Object[]{person});

        aspect.logParameters(joinPoint);
        String logged = output.getOut().trim();

        assertTrue(logged.contains("TestService[person=Person{name=John, age=30}]"));
    }

    @Test
    void logParameters_nestedObject(CapturedOutput output) {
        when(codeSignature.getParameterNames()).thenReturn(new String[]{"userInfo", "arr", "col", "mp"});

        UserInfo dto = new UserInfo();
        int[] arr = {1, 2, 3};
        List<Integer> list = List.of(1, 2, 3);
        Map<String, String> map = Map.of("key", "value");

        when(joinPoint.getArgs()).thenReturn(new Object[]{dto, arr, list, map});

        aspect.logParameters(joinPoint);
        String logged = output.getOut().trim();

        System.out.println(logged);
        assertTrue(logged.contains("TestService[userInfo=UserInfo{name=field, details=UserDetails{}}, arr=Array(length=3), col=Collection(size=3), mp=Map(size=1)]"));
    }

    static class TestService {
    }

    static class Person {
        private String name;
        private Integer age;

        public Person(String name, Integer age) {
            this.age = age;
            this.name = name;
        }
    }

    static class UserInfo {
        private String name = "field";
        private UserDetails details = new UserDetails();
    }

    static class UserDetails {
        private String description = "field";
        private UserMoreDetails moreDetails = new UserMoreDetails();
    }

    static class UserMoreDetails {
        private String value = "field";
    }
}