package my.service.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.service.domain.model.MGenre;
import my.service.domain.model.TBook;
import my.service.domain.service.TBookService;
import my.service.dto.RestBook;
import my.service.dto.RestBookSearchCondition;
import my.service.dto.RestErrorMsg;
import my.service.dto.RestGenre;

@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final TBookService service;

    @PostMapping("/search")
    public ResponseEntity<List<RestBook>> searchBook(@RequestBody RestBookSearchCondition condition) {
        List<TBook> bookList = service.getBooks(condition);
        List<RestBook> restBookList = bookList.stream()
                .map(this::toRestBook)
                .toList();
        return ResponseEntity.ok(restBookList);
    }

    private RestBook toRestBook(TBook book) {
        RestBook restBook = new RestBook();
        restBook.setUserId(book.getUserId());
        restBook.setSeqNo(book.getSeqNo());
        restBook.setTitle(book.getTitle());
        restBook.setAuthor(book.getAuthor());
        restBook.setPrice(book.getPrice());
        restBook.setPublished(formatDate(book.getPublished()));
        restBook.setPublisher(book.getPublisher());
        restBook.setBuyDate(formatDate(book.getBuyDate()));
        restBook.setCompleteDate(formatDate(book.getCompleteDate()));
        restBook.setGenre(book.getGenre() == null ? null : new RestGenre(book.getGenre().getId(), book.getGenre().getName()));
        restBook.setMemo(book.getMemo());
        restBook.setRate(book.getRate());
        restBook.setImgUrl(book.getImgUrl());
        restBook.setInfoUrl(book.getInfoUrl());
        return restBook;
    }

    @PostMapping("/regist")
    public ResponseEntity<String> registBook(@RequestBody RestBook requestBook) {
        log.info(requestBook.toString());
        try {
            TBook book = new TBook();
            book.setUserId(requestBook.getUserId());
            book.setSeqNo(requestBook.getSeqNo());
            book.setTitle(requestBook.getTitle());
            book.setAuthor(requestBook.getAuthor());
            book.setPrice(requestBook.getPrice());
            book.setPublished(parseDate(requestBook.getPublished()));
            book.setPublisher(requestBook.getPublisher());
            book.setBuyDate(parseDate(requestBook.getBuyDate()));
            book.setCompleteDate(parseDate(requestBook.getCompleteDate()));
            book.setMemo(requestBook.getMemo());
            book.setRate(requestBook.getRate());
            book.setImgUrl(requestBook.getImgUrl());
            book.setInfoUrl(requestBook.getInfoUrl());
            book.setGenreId(requestBook.getGenre().getId());
            book.setGenre(new MGenre(requestBook.getGenre().getId(), requestBook.getGenre().getName()));
            service.registBook(book);
            return ResponseEntity.status(HttpStatus.OK).body("本情報を登録しました。");
        } catch (Exception e) {
            log.error("本情報登録エラー", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("本情報登録を失敗しました。");
        }
    }

    @PostMapping("/delete/{userId}/{seqNo}")
    public ResponseEntity<String> deleteBook(@PathVariable("userId") String userId, @PathVariable("seqNo") Integer seqNo) {
        try {
            service.deleteBook(userId, seqNo);
            return ResponseEntity.status(HttpStatus.OK).body("本情報を削除しました。");
        } catch (Exception e) {
            log.error("本情報削除エラー", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("本情報削除を失敗しました。");
        }
    }

    @PostMapping("/batch/{userId}")
    public ResponseEntity<?> batchRegist(@PathVariable("userId") String userId, @RequestParam("uploadFile") MultipartFile uploadFile) throws Exception {
        if (uploadFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("アップロードファイルが空です。");
        }
        try {
            ParseResult result = parseCsv(userId, uploadFile);
            if (!result.errorList().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.errorList());
            }
            service.batchBook(userId, result.bookList());
            return ResponseEntity.status(HttpStatus.OK).body(result.bookList().size() + "件の本情報を登録しました。");
        } catch (Exception e) {
            log.error("本情報一括登録エラー", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("本情報一括登録を失敗しました。");
        }
    }

    @PostMapping("/download")
    public ResponseEntity<?> downloadExcel(@RequestBody RestBookSearchCondition condition) {
        List<TBook> bookList = service.getBooks(condition);
        try {
            byte[] excel = writeExcel(bookList);

            String filename = "台帳.xlsx";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excel);
        } catch (IOException e) {
            log.error("本情報ダウンロードエラー", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("本情報ダウンロードを失敗しました。");
        }
    }

    private record ParseResult(List<TBook> bookList, List<RestErrorMsg> errorList) {
    }

    private ParseResult parseCsv(String userId, MultipartFile uploadFile) throws IOException {
        List<TBook> bookList = new ArrayList<>();
        List<RestErrorMsg> errorList = new ArrayList<>();
        try (Reader reader = new InputStreamReader(uploadFile.getInputStream(), StandardCharsets.UTF_8);
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader("title", "author", "price", "publisher", "published", "buyDate",
                                    "completeDate", "genreId", "rate", "memo", "imgUrl", "infoUrl")
                        .setSkipHeaderRecord(false)  // ヘッダー行はない想定
                        .setTrim(true)
                        .setIgnoreSurroundingSpaces(true)
                        .get()
                        .parse(reader)) {
            int lineNo = 0;
            for (CSVRecord record : parser) {
                lineNo++;
                boolean isError = false;
                // 項目数をチェック
                if (record.size() != parser.getHeaderNames().size()) {
                    errorList.add(new RestErrorMsg(lineNo, "項目数に誤りがあります。項目数は12です。"));
                    continue;
                }
                
                TBook book = new TBook();
                // 連番seqNoは後から設定する。
                // ユーザID
                book.setUserId(userId);
                // タイトル
                book.setTitle(record.get("title"));
                // 著者
                book.setAuthor(record.get("author"));
                // 値段
                try {
                    book.setPrice(parseInt(record.get("price")));
                } catch (NumberFormatException e) {
                    errorList.add(new RestErrorMsg(lineNo, "値段には数字を入れてください。"));
                    isError = true;
                }
                // 出版社
                book.setPublisher(record.get("publisher"));
                // 発売日
                try {
                    book.setPublished(parseDate(record.get("published")));
                    if (book.getPublished() == null) {
                        errorList.add(new RestErrorMsg(lineNo, "発売日は必須です。"));
                        isError = true;
                    }
                } catch (DateTimeParseException e) {
                    errorList.add(new RestErrorMsg(lineNo, "発売日のフォーマットに誤りがあります。"));
                    isError = true;
                }
                // 購入日
                try {
                    book.setBuyDate(parseDate(record.get("buyDate")));
                    if (book.getBuyDate() == null) {
                        errorList.add(new RestErrorMsg(lineNo, "購入日は必須です。"));
                        isError = true;
                    }
                } catch (DateTimeParseException e) {
                    errorList.add(new RestErrorMsg(lineNo, "購入日のフォーマットに誤りがあります。"));
                    isError = true;
                }
                // 読了日
                try {
                    book.setCompleteDate(parseDate(record.get("completeDate")));
                } catch (DateTimeParseException e) {
                    errorList.add(new RestErrorMsg(lineNo, "読了日のフォーマットに誤りがあります。"));
                    isError = true;
                }
                // 感想
                book.setMemo(record.get("memo"));
                // 評価
                try {
                    Integer rate = parseInt(record.get("rate"));
                    book.setRate(rate);
                    if (rate == null) {
                        errorList.add(new RestErrorMsg(lineNo, "評価は必須です。"));
                        isError = true;
                    } else if (rate <= 0 || rate >= 6) {
                        errorList.add(new RestErrorMsg(lineNo, "評価には1～5を入れてください。"));
                        isError = true;
                    }
                } catch (NumberFormatException e) {
                    errorList.add(new RestErrorMsg(lineNo, "評価には数字を入れてください。"));
                    isError = true;
                }
                // ジャンル
                try {
                    Integer genreId = parseInt(record.get("genreId"));
                    book.setGenreId(genreId);
                    if (genreId == null) {
                        errorList.add(new RestErrorMsg(lineNo, "ジャンルは必須です。"));
                        isError = true;
                    } else if (genreId <= 0 || genreId >= 6) {
                        errorList.add(new RestErrorMsg(lineNo, "ジャンルには1～5を入れてください。"));
                        isError = true;
                    }
                } catch (NumberFormatException e) {
                    errorList.add(new RestErrorMsg(lineNo, "ジャンルには数字を入れてください。"));
                    isError = true;
                }
                // 画像URL
                book.setImgUrl(record.get("imgUrl"));
                // 情報URL
                book.setInfoUrl(record.get("infoUrl"));
                if (isError) {
                    continue;
                }
                bookList.add(book);
            }
        }
        return new ParseResult(bookList, errorList);
    }

    private static Integer parseInt(String value) {
        return (value == null || value.isBlank()) ? null : Integer.parseInt(value);
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // yyyy-MM-dd形式の文字列をLocalDateに変換する。未入力の場合はnullを返す
    private static LocalDate parseDate(String date) {
        return (date == null || date.isBlank()) ? null : LocalDate.parse(date, DATE_FORMATTER);
    }

    // LocalDateをyyyy-MM-dd形式の文字列に変換する。未設定の場合はnullを返す
    private static String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMATTER);
    }

    private byte[] writeExcel(List<TBook> bookList) throws IOException {
        try (InputStream templateStream = getClass().getResourceAsStream("/template/template.xlsx")) { // 引数のパスの先頭にはスラッシュ(/)が必要
            if (templateStream == null) {
                log.error("テンプレートファイルが見つかりません。src/main/resources/template/template.xlsx を確認してください。");
                throw new IOException();
            }
            // テンプレートからワークブックを作成
            try (Workbook workbook = new XSSFWorkbook(templateStream)) {
                // シート取得
                Sheet sheet = workbook.getSheetAt(0);

                // セルの枠線を設定する。
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setBorderLeft(BorderStyle.THIN);
                cellStyle.setBorderRight(BorderStyle.THIN);
                cellStyle.setBorderTop(BorderStyle.THIN);
                cellStyle.setBorderBottom(BorderStyle.THIN);
                // 上詰め
                cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
                // 改行して表示
                cellStyle.setWrapText(true);
                // 日付用のセルスタイル
                CellStyle dateCellStyle = workbook.createCellStyle();
                dateCellStyle.cloneStyleFrom(cellStyle);
                dateCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
                // 値段用のセルスタイル
                CellStyle priceCellStyle = workbook.createCellStyle();
                priceCellStyle.cloneStyleFrom(cellStyle);
                priceCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));

                // リストから1行づつ取り出してセルに設定していく
                for (int rowIndex = 2; rowIndex <= bookList.size() + 1; rowIndex++) {
                    // 行を生成
                    Row row = sheet.getRow(rowIndex);
                    if(row == null){
                        row = sheet.createRow(rowIndex);
                    }
                    // セルに値を設定していく
                    TBook bookInfo = bookList.get(rowIndex - 2);
                    // No
                    setCellValue(row, 0, rowIndex-1, cellStyle);
                    // タイトル
                    setCellValue(row, 1, bookInfo.getTitle(), cellStyle);
                    // 著者
                    setCellValue(row, 2, bookInfo.getAuthor(), cellStyle);
                    // 値段
                    setCellValue(row, 3, bookInfo.getPrice(), priceCellStyle);
                    // 出版社
                    setCellValue(row, 4, bookInfo.getPublisher(), cellStyle);
                    // 発売日
                    setCellValue(row, 5, bookInfo.getPublished(), dateCellStyle);
                    // String pattern = "yyyy-MM-dd";
                    // SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                    // if (StringUtils.isNotEmpty(bookInfo.getPublished())) {
                    //     Date publishedDate = sdf.parse(bookInfo.getPublished());
                    //     setCellValue(row, 5, publishedDate, dateCellStyle);
                    // } else {
                    //     setCellValue(row, 5, "ー", dateCellStyle);
                    // }
                    // 購入日
                    setCellValue(row, 6, bookInfo.getBuyDate(), dateCellStyle);
                    // if (StringUtils.isNotEmpty(bookInfo.getBuyDate())) {
                    //     Date buyDate = sdf.parse(bookInfo.getBuyDate());
                    //     setCellValue(row, 6, buyDate, dateCellStyle);
                    // } else {
                    //     setCellValue(row, 6, "ー", dateCellStyle);
                    // }
                    // 読了日
                    setCellValue(row, 7, bookInfo.getCompleteDate(), dateCellStyle);
                    // if (StringUtils.isNotEmpty(bookInfo.getCompleteDate())) {
                    //     Date completeDate = sdf.parse(bookInfo.getCompleteDate());
                    //     setCellValue(row, 7, completeDate, dateCellStyle);
                    // } else {
                    //     setCellValue(row, 7, "ー", dateCellStyle);
                    // }
                    // ジャンル
                    setCellValue(row, 8, bookInfo.getGenre().getName(), cellStyle);
                    // 評価
                    String strRate = "";
                    switch (bookInfo.getRate()) {
                        case 1:
                            strRate = "面白くない";
                            break;
                        case 2:
                            strRate = "あまり面白くない";
                            break;
                        case 3:
                            strRate = "普通";
                            break;
                        case 4:
                            strRate = "面白い";
                            break;
                        case 5:
                            strRate = "とても面白い";
                            break;
                        default:
                            strRate = "";
                            break;
                    }
                    setCellValue(row, 9, strRate, cellStyle);
                    // 感想
                    setCellValue(row, 10, bookInfo.getMemo(), cellStyle);
                }
                return byteArray(workbook);
            }
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * セルに値を設定する。
     * @param row
     * @param cellIndex
     * @param value
     * @param cellStyle
     */
    private void setCellValue(Row row, int cellIndex, Object value, CellStyle cellStyle) {
        Cell cell = row.getCell(cellIndex);
        if(cell == null){
            cell = row.createCell(cellIndex);
        }
        if (cellStyle != null) {
            cell.setCellStyle(cellStyle);
        }
        if (value != null) {
            if (value instanceof String) {
                cell.setCellValue((String) value);
            } else if (value instanceof Number numValue) {
                if (numValue instanceof Float floatValue) {
                    numValue = Double.valueOf(String.valueOf(floatValue));
                }
                cell.setCellValue(numValue.doubleValue());
            } else if (value instanceof LocalDate) {
                LocalDate dateValue = (LocalDate) value;
                cell.setCellValue(dateValue);
            }
        }
    }

    /**
     * ワークブックからbyte配列取得
     * @param workbook
     * @return
     * @throws IOException
     */
    private byte[] byteArray(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IOException();
        }
   }
}
