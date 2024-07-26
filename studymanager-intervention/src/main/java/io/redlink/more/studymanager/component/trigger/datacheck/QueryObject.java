/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Elastic License 2.0.
 */
package io.redlink.more.studymanager.component.trigger.datacheck;

import java.util.Set;
import java.util.stream.Collectors;

public class QueryObject {
    public enum GroupCondition {
        or("OR"), and("AND");

        private String value;

        GroupCondition(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public QueryObject() {
    }

    public GroupCondition nextGroupCondition;

    public Set<DataPointQuery> parameter;

    public String toQueryString() {
        return parameter.stream().map(DataPointQuery::toQueryString).collect(Collectors.joining(" AND "));
    }

}
