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

import java.io.Serial;
import java.util.*;

import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import org.kathrynhuxtable.spring.uifilter.beans.UIFilter;
import org.kathrynhuxtable.spring.uifilter.beans.UIFilterData;
import org.kathrynhuxtable.spring.uifilter.beans.UIFilterMatchMode;
import org.kathrynhuxtable.spring.uifilter.beans.UIFilterOperator;


/**
 * Specification class to process the UIFilter object and produce a JPA predicate.
 */
@Slf4j
public class FilterSpecification<T> implements Specification<T> {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * The filter for which to generate a predicate.
	 */
	private final UIFilter filter;

	/**
	 * A Set of attributes to be included in global searches.
	 */
	private final Set<String> globalAttributes = new HashSet<>();

	/**
	 * Construct a FilterSpecification, which constructs a JPA Predicate matching a UIFilter.
	 *
	 * @param filter the UIFilter from the UI.
	 */
	public FilterSpecification(@NonNull UIFilter filter) {
		this.filter = filter;
	}

	public FilterSpecification clearGlobalAttributes() {
		globalAttributes.clear();
		return this;
	}

	public FilterSpecification setGlobalAttributes(List<String> globalAttributes) {
		this.globalAttributes.clear();
		this.globalAttributes.addAll(globalAttributes);
		return this;
	}

	public FilterSpecification setGlobalAttributes(String... attributes) {
		return this.setGlobalAttributes(Arrays.asList(attributes));
	}

	public FilterSpecification addGlobalAttribute(String attribute) {
		globalAttributes.add(attribute);
		return this;
	}

