package com.senhorcafe.queuecart.order.domain;

import java.util.List;

public record OrderPageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
}
