package dragon.service;
import java.util.UUID;
import dragon.AuthenticatedAccountContext;
import dragon.repository.HistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class HistoryServiceTest {
    HistoryService historyService;
    @BeforeEach
    void setUp() {
        HistoryRepository historyRepository = new HistoryRepository();
        historyService = new HistoryService(historyRepository);
    }

    @Test
    void test_getAllHistory_positive(){
        AuthenticatedAccountContext.setAuthenticatedUserId(UUID.randomUUID());
        assertEquals(true, historyService.getAllHistory());
    }


    @Test
    void test_getAllHistory_negative(){
        AuthenticatedAccountContext.setAuthenticatedUserId(null);
        assertEquals(false, historyService.getAllHistory());
    }

    @Test
    void test_getRangeHistory_positive() {
        AuthenticatedAccountContext.setAuthenticatedUserId(UUID.randomUUID());
        assertEquals(true, historyService.getRangeHistory("2006-09-01", "2016-09-01"));
    }

    @Test
    void test_getRangeHistory_negative() {
        AuthenticatedAccountContext.setAuthenticatedUserId(UUID.randomUUID());
        assertEquals(false, historyService.getRangeHistory("2026-09-01", "2016-09-01"));
    }

}
