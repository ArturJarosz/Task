package com.arturjarosz.task.project.query;

import com.arturjarosz.task.project.model.Cost;

public interface ProjectQueryService {

    /**
     * Loads Cost by given costId.
     *
     * @param costId
     * @return
     */
    Cost getCostById(Long costId);

}
