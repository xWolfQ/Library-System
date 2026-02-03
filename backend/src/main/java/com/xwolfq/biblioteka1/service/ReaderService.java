// java
package com.xwolfq.biblioteka1.service;

import com.xwolfq.biblioteka1.dto.ReaderSelectDTO;
import com.xwolfq.biblioteka1.exceptions.ReaderLoanedException;
import com.xwolfq.biblioteka1.model.Reader;
import com.xwolfq.biblioteka1.repository.LoanRepository;
import com.xwolfq.biblioteka1.repository.ReaderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReaderService {

    private final ReaderRepository readerRepo;
    private final LoanRepository loanRepo;

    public ReaderService(ReaderRepository readerRepo, LoanRepository loanRepo) {
        this.readerRepo = readerRepo;
        this.loanRepo = loanRepo;
    }

    public List<Reader> getAll() {
        return readerRepo.findByActiveTrue();
    }

    public void create(Reader reader) {
        if (reader.getFirstName() == null || reader.getFirstName().isBlank())
            throw new IllegalArgumentException("Musisz wpisać Imię czytelnika");

        if (reader.getLastName() == null || reader.getLastName().isBlank())
            throw new IllegalArgumentException("Musisz wpisać Nazwisko czytelnika");

        if (reader.getEmail() == null || reader.getEmail().isBlank())
            throw new IllegalArgumentException("Email czytelnika jest wymagany");

        if (reader.getPhoneNumber() == null || reader.getPhoneNumber().isBlank())
            throw new IllegalArgumentException("Numer telefonu czytelnika jest wymagany");

        reader.setActive(true);
        readerRepo.save(reader);
    }

    public boolean delete(long id) {
        if (!readerRepo.existsById(id)) {
            return false;
        }

        // jeśli są aktywne wypożyczenia -> wyjątek (obsłuży ControllerAdvice)
        if (loanRepo.existsByBookIdAndReturnDateIsNull(id)) {
            throw new ReaderLoanedException("Nie można usunąć czytelnika: istnieją aktywne wypożyczenia");
        }

        // soft delete
        Reader reader = readerRepo.findById(id).orElse(null);
        if (reader == null) return false;
        reader.setActive(false);
        readerRepo.save(reader);
        return true;
    }

    public List<ReaderSelectDTO> getForSelect() {
        return readerRepo.findAll()
                .stream()
                .filter(Reader::isActive)
                .map(r -> new ReaderSelectDTO(r.getId(), r.getFirstName(), r.getLastName()))
                .collect(Collectors.toList());
    }

    public void update(long id, Reader newData) {
        Reader reader = readerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono czytelnika"));

        reader.setFirstName(newData.getFirstName());
        reader.setLastName(newData.getLastName());
        reader.setEmail(newData.getEmail());
        reader.setPhoneNumber(newData.getPhoneNumber());

        readerRepo.save(reader);
    }

    @Transactional
    public void deactivate(Long readerId) {
        if (loanRepo.existsByReaderIdAndReturnDateIsNull(readerId)) {
            throw new ReaderLoanedException(
                    "Nie można dezaktywować czytelnika z aktywnym wypożyczeniem"
            );
        }

        Reader reader = readerRepo.findById(readerId)
                .orElseThrow(() -> new IllegalArgumentException("Czytelnik nie istnieje"));

        reader.setActive(false);
        readerRepo.save(reader);
    }
}
