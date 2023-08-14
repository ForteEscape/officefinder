package com.dokkebi.officefinder.repository.office;

import com.dokkebi.officefinder.controller.office.dto.OfficeBasicSearchCond;
import com.dokkebi.officefinder.controller.office.dto.OfficeDetailSearchCond;
import com.dokkebi.officefinder.entity.office.Office;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OfficeRepositoryCustom {

  Page<Office> findByDetailCondition(OfficeDetailSearchCond cond, Pageable pageable);

  Page<Office> findByBasicCondition(OfficeBasicSearchCond cond, Pageable pageable);
}