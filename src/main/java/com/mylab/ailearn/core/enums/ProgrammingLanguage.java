package com.mylab.ailearn.core.enums;

import java.util.Locale;
import java.util.Optional;

/**
 * 支持的源码文件类型。当前只接收大一课程常见的 C++（.cpp）与 Java（.java）。
 */
public enum ProgrammingLanguage {

    CPP("C++", ".cpp"),
    JAVA("Java", ".java");

    private final String displayName;
    private final String extension;

    ProgrammingLanguage(String displayName, String extension) {
        this.displayName = displayName;
        this.extension = extension;
    }

    public String displayName() {
        return displayName;
    }

    public String extension() {
        return extension;
    }

    /**
     * 根据文件名后缀识别语言，无法识别时返回 {@link Optional#empty()}。
     */
    public static Optional<ProgrammingLanguage> detect(String filename) {
        if (filename == null || filename.isBlank()) {
            return Optional.empty();
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        for (ProgrammingLanguage language : values()) {
            if (lower.endsWith(language.extension)) {
                return Optional.of(language);
            }
        }
        return Optional.empty();
    }
}