/*
 * Copyright 2023 the original author or authors.
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

package org.gradle.problems.internal.transformers;

import org.gradle.api.GradleException;
import org.gradle.api.internal.tasks.execution.ExecuteTaskBuildOperationDetails;
import org.gradle.api.problems.internal.InternalProblem;
import org.gradle.api.problems.internal.InternalProblemBuilder;
import org.gradle.api.problems.internal.Problem;
import org.gradle.internal.operations.BuildOperationAncestryTracker;
import org.gradle.internal.operations.OperationIdentifier;
import org.gradle.problems.internal.OperationListener;
import org.gradle.util.Path;

import java.util.Objects;

public class TaskPathLocationTransformer extends BaseLocationTransformer {

    public TaskPathLocationTransformer(
        BuildOperationAncestryTracker buildOperationAncestryTracker,
        OperationListener buildOperationListenerManager
    ) {
        super(buildOperationAncestryTracker, buildOperationListenerManager);
    }

    @Override
    public Problem transform(InternalProblem problem, OperationIdentifier id) {
        return getExecuteTask(ExecuteTaskBuildOperationDetails.class, id)
            .map(executeOpId -> {
                try {
                    ExecuteTaskBuildOperationDetails executeTaskDetails = operationListener.getOp(executeOpId, ExecuteTaskBuildOperationDetails.class);
                    Objects.requireNonNull(executeTaskDetails, "executeTaskDetails should not be null");
                    Path taskPath = executeTaskDetails.getTask().getIdentityPath();
                    InternalProblemBuilder problemBuilder = problem.toBuilder();
                    return problemBuilder.taskPathLocation(taskPath.getPath()).build();
                } catch (Exception ex) {
                    throw new GradleException("Problem while reporting problem", ex);
                }
            }).orElse(problem);
    }
}
