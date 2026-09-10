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
