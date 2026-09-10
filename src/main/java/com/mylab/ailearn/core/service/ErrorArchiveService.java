package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;

import java.util.List;

/**
 * 错题归档：查询某学生已归档的全部错题，并支持把错题标记为「已掌握」。
 *
 * <p>供学生复习与教师看板读取使用。</p>
 */
public interface ErrorArchiveService {

    /**
     * 查询某学生的全部错题。
     *
     * @param ownerUserId 学生用户 ID
     * @return 该学生的错题列表（无错题时返回空列表，不会为 null）
     * @throws org.springframework.web.server.ResponseStatusException ownerUserId 无效时抛 400
     */
    List<ErrorRecord> listByOwner(Long ownerUserId);

    /**
     * 把某学生的一条错题标记为「已掌握」。
     *
     * @param ownerUserId 学生用户 ID
     * @param errorId     错题记录 ID
     * @return 更新后的错题记录
     * @throws org.springframework.web.server.ResponseStatusException 错题不存在或不属于该学生（404）、参数无效（400）时抛出
     */
    ErrorRecord markMastered(Long ownerUserId, Long errorId);
}
