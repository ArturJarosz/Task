package com.arturjarosz.task.user.query.impl;

import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.arturjarosz.task.user.model.QUser;
import com.arturjarosz.task.user.query.UserQueryService;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Finder
public class UserQueryServiceImpl extends AbstractQueryService<QUser> implements UserQueryService {

    private static final QUser USER = QUser.user;
    private static final NumberPath<Long> ROLE_ID = Expressions.numberPath(Long.class, "roleId");

    public UserQueryServiceImpl() {
        super(USER);
    }

    @Override
    public Map<Long, Long> getNumberOfUsersByRoleIds(Collection<Long> roleIds) {
        Map<Long, Long> countsByRoleId = new HashMap<>();
        roleIds.stream().filter(Objects::nonNull).forEach(id -> countsByRoleId.put(id, 0L));
        if (countsByRoleId.isEmpty()) {
            return countsByRoleId;
        }
        List<Tuple> counts = this.query()
                .select(ROLE_ID, USER.id.count())
                .from(USER)
                .join(USER.roleIds, ROLE_ID)
                .where(ROLE_ID.in(countsByRoleId.keySet()))
                .groupBy(ROLE_ID)
                .fetch();
        counts.forEach(tuple -> countsByRoleId.put(tuple.get(ROLE_ID), tuple.get(USER.id.count())));
        return countsByRoleId;
    }
}
