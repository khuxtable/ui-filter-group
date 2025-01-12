/*
 * Copyright 2025 the original author or authors.
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

import org.junit.Test;

import static org.junit.Assert.*;

public class TestFilter {

	@Test
	public void testFilter() {
		UIFilter filter = UIFilter.builder()
				.first(10)
				.addSortField("foo", 1)
				.addSortField("bar", -1)
				.addFilter("state", UIFilterData.builder()
						.value("Massachusetts")
						.build())
				.addFilter("state", UIFilterData.builder()
						.value("Connecticut")
						.build())
				.addFilter("state", UIFilterData.builder()
						.value("Rhode Island")
						.build())
				.addFilter("age", UIFilterData.builder()
						.value(21)
						.matchMode(UIFilterMatchMode.lt)
						.build())
				.addFilter("name", UIFilterData.builder()
						.value("James")
						.matchMode(UIFilterMatchMode.startsWith)
						.operator(UIFilterOperator.and)
						.build())
				.addFilter("name", UIFilterData.builder()
						.value("Morrison")
						.matchMode(UIFilterMatchMode.endsWith)
						.operator(UIFilterOperator.and)
						.build())
				.build();
		assertNotNull(filter.getFirst());
		assertEquals(10, filter.getFirst().intValue());
		assertNotNull(filter.getSortFields());
		assertEquals(2, filter.getSortFields().size());
		assertNotNull(filter.getFilters());
		assertEquals(3, filter.getFilters().size());
		assertTrue(filter.getFilters().containsKey("state"));
		assertEquals(3, filter.getFilters().get("state").size());
		assertEquals("Massachusetts", filter.getFilters().get("state").get(0).getValue());
		assertEquals("Connecticut", filter.getFilters().get("state").get(1).getValue());
		assertEquals("Rhode Island", filter.getFilters().get("state").get(2).getValue());
		assertTrue(filter.getFilters().containsKey("age"));
		assertEquals(1, filter.getFilters().get("age").size());
		assertEquals(21, filter.getFilters().get("age").get(0).getValue());
		assertTrue(filter.getFilters().containsKey("name"));
		assertEquals(2, filter.getFilters().get("name").size());
		assertEquals("James", filter.getFilters().get("name").get(0).getValue());
		assertEquals("Morrison", filter.getFilters().get("name").get(1).getValue());
	}
}
