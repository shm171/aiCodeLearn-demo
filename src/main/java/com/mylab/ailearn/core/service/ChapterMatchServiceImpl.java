package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.model.commonmodel.ParseResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * 题目匹配的默认实现。语言识别、章节匹配等为纯业务逻辑，可独立编译。
 */
@Service
public class ChapterMatchServiceImpl implements ChapterMatchService {

    @Override
    public ParseResult parse(String filename, String content) {
        if (filename == null || filename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文件名不能为空");
        }
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "源码内容不能为空");
        }

        ProgrammingLanguage language = ProgrammingLanguage.detect(filename)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "仅支持 .cpp 或 .java 源码文件，收到：" + filename));

        CourseChapter chapter = CourseChapter.match(filename, content);
        return new ParseResult(filename, language, chapter);
    }
}
