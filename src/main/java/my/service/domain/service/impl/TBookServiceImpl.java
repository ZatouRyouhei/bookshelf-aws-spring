package my.service.domain.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.service.domain.model.TBook;
import my.service.domain.service.TBookService;
import my.service.dto.RestBookSearchCondition;
import my.service.repository.TBookMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class TBookServiceImpl implements TBookService {

    private final TBookMapper mapper;

    @Override
    public List<TBook> getBooks(RestBookSearchCondition condition) {
        List<TBook> bookList = mapper.findMany(condition);
        return bookList;
    }

    @Override
    public void registBook(TBook book) {
        if (book.getSeqNo() == 0) {
            // 連番が0（新規登録）の時は連番を求める
            Integer maxSeqNo = mapper.getMaxSeqNo(book.getUserId());
            book.setSeqNo((maxSeqNo == null ? 0 : maxSeqNo) + 1);
            // 新規登録
            int count = mapper.insertOne(book);
            log.info("新規登録件数={}件", count);
        } else {
            // 連番が0以外の場合は更新
            int count = mapper.updateOne(book);
            log.info("更新件数={}件", count);
        }
    }

    @Override
    public void deleteBook(String userId, Integer seqNo) {
        mapper.deleteOne(userId, seqNo);
    }

    @Override
    public void batchBook(String userId, List<TBook> bookList) {
        // 連番seqNoを設定する
        Integer maxSeqNo = mapper.getMaxSeqNo(userId);
        int nextSeqNo = (maxSeqNo == null ? 0 : maxSeqNo) + 1;
        for (TBook book : bookList) {
            book.setSeqNo(nextSeqNo);
            nextSeqNo = nextSeqNo + 1;
        }
        mapper.insertMany(bookList);
    }
}
