package com.mylab.ailearn.core.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

/**
 * 单份源码的输入预算与文件名校验。
 *
 * <p>在文件解析与批改入口统一执行，把「输入不合法」明确成 400 受控错误，
 * 与「基础设施故障」（存储端口未实现、模型调用失败等，仍为服务端错误）区分开。
 * 错误信息只说明规则，<b>不回显原始输入</b>，也不携带堆栈。</p>
 *
 * <p><b>文件名规则：</b>去掉首尾空白后不超过 {@link #MAX_FILENAME_LENGTH} 个字符，
 * 且只能包含字母、数字、下划线、连字符与点——因此不含路径分隔符、控制字符与空白，
 * 校验通过的名字可以直接作为内部名称使用，不需要再拼接路径。</p>
 *
 * <p><b>源码预算：</b>不超过 {@link #MAX_SOURCE_BYTES} 字节（UTF-8）且不超过
 * {@link #MAX_SOURCE_LINES} 行。</p>
 *
 * <p>这些预算只约束「进入业务层的单份源码」；Web 层的请求体与上传大小限制是另一层，
 * 需要按实际部署方式另行配置，不能由本类代替。</p>
 */
public final class SourceInputValidator {

    /** 文件名最大长度（字符）。 */
    public static final int MAX_FILENAME_LENGTH = 128;

    /** 单份源码最大字节数（UTF-8）：256 KiB。 */
    public static final int MAX_SOURCE_BYTES = 256 * 1024;

    /** 单份源码最大行数。 */
    public static final int MAX_SOURCE_LINES = 5000;

    private SourceInputValidator() {
    }

    /**
     * 校验文件名与源码是否在预算内。
     *
     * @return 规范化（去掉首尾空白）后的文件名，可安全用作内部名称
     * @throws ResponseStatusException 400：文件名或源码为空、文件名不合规、源码超出预算
     */
    public static String requireWithinBudget(String filename, String content) {
        String safeFilename = normalizeFilename(filename);
        requireContentWithinBudget(content);
        return safeFilename;
    }

    /**
     * 规范化文件名：去掉首尾空白并校验字符规则。
     *
     * @throws ResponseStatusException 400：文件名为空、过长或含不允许的字符
     */
    public static String normalizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw badRequest("文件名不能为空");
        }
        String name = filename.strip();
        if (name.length() > MAX_FILENAME_LENGTH) {
            throw badRequest("文件名过长，最多 " + MAX_FILENAME_LENGTH + " 个字符");
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-' && c != '.') {
                throw badRequest("文件名只能包含字母、数字、下划线、连字符和点，不能包含路径分隔符或空白");
            }
        }
        return name;
    }

    /**
     * 校验源码体积与行数。
     *
     * @throws ResponseStatusException 400：源码为空、字节数或行数超出预算
     */
    public static void requireContentWithinBudget(String content) {
        if (content == null || content.isBlank()) {
            throw badRequest("源码内容不能为空");
        }
        // 字符数不会大于字节数：先用字符数做零分配的快速拒绝，再算精确字节数
        if (content.length() > MAX_SOURCE_BYTES
                || content.getBytes(StandardCharsets.UTF_8).length > MAX_SOURCE_BYTES) {
            throw badRequest("源码过大，最多 " + (MAX_SOURCE_BYTES / 1024) + " KB");
        }
        if (countLines(content) > MAX_SOURCE_LINES) {
            throw badRequest("源码行数过多，最多 " + MAX_SOURCE_LINES + " 行");
        }
    }

    /**
     * 统计源码行数，口径与批改结果中的行号一致（以 {@code '\n'} 分行，无换行时算 1 行）。
     */
    public static int countLines(String content) {
        if (content == null || content.isEmpty()) {
            return 1;
        }
        int lines = 1;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                lines++;
            }
        }
        return lines;
    }

    private static ResponseStatusException badRequest(String reason) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }
}
