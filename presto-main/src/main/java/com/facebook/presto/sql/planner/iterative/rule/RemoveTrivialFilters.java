/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.sql.planner.iterative.rule;

import com.facebook.presto.matching.Captures;
import com.facebook.presto.matching.Pattern;
import com.facebook.presto.spi.plan.FilterNode;
import com.facebook.presto.spi.plan.ValuesNode;
import com.facebook.presto.spi.relation.RowExpression;
import com.facebook.presto.sql.planner.iterative.Rule;
import com.google.common.collect.ImmutableList;

import java.util.Optional;

import static com.facebook.presto.expressions.LogicalRowExpressions.FALSE_CONSTANT;
import static com.facebook.presto.expressions.LogicalRowExpressions.TRUE_CONSTANT;
import static com.facebook.presto.sql.planner.plan.Patterns.filter;

public class RemoveTrivialFilters
        implements Rule<FilterNode>
{
    private static final Pattern<FilterNode> PATTERN = filter();

    @Override
    public Pattern<FilterNode> getPattern()
    {
        return PATTERN;
    }

    @Override
    public Result apply(FilterNode filterNode, Captures captures, Context context)
    {
        RowExpression predicate = filterNode.getPredicate();

        if (predicate.equals(TRUE_CONSTANT)) {
            // 直接将 filterNode 的子节点上提
            return Result.ofPlanNode(filterNode.getSource());
        }

        if (predicate.equals(FALSE_CONSTANT)) {
            // 裁剪掉整个 filterNode, 并用一个空的 ValuesNode 替代
            return Result.ofPlanNode(new ValuesNode(filterNode.getSourceLocation(), context.getIdAllocator().getNextId(), filterNode.getOutputVariables(), ImmutableList.of(), Optional.empty()));
        }

        return Result.empty();
    }
}
