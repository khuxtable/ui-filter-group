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

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;

import org.kathrynhuxtable.spring.uifilter.beans.UIFilter;


/**
 * Provide common methods for the UIFilterDescriptor annotation.
 * <p>
 * Defines the DescriptorMap, specifying how filter fields are mapped to domain fields.
 * </p>
 */
public interface UIFilterService<T> {

	/**
	 * Return the number of rows matched by filter criteria without paginating.
	 * This is needed for a UI to know how many pages are available.
	 *
	 * @param filter the UIFilter object.
	 * @param dao    the associated DAO object.
	 * @return the number of rows matched by the filter criteria.
	 */
	long countByFilter(@NonNull UIFilter filter,
	                   @NonNull JpaSpecificationExecutor<T> dao);

	/**
	 * Find by filter. Supports pagination, sorting, and filtering on values.
	 *
	 * @param filter       the UIFilter object.
	 * @param defaultField optional default field to sort by.
	 * @param dao          the associated DAO object.
	 * @return a List of matching domain records.
	 */
	List<T> findByFilter(@NonNull UIFilter filter,
	                     String defaultField,
	                     @NonNull JpaSpecificationExecutor<T> dao);

	/**
	 * Build a JPA sort.
	 *
	 * @param filter       the UIFilter object.
	 * @param defaultField optional default field to sort by.
	 * @return a Sort object representing the requested sort order.
	 */
	Sort buildSort(@NonNull UIFilter filter,
	               String defaultField);
}
