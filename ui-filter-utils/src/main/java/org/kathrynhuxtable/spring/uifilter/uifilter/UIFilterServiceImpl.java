/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kathrynhuxtable.spring.uifilter.uifilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import org.kathrynhuxtable.spring.uifilter.beans.UIFilter;
import org.kathrynhuxtable.spring.uifilter.beans.UIFilterSort;

@Component
public class UIFilterServiceImpl<T> implements UIFilterService<T> {

	@Override
	public long countByFilter(@NonNull UIFilter filter,
	                          @NonNull JpaSpecificationExecutor<T> dao) {
		return dao.count(new FilterSpecification<>(filter).setGlobalAttributes(Arrays.asList("name", "power", "alterEgo")));
	}

	@Override
	public List<T> findByFilter(@NonNull UIFilter filter,
	                            String defaultField,
	                            @NonNull JpaSpecificationExecutor<T> dao) {
		// Create JPA sort criteria.
		Sort sort = buildSort(filter, defaultField);

		// Create the filter predicate.
		FilterSpecification<T> filterSpecification = new FilterSpecification<>(filter);
		filterSpecification.setGlobalAttributes(Arrays.asList("name", "power", "alterEgo"));

		// Find the rows, paginating if requested.
		if (filter.getRows() == null || filter.getRows() == 0) {
			return dao.findAll(filterSpecification, sort);
		} else {
			int rows = filter.getRows();
			int first = filter.getFirst() == null ? 0 : filter.getFirst();
			int page = first / rows;
			Page<T> pageable = dao.findAll(filterSpecification, PageRequest.of(page, rows, sort));
			return pageable.getContent();
		}
	}

	@Override
	public Sort buildSort(@NonNull UIFilter filter,
	                      String defaultField) {
		// Create JPA sort criteria. Sort on id by default.
		if (filter.getSortFields() == null || filter.getSortFields().isEmpty()) {
			if (defaultField != null && !defaultField.isBlank()) {
				return Sort.by(Direction.ASC, defaultField);
			} else {
				return Sort.unsorted();
			}
		} else {
			List<Order> orders = new ArrayList<>();
			for (UIFilterSort sortField : filter.getSortFields()) {
				Direction direction = sortField.getOrder() == null || sortField.getOrder() > 0 ? Direction.ASC : Direction.DESC;
				orders.add(new Order(direction, sortField.getField()));
			}
			return Sort.by(orders);
		}
	}
}
