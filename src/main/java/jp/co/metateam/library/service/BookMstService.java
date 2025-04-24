package jp.co.metateam.library.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.micrometer.common.util.StringUtils;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.repository.BookMstRepository;

@Service
public class BookMstService {

    private final BookMstRepository bookMstRepository;
    
    @Autowired
    public BookMstService(BookMstRepository bookMstRepository){
        this.bookMstRepository = bookMstRepository;
    }
    
    public List<BookMstDto> findAvailableWithStockCount() {
        List<BookMst> books = this.bookMstRepository.findLimitedBook();
        List<BookMstDto> bookMstDtoList = new ArrayList<BookMstDto>();

        // 書籍の在庫数を取得
        // FIXME: 現状は書籍ID毎にDBに問い合わせている。一度のSQLで完了させたい。
        for (int i = 0; i < books.size(); i++) {
            BookMst book = books.get(i);
            BookMstDto bookMstDto = new BookMstDto();
            bookMstDto.setId(book.getId());
            bookMstDto.setIsbn(book.getIsbn());
            bookMstDto.setTitle(book.getTitle());
            bookMstDtoList.add(bookMstDto);
        }

        return bookMstDtoList;
    }

    @PostMapping
    public Boolean checkList(BookMstDto bookMstDto,Model model){
        String booktitle = bookMstDto.getTitle();
        String isbn = bookMstDto.getIsbn();
        boolean errFlg = false;
        List<String>errTitleList = new ArrayList<>();
        List<String> errIsbnList = new ArrayList<>();
 
        if (StringUtils.isEmpty(booktitle)){
            errTitleList.add("書籍名を入力してください");
            model.addAttribute("errtitle",errTitleList);
            errFlg = true;
        }
        if (booktitle.length()>255){
            errTitleList.add("255文字以内で入力してください");
            model.addAttribute("errtitle",errTitleList);
            errFlg = true;
        }
        if (StringUtils.isEmpty(isbn)){
            errIsbnList.add("ISBNを入力してください");
            model.addAttribute("errisbn",errIsbnList);
            errFlg = true;
        }
        if (isbn.length()!=13){
            errIsbnList.add("13文字で入力してください");
            model.addAttribute("errisbn",errIsbnList);
            errFlg = true;
        }
        if (!isbn.matches("^[0-9]+$")){
            errIsbnList.add("半角数字で入力してください");
            model.addAttribute("errisbn",errIsbnList);
            errFlg = true;
        }

        if (!errIsbnList.isEmpty()){
            model.addAttribute("errisbn",errIsbnList);
            errFlg = true;
        }

        //DBに接続して、isbnにヒットする値があるか確認

         String existingBookMsts = bookMstRepository.selectByIsbn (isbn);




        //その値をもとに、isbnの重複チェックを行う
        if(!existingBookMsts.isEmpty()){
            errIsbnList.add("登録済みのISBNです");
            model.addAttribute("errisbn", errIsbnList);
            errFlg= true;
        }

        return errFlg;
        }

    @Transactional
    public void save(BookMstDto bookMstDto) {
        try {
            BookMst bookMst = new BookMst();

            bookMst.setTitle(bookMstDto.getTitle());
            bookMst.setIsbn(bookMstDto.getIsbn());

            this.bookMstRepository.save(bookMst);
        } catch (Exception e) {
            throw e;
        }
    }
}



