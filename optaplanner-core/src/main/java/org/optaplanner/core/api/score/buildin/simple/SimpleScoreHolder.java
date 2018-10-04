/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.api.score.buildin.simple;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;

/**
 * @see SimpleScore
 */
public class SimpleScoreHolder extends AbstractScoreHolder<SimpleScore> {

    protected final Map<Rule, BiConsumer<RuleContext, Integer>> matchExecutorMap = new LinkedHashMap<>();

    protected int score;

    public SimpleScoreHolder(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled, SimpleScore.ZERO);
    }

    public int getScore() {
        return score;
    }

    // ************************************************************************
    // Setup methods
    // ************************************************************************

    @Override
    public void putConstraintWeight(Rule rule, SimpleScore constraintWeight) {
        BiConsumer<RuleContext, Integer> matchExecutor;
        if (constraintWeight.equals(SimpleScore.ZERO)) {
            matchExecutor = (RuleContext kcontext, Integer matchWeight) -> {};
        } else if (constraintWeight.getInitScore() != 0) {
            throw new IllegalStateException("The initScore (" + constraintWeight.getInitScore() + ") must be 0.");
        } else {
            matchExecutor = (RuleContext kcontext, Integer matchWeight)
                    -> addConstraintMatch(kcontext, constraintWeight.getScore() * matchWeight);
        }
        matchExecutorMap.put(rule, matchExecutor);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    /**
     * @param kcontext never null, the magic variable in DRL
     * @param weight higher is better, negative for a penalty, positive for a reward
     */
    public void addConstraintMatch(RuleContext kcontext, int weight) {
        score += weight;
        registerConstraintMatch(kcontext,
                () -> score -= weight,
                () -> SimpleScore.valueOf(weight));
    }

    @Override
    public SimpleScore extractScore(int initScore) {
        return SimpleScore.valueOfUninitialized(initScore, score);
    }

}
