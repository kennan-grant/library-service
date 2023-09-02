package com.bloomtech.library.services;

import com.amazonaws.event.DeliveryMode;
import com.bloomtech.library.exceptions.CheckableNotFoundException;
import com.bloomtech.library.exceptions.ResourceExistsException;
import com.bloomtech.library.models.Library;
import com.bloomtech.library.models.checkableTypes.*;
import com.bloomtech.library.repositories.CheckableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class CheckableServiceTest {

    //TODO: Inject dependencies and mocks

    @Autowired
    private CheckableService checkableService;

    @MockBean
    private CheckableRepository checkableRepository;

    private List<Checkable> checkables;

    @BeforeEach
    void init() {
        //Initialize test data
        checkables = new ArrayList<>();

        checkables.addAll(
                Arrays.asList(
                        new Media("1-0", "The White Whale", "Melvin H", MediaType.BOOK),
                        new Media("1-1", "The Sorcerer's Quest", "Ana T", MediaType.BOOK),
                        new Media("1-2", "When You're Gone", "Complaining at the Disco", MediaType.MUSIC),
                        new Media("1-3", "Nature Around the World", "DocuSpecialists", MediaType.VIDEO),
                        new ScienceKit("2-0", "Anatomy Model"),
                        new ScienceKit("2-1", "Robotics Kit"),
                        new Ticket("3-0", "Science Museum Tickets"),
                        new Ticket("3-1", "National Park Day Pass")
                )
        );
    }

    @Test
    void getAll() {
        when(checkableRepository.findAll()).thenReturn(checkables);

        List<Checkable> returnedCheckables = checkableService.getAll();

        assertEquals(checkables, returnedCheckables);
        verify(checkableRepository).findAll();

    }

    @Test
    void getByIsbn_checkableExists_returnCheckable() {
        String isbn = "IExist";
        Optional<Checkable> optionalCheckable = Optional.of(mock(Checkable.class));

        when(checkableRepository.findByIsbn(isbn)).thenReturn(optionalCheckable);

        Checkable checkable = checkableService.getByIsbn(isbn);

        assertEquals(checkable, optionalCheckable.get());
    }

    @Test
    void getByIsbn_checkableDoesNotExist_throwCheckableNotFoundException() {
        String isbn = "IDontExist";
        Optional<Checkable> optionalCheckable = Optional.empty();

        when(checkableRepository.findByIsbn(isbn)).thenReturn(optionalCheckable);

        assertThrows(CheckableNotFoundException.class, () -> {
            checkableService.getByIsbn(isbn);
        });
    }

    @Test
    void getByType_checkableExists_returnCheckable() {
        Class<ScienceKit> mockCheckableTypeClass = ScienceKit.class;
        Optional<Checkable> optionalCheckable = Optional.of(mock(mockCheckableTypeClass));

        when(checkableRepository.findByType(mockCheckableTypeClass)).thenReturn(optionalCheckable);

        Checkable checkable = checkableService.getByType(mockCheckableTypeClass);

        assertEquals(checkable, optionalCheckable.get());
        verify(checkableRepository).findByType(mockCheckableTypeClass);

    }

    @Test
    void getByType_checkableDoesNotExist_throwCheckableNotFoundException() {
        Class<ScienceKit> mockCheckableTypeClass = ScienceKit.class;
        Optional<Checkable> optionalCheckable = Optional.empty();

        when(checkableRepository.findByType(mockCheckableTypeClass)).thenReturn(optionalCheckable);

        assertThrows(CheckableNotFoundException.class, () -> {
            checkableService.getByType(mockCheckableTypeClass);
        });

        verify(checkableRepository).findByType(mockCheckableTypeClass);
    }

    @Test
    void save_resourceDoesNotExist_callRepoSave() {
        Checkable mockCheckable = mock(Checkable.class);
        when(checkableRepository.findAll()).thenReturn(checkables);
        when(mockCheckable.getIsbn()).thenReturn("Definitely Not the ISBN You're Looking For");

        checkableService.save(mockCheckable);

        verify(checkableRepository).save(mockCheckable);
    }

    @Test
    void save_resourceAlreadyExists_throwResourceExistsException() {
        String isbn = checkables.get(0).getIsbn();
        Checkable mockCheckable = mock(Checkable.class);

        when(checkableRepository.findAll()).thenReturn(checkables);
        when(mockCheckable.getIsbn()).thenReturn(isbn);

        assertThrows(ResourceExistsException.class, () -> {
            checkableService.save(mockCheckable);
        });
    }

    //TODO: Write Unit Tests for all CheckableService methods and possible Exceptions
}