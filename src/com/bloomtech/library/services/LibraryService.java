package com.bloomtech.library.services;

import com.bloomtech.library.exceptions.LibraryNotFoundException;
import com.bloomtech.library.exceptions.ResourceExistsException;
import com.bloomtech.library.models.*;
import com.bloomtech.library.models.checkableTypes.Checkable;
import com.bloomtech.library.models.checkableTypes.Media;
import com.bloomtech.library.repositories.LibraryCardRepository;
import com.bloomtech.library.repositories.LibraryRepository;
import com.bloomtech.library.models.CheckableAmount;
import com.bloomtech.library.views.LibraryAvailableCheckouts;
import com.bloomtech.library.views.OverdueCheckout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LibraryService {

    //TODO: Implement behavior described by the unit tests in tst.com.bloomtech.library.services.LibraryService

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private CheckableService checkableService;

    public List<Library> getLibraries() {
        List<Library> libraries = libraryRepository.findAll();
        return libraries;
    }

    public Library getLibraryByName(String name) {
        Optional<Library> libraryOption = libraryRepository.findByName(name);
        try {
            Library library = libraryOption.get();
            return library;
        } catch (NoSuchElementException e) {
            throw new LibraryNotFoundException("No such library");
        }
    }

    public void save(Library library) {
        List<Library> libraries = libraryRepository.findAll();
        if (libraries.stream().filter(p->p.getName().equals(library.getName())).findFirst().isPresent()) {
            throw new ResourceExistsException("Library with name: " + library.getName() + " already exists!");
        }
        libraryRepository.save(library);
    }

    public CheckableAmount getCheckableAmount(String libraryName, String checkableIsbn) {

        Library library = getLibraryByName(libraryName);
        Checkable checkable = checkableService.getByIsbn(checkableIsbn);
        for (CheckableAmount checkableAmount : library.getCheckables()) {
            if (checkableAmount.getCheckable().equals(checkable)) {
                return checkableAmount;
            }
        }
        return new CheckableAmount(checkable, 0);
    }

    public List<LibraryAvailableCheckouts> getLibrariesWithAvailableCheckout(String isbn) {
        List<LibraryAvailableCheckouts> available = new ArrayList<>();

        Checkable checkable = checkableService.getByIsbn(isbn);
        List<Library> libraries = getLibraries();
        for (Library library : libraries) {
            List<CheckableAmount> checkableAmounts = library.getCheckables();
            for (CheckableAmount checkableAmount : checkableAmounts) {
                if (checkableAmount.getCheckable().equals(checkable)) {
                    LibraryAvailableCheckouts libraryAvailableCheckouts =
                            new LibraryAvailableCheckouts(checkableAmount.getAmount(), library.getName());
                    available.add(libraryAvailableCheckouts);
                    break;
                }
            }
        }
        return available;
    }

    public List<OverdueCheckout> getOverdueCheckouts(String libraryName) {
        List<OverdueCheckout> overdueCheckouts = new ArrayList<>();

        Library library = getLibraryByName(libraryName);
        Set<LibraryCard> libraryCards = library.getLibraryCards();

        for (LibraryCard libraryCard : libraryCards) {
            for (Checkout checkout : libraryCard.getCheckouts()) {
                if (checkout.getDueDate().isBefore(LocalDateTime.now())) {
                    overdueCheckouts.add(new OverdueCheckout(libraryCard.getPatron(), checkout));
                }
            }
        }

        return overdueCheckouts;
    }
}
