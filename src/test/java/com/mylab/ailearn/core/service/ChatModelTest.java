package com.mylab.ailearn.core.service;


import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.model.commonmodel.GradedError;
import com.mylab.ailearn.core.model.commonmodel.GradingResult;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 双层批改接真实模型的集成测试（手工观察用）。
 *
 * <p><b>运行前提与风险</b>：这是 {@code @SpringBootTest}，会启动完整 Spring 上下文；
 * 若配置了 {@code DEEPSEEK_APIKEY}，{@code GradingService#grade} 会真实调用模型并把
 * 结果打印到控制台，产生费用；LLM 客户端未接入时退化为仅规则静态检查。</p>
 *
 * <p>断言很少，主要用于人工观察输出，不建议作为回归测试运行。</p>
 */
@SpringBootTest
public class ChatModelTest {
    @Autowired
    private GradingService service;

    @Test
    public void chatmodelTest() {
        SourceFile sourceFile = new SourceFile(null,1L,"main.cpp", ProgrammingLanguage.CPP, CourseChapter.COMPREHENSIVE,"#include <iostream>\n" +
                "using namespace std;\n" +
                "int main() {\n" +
                "   int a = 5 // 缺少分号\n" +
                "   cout << \"a=\" << A << endl; // 变量名大小写错误\n" +
                "   return 0;\n" +
                "}", LocalDateTime.now());


        GradingResult result =service.grade(sourceFile);

        System.out.println(result.errors());
    }

}
