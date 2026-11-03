package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.sharedkernel.TotalCount
import org.springframework.http.ResponseEntity

// The number of items of the whole list, in the answer that gives a page of it: the body stays the array of the
// page, so that the clients that read the whole list still work
const val TOTAL_COUNT_HEADER = "X-Total-Count"

fun <T> pagedBody(page: List<T>, total: TotalCount): ResponseEntity<List<T>> =
    ResponseEntity.ok().header(TOTAL_COUNT_HEADER, total.value.toString()).body(page)
