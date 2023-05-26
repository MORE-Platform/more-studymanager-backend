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
        return "(" + parameter.stream().map(DataPointQuery::toQueryString).collect(Collectors.joining(" AND ")) + ")";
    }

}
