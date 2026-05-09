package interfaces;

import education.Book;

public interface CanBorrowBook {
    void borrowBook(Book book);
    void returnBook(Book book);
}
