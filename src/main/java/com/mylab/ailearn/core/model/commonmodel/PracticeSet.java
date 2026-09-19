package com.mylab.ailearn.core.model.commonmodel;

import java.util.List;

/** AI 生成的类似练习集合。 */
public record PracticeSet(List<PracticeExercise> exercises) {
    public PracticeSet {
        exercises = exercises == null ? List.of() : List.copyOf(exercises);
    }
}