	@Override
	public Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaQuery<?> cq, @NonNull CriteriaBuilder cb) {
		if (filter.getFilters() == null) {
			return null;
		}

		Set<Root<?>> queryRoots = cq.getRoots();

		List<Predicate> outer = filter.getFilters().entrySet().stream()
				.map(entry ->
						buildFieldPredicate(queryRoots, cb, filter.getGlobalFieldName(), entry.getKey(), entry.getValue()))
				.filter(Objects::nonNull)
				.toList();

		return outer.isEmpty() ? null : cb.and(outer.toArray(new Predicate[0]));
	}

	private Predicate buildFieldPredicate(Set<Root<?>> queryRoots, CriteriaBuilder cb, String globalFieldName, String property,
	                                      List<UIFilterData> filters) {
		List<Predicate> inner = new ArrayList<>();
		for (UIFilterData filterData : filters) {
			if (property.equals(globalFieldName)) {
				if (!globalAttributes.isEmpty()) {
					inner.add(buildGlobalPredicate(queryRoots, cb, filterData));
				}
			} else {
				inner.add(buildSimplePredicate(queryRoots, cb, property, filterData));
			}
		}

		if (inner.isEmpty()) {
			return null;
		} else {
			UIFilterOperator operator = filters.stream()
					.map(UIFilterData::getOperator)
					.filter(Objects::nonNull)
					.findFirst()
					.orElse(UIFilterOperator.or);

			return switch (operator) {
				case and -> cb.and(inner.toArray(new Predicate[0]));
				case or -> cb.or(inner.toArray(new Predicate[0]));
			};
		}
	}

	private Predicate buildGlobalPredicate(Set<Root<?>> queryRoots, CriteriaBuilder cb, UIFilterData filterData) {
		List<Predicate> globals = globalAttributes.stream()
				.map(attr -> buildSimplePredicate(queryRoots, cb, attr, filterData))
				.toList();
		return cb.or(globals.toArray(new Predicate[0]));
	}

	private Predicate buildSimplePredicate(Set<Root<?>> queryRoots, CriteriaBuilder cb,
	                                       String attributeName, UIFilterData filterData) {
		Path<?> path = getFieldPath(queryRoots, attributeName);
		Class<?> javaType = path.getJavaType();

		UIFilterMatchMode matchMode = filterData.getMatchMode();
		if (matchMode == null) {
			matchMode = javaType == String.class ? UIFilterMatchMode.contains : UIFilterMatchMode.equals;
		}

		if (javaType == String.class) {
			return buildStringPredicate(
					cb,
					matchMode,
					(Path<String>) path,
					((String) filterData.getValue()).toLowerCase());
		} else if (Comparable.class.isAssignableFrom(javaType)) {
			return getComparablePredicate(
					cb,
					matchMode,
					(Path<Comparable>) path,
					filterData.getValue());
		} else {
			return getObjectPredicate(
					cb,
					matchMode,
					path,
					filterData.getValue());
		}
	}

	private Predicate buildStringPredicate(CriteriaBuilder cb, UIFilterMatchMode matchMode,
	                                       Expression<String> fieldExpression, Object value) {
		return switch (matchMode) {
			case between -> cb.between(
					cb.lower(fieldExpression),
					((List<String>) value).get(0).toLowerCase(),
					((List<String>) value).get(1).toLowerCase());
			case contains -> cb.like(cb.lower(fieldExpression), "%" + ((String) value).toLowerCase() + "%");
			case endsWith -> cb.like(cb.lower(fieldExpression), "%" + ((String) value).toLowerCase());
			case equals -> cb.equal(cb.lower(fieldExpression), ((String) value).toLowerCase());
			case gt -> cb.greaterThan(cb.lower(fieldExpression), ((String) value).toLowerCase());
			case gte -> cb.greaterThanOrEqualTo(cb.lower(fieldExpression), ((String) value).toLowerCase());
			case in -> cb.lower(fieldExpression).in((List<?>) value);
			case lt -> cb.lessThan(cb.lower(fieldExpression), ((String) value).toLowerCase());
			case lte -> cb.lessThanOrEqualTo(cb.lower(fieldExpression), ((String) value).toLowerCase());
			case notContains -> cb.notLike(cb.lower(fieldExpression), "%" + ((String) value).toLowerCase() + "%");
			case notEquals -> cb.notEqual(cb.lower(fieldExpression), ((String) value).toLowerCase());
			case startsWith -> cb.like(cb.lower(fieldExpression), ((String) value).toLowerCase() + "%");
			default -> throw new RuntimeException("Invalid matchmode: " + matchMode);
		};
	}

	private <FT extends Comparable<FT>> Predicate getComparablePredicate(CriteriaBuilder cb, UIFilterMatchMode matchMode,
	                                                                     Expression<FT> fieldExpression, Object value) {
		return switch (matchMode) {
			case between -> cb.between(fieldExpression, ((List<FT>) value).get(0), ((List<FT>) value).get(1));
			case equals -> cb.equal(fieldExpression, value);
			case gt -> cb.greaterThan(fieldExpression, (FT) value);
			case gte -> cb.greaterThanOrEqualTo(fieldExpression, (FT) value);
			case in -> fieldExpression.in((List<FT>) value);
			case lt -> cb.lessThan(fieldExpression, (FT) value);
			case lte -> cb.lessThanOrEqualTo(fieldExpression, (FT) value);
			case notEquals -> cb.notEqual(fieldExpression, value);
			default -> throw new RuntimeException("Invalid matchmode: " + matchMode);
		};
	}

	private Predicate getObjectPredicate(CriteriaBuilder cb, UIFilterMatchMode matchMode,
	                                     Expression<?> fieldExpression, Object value) {
		return switch (matchMode) {
			case equals -> cb.equal(fieldExpression, value);
			case in -> fieldExpression.in((List<?>) value);
			case notEquals -> cb.notEqual(fieldExpression, value);
			default -> throw new RuntimeException("Invalid matchmode: " + matchMode);
		};
	}

	private Path<?> getFieldPath(Set<Root<?>> queryRoots, String fieldName) {
		// Need to handle joins.
		return queryRoots.stream()
				.map(root -> {
					try {
						return root.get(fieldName);
					} catch (IllegalArgumentException e) {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}
}
