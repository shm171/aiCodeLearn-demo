package com.mylab.ailearn.core.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 章节匹配的启发式规则：关键词带权重、按词封顶、按词边界匹配。
 *
 * <p>目标是「不要因为常见词出现得多就把章节带偏」，而不是精确分类——
 * 关键词法本身有上限，判不出来时应回退到 {@link CourseChapter#COMPREHENSIVE}。</p>
 */
class CourseChapterTest {

    @Test
    void matchesPointerAssignment() {
        String code = """
                int main() {
                    int *p = new int(3);
                    *p = 5;
                    delete p;
                    return 0;
                }
                """;

        assertThat(CourseChapter.match("main.cpp", code)).isEqualTo(CourseChapter.POINTERS);
    }

    @Test
    void matchesFileIo() {
        String code = """
                import java.util.Scanner;
                import java.io.File;

                public class Main {
                    public static void main(String[] args) throws Exception {
                        Scanner sc = new Scanner(new File("in.txt"));
                        System.out.println(sc.nextInt());
                    }
                }
                """;

        assertThat(CourseChapter.match("Main.java", code)).isEqualTo(CourseChapter.FILE_IO);
    }

    @Test
    void matchesRecursion() {
        // 关键词法靠「递归」这类主题词判定；只有 return 时信号太弱，会退回按其它词打分
        String code = """
                // 递归求阶乘
                int factorial(int n) {
                    if (n <= 1) {
                        return 1;
                    }
                    return n * factorial(n - 1);
                }
                """;

        assertThat(CourseChapter.match("main.cpp", code)).isEqualTo(CourseChapter.FUNCTIONS);
    }

    @Test
    void matchesObjectOriented() {
        String code = """
                public class Student extends Person {
                    private String name;

                    public Student(String name) {
                        this.name = name;
                    }
                }
                """;

        assertThat(CourseChapter.match("Student.java", code)).isEqualTo(CourseChapter.OOP);
    }

    @Test
    void matchesStringHandling() {
        String code = """
                #include <string>
                int main() {
                    std::string s = "abc";
                    std::cout << s.length();
                    return 0;
                }
                """;

        assertThat(CourseChapter.match("main.cpp", code)).isEqualTo(CourseChapter.ARRAYS);
    }

    @Test
    void doesNotMatchKeywordsInsideOtherWords() {
        // "point" 里的 int、"before" 里的 for 都不是关键词命中，不应因此判成数据类型 / 控制流
        assertThat(CourseChapter.match("a.cpp", "point before")).isEqualTo(CourseChapter.COMPREHENSIVE);
    }

    @Test
    void repeatedGenericWordCannotDominate() {
        // int 出现 20 次也只按上限计入，不会压过有区分度的主题词
        String manyInts = "int " + "int a; ".repeat(20) + "std::string s; s.length();";

        assertThat(CourseChapter.match("main.cpp", manyInts)).isEqualTo(CourseChapter.ARRAYS);
    }

    @Test
    void fallsBackToComprehensiveWhenNothingMatches() {
        assertThat(CourseChapter.match("a.cpp", "")).isEqualTo(CourseChapter.COMPREHENSIVE);
        assertThat(CourseChapter.match("a.cpp", "x = y;")).isEqualTo(CourseChapter.COMPREHENSIVE);
    }

    @Test
    void displayValueContainsCodeAndTitle() {
        assertThat(CourseChapter.ARRAYS.displayValue())
                .isEqualTo(CourseChapter.ARRAYS.code() + " " + CourseChapter.ARRAYS.title());
    }
}
