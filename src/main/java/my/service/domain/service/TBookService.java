package my.service.domain.service;

import java.util.List;

import my.service.domain.model.TBook;
import my.service.dto.RestBookSearchCondition;

public interface TBookService {
    public List<TBook> getBooks(RestBookSearchCondition condition);

    public void registBook(TBook book);

    public void deleteBook(String userId, Integer seqNo);

    public void batchBook(String userId, List<TBook> bookList);
}
