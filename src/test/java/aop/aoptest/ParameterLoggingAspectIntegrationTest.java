package aop.aoptest;

import aop.aoptest.service.TestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
class ParameterLoggingAspectIntegrationTest {

    @Autowired
    private TestService testService;

    @Test
    void testAopLogging() {
        testService.doSomething(new TestDto(), new int[] { 1, 2, 3 }, List.of(1, 2, 3), Map.of("key", "value"));
    }

    static class TestDto {
        private String field = "field";
        private TestDto2 testDto = new TestDto2();
    }

    static class TestDto2 {
        private String field = "field";
        private TestDto3 testDto = new TestDto3();
    }

    static class TestDto3 {
        private String field = "field";
    }

}