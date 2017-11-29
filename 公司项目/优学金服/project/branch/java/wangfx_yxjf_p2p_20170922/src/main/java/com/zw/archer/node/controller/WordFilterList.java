package com.zw.archer.node.controller;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.zw.archer.common.controller.EntityQuery;
import com.zw.archer.node.model.WordFilter;
import com.zw.core.annotations.Logger;
import com.zw.core.annotations.ScopeType;

@Component
@Scope(ScopeType.REQUEST)
public class WordFilterList extends EntityQuery<WordFilter> {
	@Logger
	static Log log;

	public WordFilterList() {
		final String[] RESTRICTIONS = { "id like #{wordFilterList.example.id}",
				"word like #{wordFilterList.example.word}" };
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
	}

	private List<WordFilter> wordFilters;

	public List<WordFilter> getLanguages() {
		if (wordFilters == null) {
			wordFilters = getHt().findByNamedQuery(
					"WordFilter.findAllWordFilter");
		}
		return wordFilters;
	}
}
