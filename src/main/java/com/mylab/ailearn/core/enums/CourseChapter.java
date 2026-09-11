package com.mylab.ailearn.core.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 大一程序设计课程章节。用于把学生上传的源码匹配到对应章节，支撑后续薄弱知识点与刷题清单。
 *
 * <p>章节与关键词是启发性映射，后续可由题库或教材目录替换。</p>
 *
 * <p><b>打分方式</b>：关键词分三档权重（区分度高的 3 分、中等的 2 分、常见的 1 分），
 * 每命中一次记对应分数，但同一个关键词最多计 {@value #MAX_HITS_PER_KEYWORD} 次——
 * 否则 {@code int}、{@code for} 这类随处可见的词会仅凭出现次数把章节带偏。
 * 取总分最高的章节；并列时取枚举声明靠前者；全部为 0 分时回退到 {@link #COMPREHENSIVE}。</p>
 *
 * <p><b>已知局限</b>：这是纯关键词匹配，不做语法分析。例如只写 {@code int a[5]} 而没有
 * 「数组 / array / length / 下标」等字样时，不会被判为数组章节；单字符运算符
 * （{@code [ ] & *}）已刻意不作为关键词，避免它们在几乎所有程序里刷分。
 * 章节只用于聚合展示与错题分组，不参与计分。</p>
 */
public enum CourseChapter {

    FUNDAMENTALS("CH1", "程序基础与开发环境",
            strong("public static void main"),
            normal("stdio", "printf", "cout", "scanf", "cin", "include", "println"),
            weak("main")),
    DATA_TYPES("CH2", "数据类型与表达式",
            normal("类型", "表达式", "运算符", "unsigned", "const", "final"),
            weak("int", "double", "float", "char", "long", "short")),
    CONTROL_FLOW("CH3", "选择与循环结构",
            strong("switch", "case"),
            normal("break", "continue"),
            weak("if", "else", "do", "for", "while")),
    ARRAYS("CH4", "数组与字符串",
            strong("数组", "字符串", "下标", "array", "strlen", "length"),
            normal("string")),
    POINTERS("CH5", "指针与引用",
            strong("指针", "引用", "pointer", "reference", "malloc", "free"),
            normal("new", "delete")),
    FUNCTIONS("CH6", "函数与递归",
            strong("递归", "recursion"),
            normal("函数", "function", "形参", "实参", "void"),
            weak("return")),
    OOP("CH7", "面向对象初步",
            strong("extends", "implements", "interface", "构造"),
            normal("object", "this", "super", "private", "protected"),
            weak("class", "public")),
    FILE_IO("CH8", "文件与流",
            strong("fopen", "fclose", "ifstream", "ofstream", "fstream", "inputstream", "outputstream", "scanner"),
            normal("file", "文件")),
    COMPREHENSIVE("CH9", "综合练习");

    /** 单个关键词最多计入的命中次数，避免常见词靠数量刷分。 */
    private static final int MAX_HITS_PER_KEYWORD = 3;

    /** 一个带权重的关键词。 */
    private record Keyword(String text, int weight) {
    }

    private final String code;
    private final String title;
    private final List<Keyword> keywords;

    CourseChapter(String code, String title, Keyword[]... keywordGroups) {
        this.code = code;
        this.title = title;
        this.keywords = Arrays.stream(keywordGroups)
                .flatMap(Arrays::stream)
                .toList();
    }

    /** 区分度高的关键词：只在对应主题里才会出现。 */
    private static Keyword[] strong(String... words) {
        return group(3, words);
    }

    /** 中等区分度的关键词：对应主题常见，但其它主题也可能出现。 */
    private static Keyword[] normal(String... words) {
        return group(2, words);
    }

    /** 常见关键词：几乎所有程序都有，只作为微弱的倾向信号。 */
    private static Keyword[] weak(String... words) {
        return group(1, words);
    }

    private static Keyword[] group(int weight, String... words) {
        return Arrays.stream(words).map(word -> new Keyword(word, weight)).toArray(Keyword[]::new);
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
     * <p>打分口径见类注释：关键词带权重、单个词计数封顶。若多个章节得分相同，
     * 取枚举声明顺序靠前的一个；{@link #COMPREHENSIVE} 只作为兜底，不参与打分。</p>
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
        for (Keyword keyword : chapter.keywords) {
            String word = keyword.text().toLowerCase(Locale.ROOT);
            if (word.isBlank()) {
                continue;
            }
            int hits = 0;
            int index = corpus.indexOf(word);
            while (index >= 0 && hits < MAX_HITS_PER_KEYWORD) {
                if (isStandalone(corpus, index, word)) {
                    hits++;
                }
                index = corpus.indexOf(word, index + word.length());
            }
            total += hits * keyword.weight();
        }
        return total;
    }

    /**
     * 判断命中处是不是一个独立的词。ASCII 关键词要求前后不是字母、数字或下划线，
     * 否则会把 {@code printf} 里的 {@code int}、{@code before} 里的 {@code for} 算成命中。
     * 含中文的关键词不做边界判断（中文没有词边界，前面的字通常是词的一部分）。
     */
    private static boolean isStandalone(String corpus, int index, String word) {
        if (!isAscii(word)) {
            return true;
        }
        boolean leftOk = index == 0 || !isWordChar(corpus.charAt(index - 1));
        int end = index + word.length();
        boolean rightOk = end >= corpus.length() || !isWordChar(corpus.charAt(end));
        return leftOk && rightOk;
    }

    private static boolean isAscii(String text) {
        return text.chars().allMatch(c -> c < 128);
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}
