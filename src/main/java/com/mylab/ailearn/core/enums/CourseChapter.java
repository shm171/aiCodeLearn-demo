package com.mylab.ailearn.core.enums;

import java.util.Locale;
import java.util.Objects;

/**
 * 大一程序设计课程章节。用于把学生上传的源码匹配到对应章节，支撑后续薄弱知识点与刷题清单。
 * 章节与关键词是启发性映射，后续可由题库或教材目录替换。
 */
public enum CourseChapter {

    FUNDAMENTALS("CH1", "程序基础与开发环境",
            "stdio", "printf", "cout", "scanf", "cin", "include", "main", "println", "public static void main"),
    DATA_TYPES("CH2", "数据类型与表达式",
            "int", "double", "float", "char", "long", "short", "const", "final", "unsigned", "类型", "表达式", "运算符"),
    CONTROL_FLOW("CH3", "选择与循环结构",
            "if", "else", "switch", "case", "for", "while", "do", "break", "continue"),
    ARRAYS("CH4", "数组与字符串",
            "数组", "array", "[", "]", "string", "字符串", "length", "strlen", "下标"),
    POINTERS("CH5", "指针与引用",
            "指针", "pointer", "&", "*", "new", "delete", "malloc", "free", "引用", "reference"),
    FUNCTIONS("CH6", "函数与递归",
            "函数", "function", "return", "递归", "recursion", "void", "形参", "实参"),
    OOP("CH7", "面向对象初步",
            "class", "public", "private", "protected", "extends", "interface", "implements", "object", "this", "super", "构造"),
    FILE_IO("CH8", "文件与流",
            "fopen", "fclose", "FILE", "ifstream", "ofstream", "fstream", "File", "Scanner", "InputStream", "OutputStream", "文件"),
    COMPREHENSIVE("CH9", "综合练习");

    private final String code;
    private final String title;
    private final String[] keywords;

    CourseChapter(String code, String title, String... keywords) {
        this.code = code;
        this.title = title;
        this.keywords = keywords;
    }

    /** 章节编号，例如 "CH1"。 */
    public String code() {
        return code;
    }

    /** 章节标题，例如 "程序基础与开发环境"。 */
    public String title() {
        return title;
    }

    /** 展示用文案：编号 + 标题，例如 "CH1 程序基础与开发环境"。 */
    public String displayValue() {
        return code + " " + title;
    }

    /**
     * 根据文件名与源码内容做关键词打分，返回得分最高的章节；无任何命中时回退到「综合练习」。
     *
     * <p>打分方式是统计每个关键词在「文件名 + 内容」里出现的次数并累加。若多个章节得分相同，
     * 取枚举声明顺序靠前的一个（即 {@link #FUNDAMENTALS} 优先）；{@link #COMPREHENSIVE}
     * 只作为兜底，不参与打分。</p>
     *
     * @param filename 文件名，可与内容一起提供少量线索
     * @param content  源码内容
     * @return 匹配到的章节，永不返回 null；无命中时为 {@link #COMPREHENSIVE}
     */
    public static CourseChapter match(String filename, String content) {
        String corpus = (Objects.toString(filename, "") + "\n" + Objects.toString(content, "")).toLowerCase(Locale.ROOT);

        CourseChapter best = COMPREHENSIVE;
        int bestScore = 0;
        for (CourseChapter chapter : values()) {
            if (chapter == COMPREHENSIVE) {
                continue;
            }
            int score = score(chapter, corpus);
            if (score > bestScore) {
                bestScore = score;
                best = chapter;
            }
        }
        return best;
    }

    private static int score(CourseChapter chapter, String corpus) {
        int total = 0;
        for (String keyword : chapter.keywords) {
            String kw = keyword.toLowerCase(Locale.ROOT);
            if (kw.isBlank()) {
                continue;
            }
            int idx = corpus.indexOf(kw);
            int hits = 0;
            while (idx >= 0) {
                hits++;
                idx = corpus.indexOf(kw, idx + kw.length());
            }
            total += hits;
        }
        return total;
    }
}