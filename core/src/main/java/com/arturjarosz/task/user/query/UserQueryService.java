package com.arturjarosz.task.user.query;

import java.util.Collection;
import java.util.Map;

public interface UserQueryService {

    /**
     * Number of users having each of the given role ids, 0 for roles nobody has.
     */
    Map<Long, Long> getNumberOfUsersByRoleIds(Collection<Long> roleIds);
}
