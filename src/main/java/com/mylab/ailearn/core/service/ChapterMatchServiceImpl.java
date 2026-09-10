package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.model.commonmodel.ParseResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * 题目匹配的默认实现。语言识别、章节匹配等为纯业务逻辑，可独立编译。
 *
 * <p>解析前先做输入预算与文件名校验（{@link SourceInputValidator}）：文件名不合规、
 * 源码超限一律返回 400 受控错误，错误信息不回显原始输入。返回的
 * {@link ParseResult#filename()} 是校验并规范化后的名字，后续环节直接使用它。</p>
 */
@Service
public class ChapterMatchServiceImpl implements ChapterMatchService {

    @Override
    public ParseResult parse(String filename, String content) {
        // 先校验预算：空值、文件名非法、超长源码都在这里变成 400，不进入后续流程
        String safeFilename = SourceInputValidator.requireWithinBudget(filename, content);

        ProgrammingLanguage language = ProgrammingLanguage.detect(safeFilename)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "仅支持 .cpp 或 .java 源码文件"));

        CourseChapter chapter = CourseChapter.match(safeFilename, content);
        return new ParseResult(safeFilename, language, chapter);
    }
}
