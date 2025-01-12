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
package org.kathrynhuxtable.spring.uifilter.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.Builder.Default;

/**
 * A somewhat simplified filter data structure, holding the pagination information,
 * the sort information, and filtering criteria.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UIFilter {

	/**
	 * The index of the first record to be loaded.
	 */
	@Default
	Integer first = 0;

	/**
	 * The number of rows to load.
	 */
	@Default
	Integer rows = 0;

	/**
	 * The fields and orders to be used for sorting.
	 */
	@Singular
	List<UIFilterSort> sortFields;

	/**
	 * An object containing filter metadata for filtering the data. The keys
	 * represent the field names, and the values represent the corresponding filter
	 * metadata.
	 */
	@Singular
	Map<String, List<UIFilterData>> filters;

	/**
	 * The name of the attribute supplied for a global search.
	 */
	String globalFieldName;

	public static class UIFilterBuilder {

		public UIFilterBuilder addSortField(String field, int order) {
			return this.sortField(new UIFilterSort(field, order));
		}

		public UIFilterBuilder addFilter(String filterKey, UIFilterData filterValue) {
			if (this.filters$key == null) {
				this.filters$key = new ArrayList<String>();
				this.filters$value = new ArrayList<List<UIFilterData>>();
			}
			if (!this.filters$key.contains(filterKey)) {
				this.filters$key.add(filterKey);
				this.filters$value.add(new ArrayList<>());
			}
			this.filters$value.get(this.filters$key.indexOf(filterKey)).add(filterValue);
			return this;
		}
	}
}
