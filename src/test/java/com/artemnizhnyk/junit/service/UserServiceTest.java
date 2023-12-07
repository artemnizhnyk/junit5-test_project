package com.artemnizhnyk.junit.service;

import com.artemnizhnyk.junit.dao.UserDao;
import com.artemnizhnyk.junit.dto.User;
import com.artemnizhnyk.junit.paramresolver.UserServiceParamResolver;
import jdk.jshell.spi.ExecutionControl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith({
        UserServiceParamResolver.class,
        MockitoExtension.class
})
public class UserServiceTest {

    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");
    @Captor
    private ArgumentCaptor<Integer> argumentCaptor;
    @Mock
    private UserDao userDao;
    @InjectMocks
    private UserService userService;


    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }

    @BeforeAll
    static void init() {
        System.out.println("Before all: ");

    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
//        this.userDao = mock(UserDao.class);
//        this.userService = new UserService(userDao);
    }

    @Test
    void throwExceptionIfDatabaseIsNotAvailable() {
        doThrow(RuntimeException.class).when(userDao).delete(IVAN.getId());
        assertThrows(RuntimeException.class, () -> userService.delete(IVAN.getId()));

    }

    @Test
    void shouldDeleteExistedUser() {
        userService.add(IVAN);
        Mockito.doReturn(true).when(userDao).delete(IVAN.getId());

        boolean deleteResult = userService.delete(IVAN.getId());

//        ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(userDao).delete(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).isEqualTo(IVAN.getId());

        assertThat(deleteResult).isTrue();
        System.out.println("Artem lox");
    }

    @Test
    void usersEmptyIfNoUsersAdded() {
        System.out.println("Test 1: " + this);
        List<User> users = userService.getAll();
        assertTrue(users.isEmpty());
    }

    @Test
    void usersSizeIfUsersAdded() {
        System.out.println("Test 2: " + this);
        userService.add(IVAN);
        userService.add(PETR);

        List<User> users = userService.getAll();

        assertThat(users).hasSize(2);
    }

    @Test
    @Tag("login")
    void loginSuccessIfUserExist() {
        userService.add(IVAN);
        Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());

        assertTrue(maybeUser.isPresent());
        maybeUser.ifPresent(user -> assertEquals(IVAN, user));
    }

    @Test
    void checkLoginFunctionalityPerformance() {
        Optional<User> maybeUser = assertTimeout(Duration.ofMillis(200L), () -> {
            Thread.sleep(100L);
           return userService.login(IVAN.getUsername(), IVAN.getPassword());
        });
    }

    @Test
    @Tag("login")
    void throwExceptionIfUsernameOrPasswordIsNull() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> userService.login(null, "dummy")),
                () -> assertThrows(IllegalArgumentException.class, () -> userService.login("dummy", null))
        );
    }

    @Test
    void usersConvertedToMapById() {
        userService.add(IVAN, PETR);

        Map<Integer, User> users = userService.getAllConvertedById();

        assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(), PETR.getId()),
                () -> assertThat(users).containsValues(IVAN, PETR)
        );
    }

    @Test
    void loginFailIfPasswordIsNotCorrect() {
        userService.add(IVAN);

        Optional<User> maybeUser = userService.login(IVAN.getUsername(), "dummy");

        assertTrue(maybeUser.isEmpty());
    }

    @Test
    void loginFailIfUserDoesNotExist() {
        userService.add(IVAN);

        Optional<User> maybeUser = userService.login("dummy", IVAN.getPassword());

        assertTrue(maybeUser.isEmpty());
    }

    @ParameterizedTest(name = "{arguments} test")
//    @NullSource
//    @EmptySource
//    @ValueSource(strings = {
//            "Ivan", "Petr"
//    })
    @MethodSource("getArgumentsForLoginTest")
//    @CsvFileSource(resources = "/login-test-data.csv", numLinesToSkip = 1)
//    @CsvSource({
//        "Ivan,123",
//        "Petr,111"
//    })
    void loginParametrizedTest(String username, String password, Optional<User> user) {
        userService.add(IVAN, PETR);

        Optional<User> maybeUser = userService.login(username, password);
        assertThat(maybeUser).isEqualTo(user);
    }

    static Stream<Arguments> getArgumentsForLoginTest() {
        return Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)),
                Arguments.of("Petr", "111", Optional.of(PETR)),
                Arguments.of("Petr", "dummy", Optional.empty()),
                Arguments.of("dummy", "123", Optional.empty())
        );
    }

    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    static void closeConnectionPool() {
        System.out.println("After all: ");
    }
}
